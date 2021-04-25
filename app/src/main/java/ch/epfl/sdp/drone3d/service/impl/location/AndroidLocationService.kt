/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationListener
import android.location.LocationManager
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.module.LocationModule.LocationProvider
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Location service using android location functionalities
 */
@Singleton
class AndroidLocationService @Inject constructor(
    private val locationManager: LocationManager,
    @LocationProvider val locationProvider: String?,
    private val locationCriteria: Criteria,
    @ApplicationContext private val context: Context
) : LocationService {

    private var listenerId = 1
    private val listeners: MutableMap<Int, LocationListener> = HashMap()

    private fun getMyLocationProvider(): String? {
        return locationProvider ?: locationManager.getBestProvider(locationCriteria, true)
    }

    override fun isLocationEnabled(): Boolean {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && getMyLocationProvider() != null
    }

    override fun getCurrentLocation(): LatLng? {
        if (!isLocationEnabled()) {
            return null
        }
        return try {
            val provider = getMyLocationProvider() ?: return null
            val loc =
                locationManager.getLastKnownLocation(provider)
                    ?: return null
            LatLng(loc.latitude, loc.longitude)
        } catch (ex: SecurityException) {
            // We need to explicitly catch this exception, even though we throw it again immediately
            throw ex
        }
    }

    override fun subscribeToLocationUpdates(
        consumer: (LatLng) -> Unit,
        minTimeDelta: Long,
        minDistanceDelta: Long
    ): Int? {
        if (!isLocationEnabled()) {
            return null
        }
        val id = ++listenerId
        val listener = LocationListener {
            val location = LatLng(it.latitude, it.longitude)
            consumer(location)
        }

        listeners[id] = listener
        val provider = getMyLocationProvider() ?: return null
        try {
            consumer(getCurrentLocation()!!)
            locationManager.requestLocationUpdates(
                provider,
                minTimeDelta,
                minDistanceDelta.toFloat(),
                listener
            )
        } catch (ex: SecurityException) {
            // We need to explicitly catch this exception, even though we throw it again immediately
            throw ex
        }

        return id
    }

    override fun unsubscribeFromLocationUpdates(subscriptionId: Int) {
        val listener: LocationListener? = listeners.remove(subscriptionId)
        if (listener != null) {
            locationManager.removeUpdates(listener)
        }
    }

}