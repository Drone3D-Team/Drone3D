/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.location

import android.annotation.SuppressLint
import android.location.Criteria
import android.location.LocationListener
import android.location.LocationManager
import ch.epfl.sdp.drone3d.service.api.location.LocationPermissionService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.module.LocationModule.LocationProvider
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Location service using android location functionality
 */
class AndroidLocationService @Inject constructor(
    private val locationManager: LocationManager,
    @LocationProvider val locationProvider: String?,
    private val locationCriteria: Criteria,
    private val permissionService: LocationPermissionService
) : LocationService {

    private var listenerId = 1
    private val listeners: MutableMap<Int, LocationListener> = HashMap()

    private fun getMyLocationProvider(): String? {
        return locationProvider ?: locationManager.getBestProvider(locationCriteria, true)
    }

    override fun isLocationEnabled(): Boolean {
        return permissionService.isPermissionGranted() && getMyLocationProvider() != null
    }

    // Permission is checked on isLocationEnabled()
    @SuppressLint("MissingPermission")
    override fun getCurrentLocation(): LatLng? {
        if (!isLocationEnabled()) {
            return null
        }

        val provider = getMyLocationProvider() ?: return null
        val loc = locationManager.getLastKnownLocation(provider) ?: return null
        return LatLng(loc.latitude, loc.longitude)
    }

    // Permission is checked on isLocationEnabled()
    @SuppressLint("MissingPermission")
    override fun subscribeToLocationUpdates(
        consumer: (LatLng) -> Unit,
        minTimeDelta: Long,
        minDistanceDelta: Float
    ): Int? {
        if (!isLocationEnabled()) {
            return null
        }
        var id: Int
        runBlocking {
            id = ++listenerId
        }
        val listener = LocationListener {
            val location = LatLng(it.latitude, it.longitude)
            consumer(location)
        }

        listeners[id] = listener
        val provider = getMyLocationProvider() ?: return null

        val loc = getCurrentLocation()
        if(loc != null){
            consumer(loc)
        }
        locationManager.requestLocationUpdates(
            provider,
            minTimeDelta,
            minDistanceDelta,
            listener
        )

        return id
    }

    override fun unsubscribeFromLocationUpdates(subscriptionId: Int): Boolean {
        return listeners.remove(subscriptionId)?.run {
            locationManager.removeUpdates(this)
            true
        } ?: false
    }
}
