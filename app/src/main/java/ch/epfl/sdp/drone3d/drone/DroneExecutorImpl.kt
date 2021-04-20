package ch.epfl.sdp.drone3d.drone

import ch.epfl.sdp.drone3d.Drone3D
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.ui.ToastHandler
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.System
import io.mavsdk.mission.Mission
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class DroneExecutorImpl(private val service: DroneService,
                        private val data: DroneDataImpl): DroneExecutor {

    /**
     * @return the connected instance as a Completable
     */
    private fun getConnectedInstance(): Completable? {
        return service.provideDrone()?.core?.connectionState
                    ?.filter { state -> state.isConnected }
                    ?.firstOrError()
                    ?.toCompletable()
    }

    override fun startMission(missionPlan: Mission.MissionPlan, groupId: String) {
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
                            ToastHandler.showToast(Drone3D.getInstance(), R.string.drone_mission_success)
                        },
                        {
                            ToastHandler.showToast(Drone3D.getInstance(), R.string.drone_mission_error)
                        }
                )
        )
    }

    override fun pauseMission() {
        val completable = getConnectedInstance() ?: throw IllegalStateException("Could not query drone instance")
        val instance = service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        data.addSubscription(
                completable
                        .andThen(instance.mission.pauseMission())
                        .subscribe(
                                {
                                    data.getMutableMissionPaused().postValue(true)
                                    ToastHandler.showToast(Drone3D.getInstance(), R.string.drone_pause_success)
                                },
                                {
                                    ToastHandler.showToast(Drone3D.getInstance(), R.string.drone_pause_error)
                                }
                        )
        )
    }

    override fun resumeMission() {
        val completable = getConnectedInstance() ?: throw IllegalStateException("Could not query drone instance")
        val instance = service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        data.addSubscription(
                completable
                        .andThen(instance.mission.startMission())
                        .subscribe(
                                {
                                    data.getMutableMissionPaused().postValue(false)
                                    ToastHandler.showToast(Drone3D.getInstance(), R.string.drone_mission_success)
                                },
                                {
                                    ToastHandler.showToast(Drone3D.getInstance(), R.string.drone_mission_error)
                                }
                        )
        )
    }

    override fun returnToHomeLocationAndLand() {
        val returnLocation = data.getHomeLocation().value
                ?: throw IllegalStateException(Drone3D.getInstance().getString(R.string.drone_home_error))

        val completable = getConnectedInstance() ?: throw IllegalStateException("Could not query drone instance")
        val instance = service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        goToLocation(completable, instance, returnLocation)
    }

    override fun returnToUserLocationAndLand() {
        // TODO Query user position
        val returnLocation = Telemetry.Position(.0, .0, 10f, 10f)

        val completable = getConnectedInstance() ?: throw IllegalStateException("Could not query drone instance")
        val instance = service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        goToLocation(completable, instance, returnLocation)

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

    private fun distance(pos: Telemetry.Position, returnLocation: Telemetry.Position): Int {
        return LatLng(pos.latitudeDeg, pos.longitudeDeg)
                .distanceTo(LatLng(returnLocation.latitudeDeg, returnLocation.longitudeDeg))
                .roundToInt()
    }

    private fun goToLocation(completable: Completable, instance: System, returnLocation: Telemetry.Position) {
        val future = completable
                .andThen(instance.mission.pauseMission())
                .andThen(instance.mission.clearMission())
                .andThen(instance.action.returnToLaunch())

        data.addSubscription(
                future.subscribe(
                        {
                            data.getMutableMission().postValue(listOf(DroneUtils.generateMissionItem(returnLocation.latitudeDeg, returnLocation.longitudeDeg, returnLocation.absoluteAltitudeM)))
                            ToastHandler.showToast(Drone3D.getInstance(), R.string.drone_home_success)
                        },
                        {
                            data.getMutableMission().postValue(null)
                            ToastHandler.showToast(Drone3D.getInstance(), R.string.drone_home_error)
                        }
                )
        )
    }
}