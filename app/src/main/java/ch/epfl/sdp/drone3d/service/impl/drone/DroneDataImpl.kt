/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.drone

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.drone3d.service.api.drone.DroneData
import ch.epfl.sdp.drone3d.service.api.drone.DroneDataEditable
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.System
import io.mavsdk.mission.Mission
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import timber.log.Timber
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * This class regroup all the data from the drone. This is inspired a lot by the Drone class from Fly2Find project.
 *
 * With minor adjustments such as:
 *  - Remove unused properties of the drone
 *  - Add functionality used by our app
 *  - Convert certain types to our owns
 *  - Change from object to api/implementation
 */
class DroneDataImpl constructor(val provider: DroneService) : DroneDataEditable {

    private val disposables: MutableList<Disposable> = ArrayList()

    private val position: MutableLiveData<LatLng> = MutableLiveData()
    private val batteryLevel: MutableLiveData<Float> = MutableLiveData()
    private val absoluteAltitude: MutableLiveData<Float> = MutableLiveData()
    private val relativeAltitude: MutableLiveData<Float> = MutableLiveData()
    private val speed: MutableLiveData<Float> = MutableLiveData()
    private val homeLocation: MutableLiveData<Telemetry.Position> = MutableLiveData()
    private val isFlying: MutableLiveData<Boolean> = MutableLiveData(false)
    private val isConnected: MutableLiveData<Boolean> = MutableLiveData(false)
    private val isMissionPaused: MutableLiveData<Boolean> = MutableLiveData(true)
    private val cameraResolution: MutableLiveData<DroneData.CameraResolution> = MutableLiveData()
    private val videoStreamUri: MutableLiveData<String> = MutableLiveData()
    private val mission: MutableLiveData<List<Mission.MissionItem>> = MutableLiveData()
    private val missionProgress: MutableLiveData<Float> = MutableLiveData()
    private val focalLength: MutableLiveData<Float> = MutableLiveData()
    private val sensorSize: MutableLiveData<DroneData.SensorSize> = MutableLiveData()
    private val droneStatus: MutableLiveData<DroneData.DroneStatus> = MutableLiveData(DroneData.DroneStatus.ARMING)

    init {
        createDefaultSubs()
    }

    /**
     * Setup the observers for the data of the drone we keep track of
     */
    private fun createDefaultSubs() {

        val droneInstance = provider.provideDrone()

        if (droneInstance == null) {
            // Reset
            position.postValue(null)
            batteryLevel.postValue(null)
            absoluteAltitude.postValue(null)
            speed.postValue(null)
            homeLocation.postValue(null)
            isFlying.postValue(false)
            isConnected.postValue(false)
            mission.postValue(null)
            missionProgress.postValue(null)
            isMissionPaused.postValue(true)
            cameraResolution.postValue(null)
            droneStatus.postValue(DroneData.DroneStatus.IDLE)
        } else {
            addFlightModeSubscriptions(droneInstance)
            addArmedSubscriptions(droneInstance)
            addPositionSubscriptions(droneInstance)
            addBatteryLevelSubscriptions(droneInstance)
            addSpeedSubscriptions(droneInstance)
            addIsFlyingSubscriptions(droneInstance)
            addHomeSubscriptions(droneInstance)
            addMissionProgressSubscriptions(droneInstance)
            addIsConnectedSubscriptions(droneInstance)
            addCameraSubscriptions(droneInstance)
            addVideoStreamSubscriptions(droneInstance)
        }
    }

    private fun addFlightModeSubscriptions(droneInstance: System) {
        addSubscription(droneInstance.telemetry.flightMode, "Flight Mode") { flightMode ->
            if (flightMode == Telemetry.FlightMode.HOLD) isMissionPaused.postValue(true)
            if (flightMode == Telemetry.FlightMode.MISSION) isMissionPaused.postValue(false)
        }
    }

    private fun addArmedSubscriptions(droneInstance: System) {
        addSubscription(droneInstance.telemetry.armed, "Armed") { armed ->
            if (!armed) isMissionPaused.postValue(true)
        }
    }

    private fun addPositionSubscriptions(droneInstance: System) {
        addSubscription(droneInstance.telemetry.position, "Telemetry Position") { position ->
            val latLng = LatLng(position.latitudeDeg, position.longitudeDeg)
            this.position.postValue(latLng)
            //Absolute altitude is the altitude w.r. to the sea level
            absoluteAltitude.postValue(position.absoluteAltitudeM)
            relativeAltitude.postValue(position.relativeAltitudeM)
        }
    }

