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

    override fun startMission(context: Context, missionPlan: Mission.MissionPlan) {
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

    override fun returnToHomeLocationAndLand(context: Context) {
        val returnLocation = data.getHomeLocation().value
                ?: throw IllegalStateException(context.getString(R.string.drone_home_error))

        val completable = getConnectedInstance() ?: throw IllegalStateException("Could not query drone instance")
        val instance = service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        goToLocation(context, completable, instance, returnLocation)
    }

    override fun returnToUserLocationAndLand(context: Context) {
        // TODO Query user position
        val returnLocation = Telemetry.Position(.0, .0, 10f, 10f)

        val completable = getConnectedInstance() ?: throw IllegalStateException("Could not query drone instance")
        val instance = service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        goToLocation(context, completable, instance, returnLocation)
    }

    private fun distance(pos: Telemetry.Position, returnLocation: Telemetry.Position): Int {
        return LatLng(pos.latitudeDeg, pos.longitudeDeg)
                .distanceTo(LatLng(returnLocation.latitudeDeg, returnLocation.longitudeDeg))
                .roundToInt()
    }

    private fun goToLocation(context: Context, completable: Completable, instance: System, returnLocation: Telemetry.Position) {
        val future = completable
                .andThen(instance.mission.pauseMission())
                .andThen(instance.mission.clearMission())
                .andThen(instance.action.returnToLaunch())

        // Go to location
        data.addSubscription(
                future.subscribe(
                        {
                            data.getMutableMission().postValue(listOf(DroneUtils.generateMissionItem(returnLocation.latitudeDeg, returnLocation.longitudeDeg, returnLocation.absoluteAltitudeM)))
                            ToastHandler.showToastAsync(context, R.string.drone_home_success)
                        },
                        {
                            data.getMutableMission().postValue(null)
                            ToastHandler.showToastAsync(context, R.string.drone_home_error)
                        }
                )
        )

        // Land when arrived
        data.addSubscription(
                instance.telemetry.position.subscribe(
                        { pos ->
                            val isRightPos = distance(pos, returnLocation) == 0
                            val isStopped = data.getSpeed().value?.roundToInt() == 0
                            if (isRightPos.and(isStopped)) instance.action.land().blockingAwait(1, TimeUnit.SECONDS)
                            data.getMutableMissionPaused().postValue(true)
                        },
                        { e -> Timber.e("ERROR LANDING : $e") }
                )
        )
    }
}