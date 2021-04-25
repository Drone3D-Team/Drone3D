/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.api.location

import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * This service provides the user location
 */
interface LocationService {

    /**
     * Returns true if the location service is enabled.
     * It is usually the case if the location permission is granted.
     */
    fun isLocationEnabled(): Boolean

    /**
     * Returns the current location of the user.
     * May be null if the location service is disabled or the location couldn't be retrieved.
     */
    fun getCurrentLocation(): LatLng?

    /**
     * Subscribe a [consumer] to location updates.
     * Every time the location changes from [minDistanceDelta] meters and
     * at least [minTimeDelta] milliseconds have elapsed from the last update,
     * [consumer] will be called with the latest location.
     * Returns an identifier of the subscription which must be used to unsubscribe.
     * Returns null if the location service is disabled.
     */
    fun subscribeToLocationUpdates(
        consumer: (LatLng) -> Unit,
        minTimeDelta: Long,
        minDistanceDelta: Long
    ): Int?

    /**
     * Unsubscribe a previous consumer with [subscriptionId] from location updates.
     * Returns true if something was unsubscribed.
     */
    fun unsubscribeFromLocationUpdates(subscriptionId: Int): Boolean

}