    private fun addBatteryLevelSubscriptions(droneInstance: System) {
        addSubscription(droneInstance.telemetry.battery, "Battery") { battery ->
            batteryLevel.postValue(battery.remainingPercent)
        }
    }

    private fun addSpeedSubscriptions(droneInstance: System) {
        addSubscription(
            droneInstance.telemetry.positionVelocityNed,
            "GroundSpeedNed"
        ) { vector_speed ->
            speed.postValue(
                sqrt(
                    vector_speed.velocity.eastMS.pow(2) + vector_speed.velocity.northMS.pow(2)
                )
            )
        }
    }

    private fun addIsFlyingSubscriptions(droneInstance: System) {
        addSubscription(droneInstance.telemetry.inAir, "inAir") { isFlying ->
            this.isFlying.postValue(isFlying)
        }
    }

    private fun addHomeSubscriptions(droneInstance: System) {
        addSubscription(droneInstance.telemetry.home, "home") { home ->
            homeLocation.postValue(home)
        }
    }

    private fun addIsConnectedSubscriptions(droneInstance: System) {
        addSubscription(droneInstance.core.connectionState, "connectionState") { state ->
            isConnected.postValue(state.isConnected)
        }
    }

    private fun addMissionProgressSubscriptions(droneInstance: System) {
        addSubscription(droneInstance.mission.missionProgress, "progress") { progress ->
            this.missionProgress.postValue(progress.current.toFloat() / progress.total)
        }
    }

    private fun addCameraSubscriptions(droneInstance: System) {
        addSubscription(droneInstance.camera.information, "cameraResolution") { i ->
            cameraResolution.postValue(
                DroneData.CameraResolution(
                    i.horizontalResolutionPx,
                    i.verticalResolutionPx
                )
            )
            focalLength.postValue(i.focalLengthMm)
            sensorSize.postValue(
                DroneData.SensorSize(
                    i.horizontalSensorSizeMm,
                    i.verticalSensorSizeMm
                )
            )
        }
    }
    private fun addVideoStreamSubscriptions(droneInstance: System) {
        addSubscription(droneInstance.camera.videoStreamInfo, "videoStreamUri") { i ->
            videoStreamUri.postValue(i.settings.uri)
        }
    }

    @Synchronized
    private fun disposeOfAll() {
        disposables.forEach(Disposable::dispose)
        disposables.clear()
    }

    override fun getPosition(): LiveData<LatLng> = position

    override fun getBatteryLevel(): LiveData<Float> = batteryLevel

    override fun getAbsoluteAltitude(): LiveData<Float> = absoluteAltitude

    override fun getRelativeAltitude(): LiveData<Float> = relativeAltitude

    override fun getSpeed(): LiveData<Float> = speed

    override fun getHomeLocation(): LiveData<Telemetry.Position> = homeLocation

    override fun isFlying(): LiveData<Boolean> = isFlying

    override fun isConnected(): LiveData<Boolean> = isConnected

    override fun getCameraResolution(): LiveData<DroneData.CameraResolution> = cameraResolution

    override fun getVideoStreamUri(): LiveData<String> = videoStreamUri

    override fun getMissionProgress(): LiveData<Float> = missionProgress

    override fun getFocalLength(): LiveData<Float> = focalLength

    override fun getSensorSize(): LiveData<DroneData.SensorSize> = sensorSize

    override fun refresh() {
        disposeOfAll()
        createDefaultSubs()
    }

    @Synchronized
    override fun purge() {
        disposables.removeAll { it.isDisposed }
    }

    override fun getMutableMission(): MutableLiveData<List<Mission.MissionItem>> = mission

    override fun getMutableMissionPaused(): MutableLiveData<Boolean> = isMissionPaused

    override fun getMutableDroneStatus(): MutableLiveData<DroneData.DroneStatus> = droneStatus

    private fun <T> addSubscription(flow: Flowable<T>, name: String, onNext: Consumer<in T>) {
        disposables.add(
                flow.distinctUntilChanged().subscribe(
                    onNext,
                    { error -> Timber.e(error,"Error $name : $error") }
                )
        )
    }

    protected fun finalize() {
        disposeOfAll()
    }
}
