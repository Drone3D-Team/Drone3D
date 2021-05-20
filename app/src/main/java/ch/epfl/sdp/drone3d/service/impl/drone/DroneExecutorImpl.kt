/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.drone

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
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
import io.reactivex.Flowable
import timber.log.Timber
import kotlin.math.PI

/**
 * This class is an implementation of [DroneExecutor]. As such it is responsible of the mission management of the drone.
 *
 * This code was inspired by the fly2find project.
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
                missionPlan.missionItems[0].relativeAltitudeM))

        val disarmed = changeFromTo(instance.telemetry.armed)
                .doOnComplete { throw Error(ctx.getString(R.string.drone_disarmed_during_setup)) }

        val mission = connected(instance)
                .doOnComplete{ data.getMutableDroneStatus().postValue(ARMING) }
                .andThen(armedAndFlying(ctx, instance))
                .andThen(sendMissionAndStart(ctx, instance, missionPlan))

        return Completable.ambArray(mission, disarmed)
                .andThen(finish(ctx, instance))
    }

    private fun connected(instance: System): Completable =
            instance.core.connectionState.filter{ it.isConnected }.firstOrError().toCompletable()

    private fun armedAndFlying(ctx: Context, instance: System): Completable =
            instance.telemetry.armed.firstOrError()
                    .flatMapCompletable { if(it) armed(ctx) else arm(ctx, instance) }
                    .doOnComplete{ data.getMutableDroneStatus().postValue(TAKING_OFF) }
                    // Takeoff if the drone isn't flying
                    .andThen(instance.telemetry.inAir.firstOrError())
                    .flatMapCompletable { if(it) flying(ctx) else takeoff(ctx, instance) }

    private fun armed(ctx: Context): Completable =
            Completable.fromCallable { ToastHandler.showToastAsync(ctx, R.string.drone_already_armed) }

    private fun arm(ctx: Context, instance: System): Completable =
            instance.action.arm()
                    .retry{ attempt, ex ->
                        Timber.e(ex,"Failure while arming the drone, try $attempt / $MAX_RETRIES")
                        ToastHandler.showToastAsync(ctx, R.string.drone_mission_retry, Toast.LENGTH_LONG,
                                ex, attempt, MAX_RETRIES)
                        attempt < MAX_RETRIES
                    }
                    .doOnComplete { ToastHandler.showToastAsync(ctx, R.string.drone_armed) }

    private fun flying(ctx: Context): Completable =
        Completable.fromCallable { ToastHandler.showToastAsync(ctx, R.string.drone_already_flying) }

    private fun takeoff(ctx: Context, instance: System): Completable =
            instance.action.takeoff()
                    .doOnComplete{ ToastHandler.showToastAsync(ctx, R.string.drone_took_off) }

    private fun sendMissionAndStart(ctx: Context,
                                    instance: System,
                                    missionPlan: Mission.MissionPlan): Completable =
        instance.telemetry.flightMode.firstOrError()
                .flatMapCompletable { flightMode ->
                    when(flightMode) {
                        // Ready to start the mission
                        READY, HOLD, LAND, TAKEOFF ->
                            start(ctx, instance, missionPlan)
                        // A mission is already in progress, cannot start a new one
                        MISSION -> {
                            ToastHandler.showToastAsync(ctx, R.string.mission_already_in_progress)
                            data.getMutableDroneStatus().postValue(EXECUTING_MISSION)
                            Completable.complete()
                        }
                        else -> throw IllegalStateException("Unknown state : $flightMode")
                    }
                }

    override fun returnToHomeLocationAndLand(ctx: Context): Completable {
        val returnLocation = data.getHomeLocation().value
                ?: throw IllegalStateException(ctx.getString(R.string.drone_return_error))

        val location = LatLng(returnLocation.latitudeDeg, returnLocation.longitudeDeg)
        val altitude = returnLocation.relativeAltitudeM

        return returnTo(ctx, location, altitude, R.string.drone_mission_return_launch)
    }

    override fun returnToUserLocationAndLand(ctx: Context): Completable {
        if (!locationService.isLocationEnabled())
            throw IllegalStateException("Location is not enabled")

        val userPosition = locationService.getCurrentLocation()!!
        val altitude = data.getPosition().value?.altitude?.toFloat() ?: DEFAULT_ALTITUDE

        return returnTo(ctx, userPosition, altitude, R.string.drone_mission_to_user)
    }

    private fun returnTo(ctx: Context, returnLocation: LatLng, altitude: Float, @StringRes msg: Int): Completable {

        val droneInstance = getInstance()
        val missionPlan = DroneUtils.makeDroneMission(listOf(returnLocation), altitude)

        return droneInstance.mission.pauseMission()
            .doOnComplete {
                data.getMutableMissionPaused().postValue(true)
                data.getMutableDroneStatus().postValue(SENDING_ORDER)
            }
            .andThen(droneInstance.mission.uploadMission(missionPlan)
            .andThen(droneInstance.mission.startMission())
                .doOnComplete {
                    data.getMutableDroneStatus().postValue(EXECUTING_MISSION)
                    data.getMutableMission().postValue(null)
                    data.getMutableMissionPaused().postValue(false)
                    ToastHandler.showToastAsync(ctx, msg)
                })
    }

    override fun pauseMission(ctx: Context): Completable {
        val instance = getInstance()

        data.getMutableDroneStatus().postValue(SENDING_ORDER)
        return instance.mission.pauseMission().doOnComplete {
            data.getMutableMissionPaused().postValue(true)
            data.getMutableDroneStatus().postValue(PAUSED)
            ToastHandler.showToastAsync(ctx, R.string.drone_pause_success)
        }
    }

    override fun resumeMission(ctx: Context): Completable {
        val instance = getInstance()

        data.getMutableDroneStatus().postValue(SENDING_ORDER)
        return instance.mission.startMission().doOnComplete {
            data.getMutableMissionPaused().postValue(false)
            data.getMutableDroneStatus().postValue(EXECUTING_MISSION)
            ToastHandler.showToastAsync(ctx, R.string.drone_mission_success)
        }
    }

    private fun start(ctx: Context, instance: System, missionPlan: Mission.MissionPlan): Completable = 
            Completable.fromCallable { data.getMutableDroneStatus().postValue(SENDING_ORDER) }
                    .andThen(instance.mission.setReturnToLaunchAfterMission(false))
                    .andThen(instance.mission.uploadMission(missionPlan)
                            .andThen(instance.mission.startMission())
                            .doOnComplete {
                                data.getMutableDroneStatus().postValue(EXECUTING_MISSION)
                                data.getMutableMission().postValue(missionPlan.missionItems.dropLast(1))
                                data.getMutableMissionPaused().postValue(false)
                                ToastHandler.showToastAsync(ctx, R.string.drone_mission_success)
                            })

    private fun finish(ctx: Context, instance: System): Completable = 
            instance.mission.missionProgress
                            .filter{ it.current >= it.total - 1 }.firstOrError().toCompletable()
                            .doOnComplete{
                                ToastHandler.showToastAsync(ctx, R.string.drone_mission_return_launch)
                                data.getMutableDroneStatus().postValue(GOING_BACK)
                            }
                    .andThen(instance.mission.missionProgress)
                            .filter{ it.current == it.total }.firstOrError().toCompletable()
                            .doOnComplete{ data.getMutableDroneStatus().postValue(LANDING) }
                    .andThen(instance.action.land())
                    .andThen(instance.telemetry.inAir).filter { !it }.firstOrError().toCompletable()
                    .doOnComplete{
                        // Completion
                        data.getMutableDroneStatus().postValue(IDLE)
                        data.getMutableMission().postValue(null)
                        data.getMutableMissionPaused().postValue(true)
                    }


    private fun changeFromTo(flow: Flowable<Boolean>): Completable {
        var last = false
        return flow.distinctUntilChanged().filter { current ->
            if(!current && last)
                true
            else {
                last = current
                false
            }
        }.firstOrError().toCompletable()
    }

    private fun getInstance(): System = service.provideDrone()
            ?: throw IllegalStateException("Could not query drone instance")
}
