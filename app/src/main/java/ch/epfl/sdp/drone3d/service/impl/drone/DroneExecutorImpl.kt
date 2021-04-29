/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.drone

import android.content.Context
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.api.drone.DroneDataEditable
import ch.epfl.sdp.drone3d.service.api.drone.DroneExecutor
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.ui.ToastHandler
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.System
import io.mavsdk.mission.Mission
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * This class is an implementation of [DroneExecutor]. As such it is responsible of the mission management of the drone.
 *
 * Parts of this code were taken from the fly2find project and adapted to our needs.
 */
class DroneExecutorImpl(private val service: DroneService,
                        private val data: DroneDataEditable): DroneExecutor {

    private fun getConnectedInstance(): Completable? {
        return service.provideDrone()?.core?.connectionState
                    ?.filter { state -> state.isConnected }
                    ?.firstOrError()
                    ?.toCompletable()
    }

    override fun startMission(context: Context, missionPlan: Mission.MissionPlan): Completable {
        val completable = getConnectedInstance() ?: throw IllegalStateException("Could not query drone instance")
        val instance = service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        val future = completable.andThen(instance.mission.setReturnToLaunchAfterMission(true))
                .andThen(instance.mission.uploadMission(missionPlan))
                .andThen(instance.action.arm())
                .andThen { it.onComplete() }
                .andThen(instance.mission.startMission())

        data.addSubscription(
                future.subscribe(
                        {
                            data.getMutableMission().postValue(missionPlan.missionItems)
                            data.getMutableMissionPaused().postValue(false)
                            ToastHandler.showToastAsync(context, R.string.drone_mission_success)
                        },
                        {
                            ToastHandler.showToastAsync(context, R.string.drone_mission_error)
                        }
                )
        )

        return returnToLaunch(context, future, instance)
    }

    private fun returnToLaunch(context: Context, future: Completable, instance: System): Completable {
        // Go to back to launch
        val returnLocation = data.getHomeLocation().value
            ?: throw IllegalStateException(context.getString(R.string.drone_return_error))

        data.addSubscription(future.subscribe({
            data.getMutableMission().postValue(listOf(DroneUtils.generateMissionItem(
                returnLocation.latitudeDeg,
                returnLocation.longitudeDeg,
                returnLocation.absoluteAltitudeM)))
            ToastHandler.showToastAsync(context, R.string.drone_return_success)
            }, { data.getMutableMission().postValue(null)
                ToastHandler.showToastAsync(context, R.string.drone_return_error) })
        )

        val hasArrived = instance.telemetry.position.filter { pos ->
            val isRightPos = distance(pos, returnLocation) == 0
            val isStopped = data.getSpeed().value?.roundToInt() == 0
            val isMissionFinished = data.getMutableMission().value?.isEmpty()!!
            isRightPos && isStopped && isMissionFinished
        }

        // Land when arrived
        data.addSubscription(hasArrived.subscribe({
            instance.action.land().blockingAwait(1, TimeUnit.SECONDS)
                    data.getMutableMissionPaused().postValue(true) },
                { e -> Timber.e("ERROR LANDING : $e") })
        )

        return Completable.fromFuture(hasArrived.toFuture())
    }

    override fun pauseMission(context: Context) {
        val completable = getConnectedInstance() ?: throw IllegalStateException("Could not query drone instance")
        val instance = service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

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
        val completable = getConnectedInstance() ?: throw IllegalStateException("Could not query drone instance")
        val instance = service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

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
        // TODO Query user position
        val returnLocation = Telemetry.Position(.0, .0, 10f, 10f)
        return  goToLocation(context, returnLocation)
    }

    private fun distance(pos: Telemetry.Position, returnLocation: Telemetry.Position): Int {
        return LatLng(pos.latitudeDeg, pos.longitudeDeg)
                .distanceTo(LatLng(returnLocation.latitudeDeg, returnLocation.longitudeDeg))
                .roundToInt()
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

        val hasArrived = droneInstance.telemetry.position.filter { pos ->
            val isRightPos = distance(pos, returnLocation) == 0
            val isStopped = data.getSpeed().value?.roundToInt() == 0
            isRightPos && isStopped }

        // Land when arrived
        data.addSubscription(hasArrived.subscribe({
                droneInstance.action.land().blockingAwait(1, TimeUnit.SECONDS)
                data.getMutableMissionPaused().postValue(true) },
            { e -> Timber.e("ERROR LANDING : $e") }))

        return Completable.fromFuture(hasArrived.toFuture())
    }
}