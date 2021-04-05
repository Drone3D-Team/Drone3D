/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.drone

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.drone3d.service.storage.data.LatLong
import io.mavsdk.System
import io.mavsdk.core.Core
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.*

/**
 * This class regroup all the data from the drone. This is inspired a lot by the Drone class from Fly2Find project.
 *
 * With minor adjustments such as:
 *  - Remove unused properties of the drone
 *  - Add functionality used by our app
 *  - Convert certain types to our owns
 */
class DroneData @Inject constructor(provider: DroneProvider) {

    data class CameraResolution(val width: Int, val height: Int)

    // Drone instance
    private val instance: System = provider.provideDrone()

    private val disposables: MutableList<Disposable> = ArrayList()

    val position: MutableLiveData<LatLong> = MutableLiveData()
    val batteryLevel: MutableLiveData<Float> = MutableLiveData()
    val absoluteAltitude: MutableLiveData<Float> = MutableLiveData()
    val speed: MutableLiveData<Float> = MutableLiveData()
    val homeLocation: MutableLiveData<Telemetry.Position> = MutableLiveData()
    val isFlying: MutableLiveData<Boolean> = MutableLiveData(false)
    val isConnected: MutableLiveData<Boolean> = MutableLiveData(false)
    val isMissionPaused: MutableLiveData<Boolean> = MutableLiveData(true)
    val cameraResolution: MutableLiveData<CameraResolution> = MutableLiveData()

    init {
        createDefaultSubs()
    }

    private fun createDefaultSubs() {
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
            cameraResolution.postValue(CameraResolution(i.verticalResolutionPx, i.horizontalResolutionPx))
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

    /**
     * Dump every outdated subscriptions
     *
     * Dispose of all subscriptions, clear the list and recreate the default ones
     */
    fun disposeOutdatedSubs() {
        disposeOfAll()
        createDefaultSubs()
    }

    protected fun finalize() {
        disposeOfAll()
    }

    /**
     * Returns the connected instance as a Completable
     */
    private fun getConnectedInstance(): Completable {
        return instance.core.connectionState
                .filter(Core.ConnectionState::getIsConnected)
                .firstOrError()
                .toCompletable()
    }
}