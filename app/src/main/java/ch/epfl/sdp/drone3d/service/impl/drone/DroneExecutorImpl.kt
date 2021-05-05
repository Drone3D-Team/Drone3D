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
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import timber.log.Timber
import java.util.concurrent.TimeUnit

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
        private const val ERROR_MARGIN: Double = 0.5
    }

    private fun getConnectedInstance(): Completable? {
        return service.provideDrone()?.core?.connectionState
            ?.filter { state -> state.isConnected }
            ?.firstOrError()
            ?.toCompletable()
    }

    override fun startMission(context: Context, missionPlan: Mission.MissionPlan): Completable {
        if (missionPlan.missionItems.isEmpty())
            throw IllegalArgumentException("Cannot start an empty mission")

        val instance =
                service.provideDrone()
                        ?: throw IllegalStateException("Could not query drone instance")

        // Allowed start states are Landed, arming, taking off. Starting a mission on other states
        // is dangerous and might break things
        return when(val startStatus = data.getDroneStatus().value) {
            IDLE, ARMING -> arm(context, instance, missionPlan)
            TAKING_OFF -> takeoff(context, instance, missionPlan)
            STARTING_MISSION -> start(context, instance, missionPlan)
            else -> throw IllegalStateException("Cannot start a new mission in state $startStatus")
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
                            data.getMutableMission().postValue(missionPlan.missionItems)
                            data.getMutableMissionPaused().postValue(false)
                            ToastHandler.showToastAsync(context, R.string.drone_mission_success)
                        })

        return finish(context, instance, startMission)
    }

    private fun finish(context: Context, instance: System, before: Completable): Completable {
        return before.andThen(instance.mission.missionProgress).distinctUntilChanged()
                .filter{ it.current == it.total }.firstOrError().toCompletable()
                    .doOnComplete{
                        ToastHandler.showToastAsync(context, "Mission done, returning to launch point")
                        data.getMutableDroneStatus().postValue(GOING_BACK)
                    }
                .andThen(instance.mission.isMissionFinished)
                .filter{ it }.toSingle().toCompletable()
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

    override fun pauseMission(context: Context) {
        val completable =
            getConnectedInstance() ?: throw IllegalStateException("Could not query drone instance")
        val instance =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        data.addSubscription(
            completable
                .andThen(instance.mission.pauseMission())
                .subscribe(
                    {
                        data.getMutableMissionPaused().postValue(true)
                        ToastHandler.showToastAsync(context, R.string.drone_pause_success)
                    },
                    {
                        ToastHandler.showToastAsync(context, R.string.drone_pause_error)
                    }
                )
        )
    }

    override fun resumeMission(context: Context) {
        val completable =
            getConnectedInstance() ?: throw IllegalStateException("Could not query drone instance")
        val instance =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        data.addSubscription(
            completable
                .andThen(instance.mission.startMission())
                .subscribe(
                    {
                        data.getMutableMissionPaused().postValue(false)
                        ToastHandler.showToastAsync(context, R.string.drone_mission_success)
                    },
                    {
                        ToastHandler.showToastAsync(context, R.string.drone_mission_error)
                    }
                )
        )
    }

    override fun returnToHomeLocationAndLand(context: Context): Completable {
        val returnLocation = data.getHomeLocation().value
                ?: throw IllegalStateException(context.getString(R.string.drone_return_error))

        return goToLocation(context, returnLocation)
    }

    override fun returnToUserLocationAndLand(context: Context): Completable {
        if (!locationService.isLocationEnabled()) {
            throw IllegalStateException("Location is not enabled")
        }
        val userPosition = locationService.getCurrentLocation()!!
        val returnLocation =
            Telemetry.Position(userPosition.latitude, userPosition.longitude, 0f, 0f)

        return goToLocation(context, returnLocation)
    }

    private fun distance(pos: Telemetry.Position, returnLocation: Telemetry.Position): Double {
        return LatLng(pos.latitudeDeg, pos.longitudeDeg)
            .distanceTo(LatLng(returnLocation.latitudeDeg, returnLocation.longitudeDeg))
    }

    private fun goToLocation(context: Context, returnLocation: Telemetry.Position): Completable {

        val instanceCompletable = getConnectedInstance() ?: throw IllegalStateException("Could not query drone instance")
        val droneInstance = service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        val future = instanceCompletable
                .andThen(droneInstance.mission.pauseMission())
                .andThen(droneInstance.mission.clearMission())
                .andThen(droneInstance.action.gotoLocation(
                    returnLocation.latitudeDeg,
                    returnLocation.longitudeDeg,
                    returnLocation.absoluteAltitudeM,
                    0f))

        // Go to location
        data.addSubscription(
                future.subscribe({
                    data.getMutableMission().postValue(listOf(DroneUtils.generateMissionItem(
                        returnLocation.latitudeDeg,
                        returnLocation.longitudeDeg,
                        returnLocation.absoluteAltitudeM)))
                    ToastHandler.showToastAsync(context, R.string.drone_return_success) },
                { data.getMutableMission().postValue(null)
                    ToastHandler.showToastAsync(context, R.string.drone_return_error) }))

        // Land when arrived
        data.addSubscription(
                droneInstance.telemetry.position.subscribe(
                        { pos ->
                            performLanding(droneInstance, pos, returnLocation)
                        },
                        { e -> Timber.e("ERROR LANDING : $e") }
                )
        )

        return future.andThen(droneInstance.mission.isMissionFinished).toCompletable()
    }

    private fun performLanding(
        instance: System,
        pos: Telemetry.Position,
        returnLocation: Telemetry.Position
    ) {
        val minSpeed = 0.2f
        val isRightPos = distance(pos, returnLocation) < ERROR_MARGIN
        val isStopped = data.getSpeed().value!! < minSpeed
        if (isRightPos.and(isStopped))
            instance.action.land().blockingAwait(1, TimeUnit.SECONDS)
        data.getMutableMissionPaused().postValue(true)
    }
}
