/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.drone

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.drone3d.service.storage.data.LatLong
import io.mavsdk.System
import io.mavsdk.core.Core
import io.mavsdk.mission.Mission
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
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

    // Drone instance
    private val instance: System = provider.provideDrone()

    private val disposables: MutableList<Disposable> = ArrayList()

    val position: MutableLiveData<LatLong> = MutableLiveData()
    val batteryLevel: MutableLiveData<Float> = MutableLiveData()
    val absoluteAltitude: MutableLiveData<Float> = MutableLiveData()
    val speed: MutableLiveData<Float> = MutableLiveData()
    val mission: MutableLiveData<List<Mission.MissionItem>> = MutableLiveData()
    val homeLocation: MutableLiveData<Telemetry.Position> = MutableLiveData()
    val isFlying: MutableLiveData<Boolean> = MutableLiveData(false)
    val isConnected: MutableLiveData<Boolean> = MutableLiveData(false)
    val isMissionPaused: MutableLiveData<Boolean> = MutableLiveData(true)

    init {
        createDefaultSubs()
    }

    private fun createDefaultSubs() {
        disposables.add(instance.telemetry.flightMode.distinctUntilChanged()
                .subscribe(
                        { flightMode ->
                            if (flightMode == Telemetry.FlightMode.HOLD) isMissionPaused.postValue(true)
                            if (flightMode == Telemetry.FlightMode.MISSION) isMissionPaused.postValue(false)
                        },
                        { error -> Timber.e("Error Flight Mode: $error") }
                )
        )
        disposables.add(instance.telemetry.armed.distinctUntilChanged()
                .subscribe(
                        { armed -> if (!armed) isMissionPaused.postValue(true) },
                        { error -> Timber.e("Error Armed : $error") }
                )
        )
        disposables.add(instance.telemetry.position.distinctUntilChanged()
                .subscribe(
                        { position ->
                            val latLng = LatLong(position.latitudeDeg, position.longitudeDeg)
                            this.position.postValue(latLng)
                            //Absolute altitude is the altitude w.r. to the sea level
                            absoluteAltitude.postValue(position.absoluteAltitudeM)
                            //Relative altitude is the altitude w.r. to the take off level
                        },
                        { error -> Timber.e("Error Telemetry Position : $error") }
                )
        )
        disposables.add(instance.telemetry.battery.distinctUntilChanged()
                .subscribe(
                        { battery -> batteryLevel.postValue(battery.remainingPercent) },
                        { error -> Timber.e("Error Battery : $error") }
                )
        )
        disposables.add(instance.telemetry.positionVelocityNed.distinctUntilChanged()
                .subscribe(
                        { vector_speed -> speed.postValue(sqrt(vector_speed.velocity.eastMS.pow(2) + vector_speed.velocity.northMS.pow(2))) },
                        { error -> Timber.e("Error GroundSpeedNed : $error") }
                )
        )
        disposables.add(instance.telemetry.inAir.distinctUntilChanged()
                .subscribe(
                        { isFlying -> this.isFlying.postValue(isFlying) },
                        { error -> Timber.e("Error inAir : $error") }
                )
        )
        disposables.add(instance.telemetry.home.distinctUntilChanged()
                .subscribe(
                        { home -> homeLocation.postValue(home) },
                        { error -> Timber.e("Error home : $error") }
                )
        )
        disposables.add(instance.core.connectionState.distinctUntilChanged()
                .subscribe(
                        { state -> isConnected.postValue(state.isConnected) },
                        { error -> Timber.e("Error connectionState : $error") }
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
    fun dumpOutdatedSubs() {
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