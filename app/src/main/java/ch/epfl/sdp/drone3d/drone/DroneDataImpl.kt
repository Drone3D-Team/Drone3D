/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.drone

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.drone3d.service.storage.data.LatLong
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
class DroneDataImpl constructor(val provider: DroneService) : DroneData {

    // Drone instance
    private lateinit var instance: System

    private val disposables: MutableList<Disposable> = ArrayList()

    private val position: MutableLiveData<LatLong> = MutableLiveData()
    private val batteryLevel: MutableLiveData<Float> = MutableLiveData()
    private val absoluteAltitude: MutableLiveData<Float> = MutableLiveData()
    private val speed: MutableLiveData<Float> = MutableLiveData()
    private val homeLocation: MutableLiveData<Telemetry.Position> = MutableLiveData()
    private val isFlying: MutableLiveData<Boolean> = MutableLiveData(false)
    private val isConnected: MutableLiveData<Boolean> = MutableLiveData(false)
    private val isMissionPaused: MutableLiveData<Boolean> = MutableLiveData(true)
    private val cameraResolution: MutableLiveData<DroneData.CameraResolution> = MutableLiveData()
    private val videoStreamUri: MutableLiveData<String> = MutableLiveData()
    private val missionPlan: MutableLiveData<Mission.MissionPlan> = MutableLiveData()

    init {
        createDefaultSubs()
    }

    private fun createDefaultSubs() {
        instance = provider.provideDrone()

        addSubscription(instance.telemetry.flightMode, "Flight Mode") { flightMode ->
            if (flightMode == Telemetry.FlightMode.HOLD) isMissionPaused.postValue(true)
            if (flightMode == Telemetry.FlightMode.MISSION) isMissionPaused.postValue(false)
        }
        addSubscription(instance.telemetry.armed, "Armed") { armed ->
            if (!armed) isMissionPaused.postValue(true)
        }
        addSubscription(instance.telemetry.position, "Telemetry Position") { position ->
            val latLng = LatLong(position.latitudeDeg, position.longitudeDeg)
            this.position.postValue(latLng)
            //Absolute altitude is the altitude w.r. to the sea level
            absoluteAltitude.postValue(position.absoluteAltitudeM)
        }
        addSubscription(instance.telemetry.battery, "Battery") { battery ->
            batteryLevel.postValue(battery.remainingPercent)
        }
        addSubscription(instance.telemetry.positionVelocityNed, "GroundSpeedNed") { vector_speed ->
            speed.postValue(sqrt(
                vector_speed.velocity.eastMS.pow(2) + vector_speed.velocity.northMS.pow(2)))
        }
        addSubscription(instance.telemetry.inAir, "inAir") { isFlying ->
            this.isFlying.postValue(isFlying)
        }
        addSubscription(instance.telemetry.home, "home") { home -> homeLocation.postValue(home) }
        addSubscription(instance.core.connectionState, "connectionState") { state ->
            isConnected.postValue(state.isConnected)
        }
        addSubscription(instance.camera.information, "cameraResolution") { i ->
            cameraResolution.postValue(DroneData.CameraResolution(i.verticalResolutionPx, i.horizontalResolutionPx))
        }
        addSubscription(instance.camera.videoStreamInfo, "videoStreamUri") { i->
            videoStreamUri.postValue(i.settings.uri)
        }
    }

    private fun <T> addSubscription(flow: Flowable<T>, name: String, onNext: Consumer<in T>) {
        disposables.add(
            flow.distinctUntilChanged().subscribe(
                onNext,
                {error -> Timber.e(error,"Error $name : $error")}
            )
        )
    }

    private fun disposeOfAll() {
        disposables.forEach(Disposable::dispose)
        disposables.clear()
    }

    protected fun finalize() {
        disposeOfAll()
    }

    override fun getPosition(): LiveData<LatLong> = position

    override fun getBatteryLevel(): LiveData<Float> = batteryLevel

    override fun getAbsoluteAltitude(): LiveData<Float> = absoluteAltitude

    override fun getSpeed(): LiveData<Float> = speed

    override fun getHomeLocation(): LiveData<Telemetry.Position> = homeLocation

    override fun isFlying(): LiveData<Boolean> = isFlying

    override fun isConnected(): LiveData<Boolean> = isConnected

    override fun isMissionPaused(): LiveData<Boolean> = isMissionPaused

    override fun getCameraResolution(): LiveData<DroneData.CameraResolution> = cameraResolution

    override fun getVideoStreamUri(): LiveData<String> = videoStreamUri

    override fun getMissionPlan(): LiveData<Mission.MissionPlan> = missionPlan

    override fun refresh() {
        disposeOfAll()
        createDefaultSubs()
    }
}
