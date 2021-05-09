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
        //TODO: CHANGE
        private const val DEFAULT_ALTITUDE: Float = 20f
    }

    override fun startMission(context: Context, missionPlan: Mission.MissionPlan): Completable {
        if (missionPlan.missionItems.isEmpty())
            throw IllegalArgumentException("Cannot start an empty mission")

        val instance = getInstance()

        val home = data.getHomeLocation().value ?: throw IllegalStateException("Could not query launch point")
        missionPlan.missionItems.add(
            DroneUtils.generateMissionItem(
                home.latitudeDeg,
                home.longitudeDeg,
                home.relativeAltitudeM,
                missionPlan.missionItems[0].gimbalPitchDeg,
                false))

        // Allowed start states are Landed, arming, taking off. Starting a mission on other states
        // is dangerous and might break things
        return when(val startStatus = data.getDroneStatus().value) {
            IDLE, ARMING -> arm(context, instance, missionPlan)
            TAKING_OFF -> takeoff(context, instance, missionPlan)
            STARTING_MISSION -> start(context, instance, missionPlan)
            else -> throw IllegalStateException("Cannot start a new mission in state $startStatus")
        }
    }

    override fun returnToHomeLocationAndLand(context: Context): Completable {
        val returnLocation = data.getHomeLocation().value
                ?: throw IllegalStateException(context.getString(R.string.drone_return_error))

        val location = LatLng(returnLocation.latitudeDeg, returnLocation.longitudeDeg)
        val altitude = returnLocation.relativeAltitudeM

        return goToLocation(context, location, altitude)
    }

    override fun returnToUserLocationAndLand(context: Context): Completable {
        if (!locationService.isLocationEnabled())
            throw IllegalStateException("Location is not enabled")

        val userPosition = locationService.getCurrentLocation()!!
        val altitude = data.getPosition().value?.altitude?.toFloat() ?: DEFAULT_ALTITUDE

        return goToLocation(context, userPosition, altitude)
    }

    private fun goToLocation(context: Context, returnLocation: LatLng, altitude: Float): Completable {

        val droneInstance = getInstance()
        val missionPlan = DroneUtils.makeDroneMission(listOf(returnLocation), altitude,0f)

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
                    ToastHandler.showToastAsync(context, R.string.drone_mission_success)
                })
    }

    override fun pauseMission(context: Context): Completable {
        val instance = getInstance()

        return instance.mission.pauseMission().doOnComplete {
            data.getMutableMissionPaused().postValue(true)
            ToastHandler.showToastAsync(context, R.string.drone_pause_success)
        }
    }

    override fun resumeMission(context: Context): Completable {
        val instance = getInstance()

        return instance.mission.startMission().doOnComplete {
            data.getMutableMissionPaused().postValue(false)
            ToastHandler.showToastAsync(context, R.string.drone_mission_success)
        }
    }

    private fun arm(context: Context, instance: System, missionPlan: Mission.MissionPlan,
                    before: Completable = Completable.complete()): Completable {

        val armed = before.doOnComplete{ data.getMutableDroneStatus().postValue(ARMING) }
                .andThen(instance.action.arm())
                .retry{ attempt, ex ->
                    Timber.e(ex,"Failure while arming the drone, try $attempt / $MAX_RETRIES")
                    ToastHandler.showToastAsync(context, R.string.drone_mission_retry, Toast.LENGTH_LONG,
                            ex, attempt, MAX_RETRIES)
                    attempt < MAX_RETRIES
                }.doOnComplete { ToastHandler.showToastAsync(context, "Drone armed") }

        return takeoff(context, instance, missionPlan, armed)
    }

    private fun takeoff(context: Context, instance: System, missionPlan: Mission.MissionPlan,
                        before: Completable = Completable.complete()): Completable {

        val takeoff = before.doOnComplete{ data.getMutableDroneStatus().postValue(TAKING_OFF) }
                .andThen(instance.action.takeoff())
                .doOnComplete{ ToastHandler.showToastAsync(context, "Drone took off") }

        return start(context, instance, missionPlan, takeoff)
    }

    private fun start(context: Context, instance: System, missionPlan: Mission.MissionPlan,
                      before: Completable = Completable.complete()): Completable {

        val startMission = before.doOnComplete { data.getMutableDroneStatus().postValue(STARTING_MISSION) }
                .andThen(instance.mission.setReturnToLaunchAfterMission(true))
                .andThen(instance.mission.uploadMission(missionPlan)
                        .andThen(instance.mission.startMission())
                        .doOnComplete {
                            data.getMutableDroneStatus().postValue(EXECUTING_MISSION)
                            data.getMutableMission().postValue(missionPlan.missionItems.dropLast(1))
                            data.getMutableMissionPaused().postValue(false)
                            ToastHandler.showToastAsync(context, R.string.drone_mission_success)
                        })

        return finish(context, instance, startMission)
    }

    private fun finish(context: Context, instance: System, before: Completable): Completable {
        return before.andThen(instance.mission.missionProgress).distinctUntilChanged()
                .filter{ it.current >= it.total - 1 }.firstOrError().toCompletable()
                .doOnComplete{
                    ToastHandler.showToastAsync(context, "Mission done, returning to launch point")
                    data.getMutableDroneStatus().postValue(GOING_BACK)
                }
                .andThen(instance.mission.missionProgress).distinctUntilChanged()
                .filter{ it.current == it.total }.firstOrError().toCompletable()
                .doOnComplete{ data.getMutableDroneStatus().postValue(LANDING) }
                .andThen(instance.action.land())
                .andThen(instance.action.disarm())
                .doOnComplete{
                    // Completion
                    data.getMutableDroneStatus().postValue(IDLE)
                    data.getMutableMission().postValue(null)
                    data.getMutableMissionPaused().postValue(true)
                }
    }

    private fun getInstance(): System = service.provideDrone()
            ?: throw IllegalStateException("Could not query drone instance")
}
