/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.drone

import android.content.Context
import android.widget.Toast
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.api.drone.DroneData.DroneStatus.*
import ch.epfl.sdp.drone3d.service.api.drone.DroneDataEditable
import ch.epfl.sdp.drone3d.service.api.drone.DroneExecutor
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.ui.ToastHandler
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.System
import io.mavsdk.mission.Mission
import io.mavsdk.telemetry.Telemetry.FlightMode.*
import io.reactivex.Completable
import timber.log.Timber

/**
 * This class is an implementation of [DroneExecutor]. As such it is responsible of the mission management of the drone.
 *
 * Parts of this code were taken from the fly2find project and adapted to our needs.
 */
class DroneExecutorImpl(
    private val service: DroneService,
    private val data: DroneDataEditable,
    private val locationService: LocationService
) : DroneExecutor {

    companion object {
        private const val MAX_RETRIES: Int = 5
        private const val DEFAULT_ALTITUDE: Float = 20f
    }

    override fun startMission(ctx: Context, missionPlan: Mission.MissionPlan): Completable {
        if (missionPlan.missionItems.isEmpty())
            throw IllegalArgumentException("Cannot start an empty mission")

        val instance = getInstance()

        val home = data.getHomeLocation().value ?: throw IllegalStateException("Could not query launch point")
        missionPlan.missionItems.add(
            DroneUtils.generateMissionItem(
                home.latitudeDeg,
                home.longitudeDeg,
                home.relativeAltitudeM))

        return instance.core.connectionState.filter{ it.isConnected } // The drone must be connected
                    .firstOrError().toCompletable()
                .doOnComplete{ data.getMutableDroneStatus().postValue(ARMING) }
                // Arm the drone if not already
                .andThen(instance.telemetry.armed.firstOrError()
                        .flatMapCompletable { if(it) armed(ctx) else arm(ctx, instance) })
                // now, the drone is armed for sure. Do what we should based on the flight mode
                .andThen(instance.telemetry.flightMode.firstOrError()
                        .flatMapCompletable { curFlightMode ->
                            when(curFlightMode) {
                                // Ready -> takeoff
                                READY -> takeoff(ctx, instance, missionPlan)
                                // taking off -> wait until it ends to upload the mission
                                TAKEOFF -> {
                                    data.getMutableDroneStatus().postValue(ARMING)
                                    instance.telemetry.flightMode.filter { it == HOLD }
                                            .firstOrError().toCompletable()
                                    .andThen(start(ctx, instance, missionPlan))
                                }
                                // Holding position, start mission
                                HOLD -> start(ctx, instance, missionPlan)
                                // A mission is already in progress, cannot start a new one
                                MISSION -> {
                                    ToastHandler.showToastAsync(ctx, R.string.mission_already_in_progress)
                                    data.getMutableDroneStatus().postValue(EXECUTING_MISSION)
                                    finish(ctx, instance)
                                }
                                LAND -> {
                                    ToastHandler.showToastAsync(ctx, R.string.mission_already_in_progress)
                                    data.getMutableDroneStatus().postValue(LANDING)
                                    instance.telemetry.flightMode.filter { it == READY }
                                            .firstOrError().toCompletable()
                                    .andThen(end(instance))
                                }
                                else -> throw IllegalStateException("Unknown state : $curFlightMode")
                            }
                        })
    }

    private fun armed(ctx: Context): Completable =
            Completable.fromCallable { ToastHandler.showToastAsync(ctx, "Drone already armed") }

    private fun arm(ctx: Context, instance: System): Completable =
            instance.action.arm()
                    .retry{ attempt, ex ->
                        Timber.e(ex,"Failure while arming the drone, try $attempt / $MAX_RETRIES")
                        ToastHandler.showToastAsync(ctx, R.string.drone_mission_retry, Toast.LENGTH_LONG,
                                ex, attempt, MAX_RETRIES)
                        attempt < MAX_RETRIES
                    }
            .doOnComplete { ToastHandler.showToastAsync(ctx, "Drone armed") }

    override fun returnToHomeLocationAndLand(ctx: Context): Completable {
        val returnLocation = data.getHomeLocation().value
                ?: throw IllegalStateException(ctx.getString(R.string.drone_return_error))

        val location = LatLng(returnLocation.latitudeDeg, returnLocation.longitudeDeg)
        val altitude = returnLocation.relativeAltitudeM

        return goToLocation(ctx, location, altitude)
    }

    override fun returnToUserLocationAndLand(ctx: Context): Completable {
        if (!locationService.isLocationEnabled())
            throw IllegalStateException("Location is not enabled")

        val userPosition = locationService.getCurrentLocation()!!
        val altitude = data.getPosition().value?.altitude?.toFloat() ?: DEFAULT_ALTITUDE

        return goToLocation(ctx, userPosition, altitude)
    }

    private fun goToLocation(ctx: Context, returnLocation: LatLng, altitude: Float): Completable {

        val droneInstance = getInstance()
        val missionPlan = DroneUtils.makeDroneMission(listOf(returnLocation), altitude)

        return droneInstance.mission.pauseMission()
            .doOnComplete {
                data.getMutableMissionPaused().postValue(true)
                data.getMutableDroneStatus().postValue(STARTING_MISSION)
            }.andThen(droneInstance.mission.setReturnToLaunchAfterMission(false))
            .andThen(droneInstance.mission.uploadMission(missionPlan)
            .andThen(droneInstance.mission.startMission())
                .doOnComplete {
                    data.getMutableDroneStatus().postValue(EXECUTING_MISSION)
                    data.getMutableMission().postValue(null)
                    data.getMutableMissionPaused().postValue(false)
                    ToastHandler.showToastAsync(ctx, R.string.drone_mission_success)
                })
    }

    override fun pauseMission(ctx: Context): Completable {
        val instance = getInstance()

        return instance.mission.pauseMission().doOnComplete {
            data.getMutableMissionPaused().postValue(true)
            ToastHandler.showToastAsync(ctx, R.string.drone_pause_success)
        }
    }

    override fun resumeMission(ctx: Context): Completable {
        val instance = getInstance()

        return instance.mission.startMission().doOnComplete {
            data.getMutableMissionPaused().postValue(false)
            ToastHandler.showToastAsync(ctx, R.string.drone_mission_success)
        }
    }

    private fun takeoff(ctx: Context, instance: System, missionPlan: Mission.MissionPlan): Completable =
            Completable.fromCallable { data.getMutableDroneStatus().postValue(TAKING_OFF) } 
                    .andThen(instance.action.takeoff())
                        .doOnComplete{ ToastHandler.showToastAsync(ctx, "Drone took off") }
                    .andThen(start(ctx, instance, missionPlan))

    private fun start(ctx: Context, instance: System, missionPlan: Mission.MissionPlan): Completable = 
            Completable.fromCallable { data.getMutableDroneStatus().postValue(STARTING_MISSION) }
                    .andThen(instance.mission.setReturnToLaunchAfterMission(false))
                    .andThen(instance.mission.uploadMission(missionPlan)
                            .andThen(instance.mission.startMission())
                            .doOnComplete {
                                data.getMutableDroneStatus().postValue(EXECUTING_MISSION)
                                data.getMutableMission().postValue(missionPlan.missionItems.dropLast(1))
                                data.getMutableMissionPaused().postValue(false)
                                ToastHandler.showToastAsync(ctx, R.string.drone_mission_success)
                            })
                    .andThen(finish(ctx, instance))

    private fun finish(ctx: Context, instance: System): Completable = 
            instance.mission.missionProgress.distinctUntilChanged()
                            .filter{ it.current >= it.total - 1 }.firstOrError().toCompletable()
                            .doOnComplete{
                                ToastHandler.showToastAsync(ctx, "Mission done, returning to launch point")
                                data.getMutableDroneStatus().postValue(GOING_BACK)
                            }
                    .andThen(instance.mission.missionProgress).distinctUntilChanged()
                            .filter{ it.current == it.total }.firstOrError().toCompletable()
                            .doOnComplete{ data.getMutableDroneStatus().postValue(LANDING) }
                    .andThen(instance.action.land())
                    .andThen(end(instance))
    
    private fun end(instance: System) = 
            instance.action.disarm()
                    .doOnComplete{
                        // Completion
                        data.getMutableDroneStatus().postValue(IDLE)
                        data.getMutableMission().postValue(null)
                        data.getMutableMissionPaused().postValue(true)
                    }

    private fun getInstance(): System = service.provideDrone()
            ?: throw IllegalStateException("Could not query drone instance")
}
