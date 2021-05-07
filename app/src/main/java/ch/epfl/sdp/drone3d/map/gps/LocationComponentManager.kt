/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.gps

import android.annotation.SuppressLint
import android.app.Activity
import androidx.core.content.ContextCompat
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.map.MapboxUtility
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap

/**
 * The manager for showing the user location on a mapboxMap
 */
class LocationComponentManager {

    companion object {

        /**
         * Enables the location using a [locationService] on the [mapboxMap] in the [activity]
         */
        @SuppressLint("MissingPermission")
        fun enableLocationComponent(
            activity: Activity,
            mapboxMap: MapboxMap,
            locationService: LocationService
        ) {
            val style = mapboxMap.style
            // Check if permissions are enabled and if not request
            if (locationService.isLocationEnabled() && style != null) {

                // Create and customize the LocationComponent's options
                val customLocationComponentOptions = LocationComponentOptions.builder(activity)
                    .trackingGesturesManagement(true)
                    .accuracyColor(ContextCompat.getColor(activity, R.color.blue))
                    .build()

                val locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(activity, style)
                        .locationComponentOptions(customLocationComponentOptions)
                        .build()

                // Get an instance of the LocationComponent and then adjust its settings
                mapboxMap.locationComponent.apply {

                    // Activate the LocationComponent with options
                    activateLocationComponent(locationComponentActivationOptions)

                    // Enable to make the LocationComponent visible
                    isLocationComponentEnabled = true

                    // Set the LocationComponent's camera mode
                    cameraMode = CameraMode.TRACKING

                    // Set the LocationComponent's render mode
                    renderMode = RenderMode.COMPASS
                }

                if (locationService.isLocationEnabled() && locationService.getCurrentLocation() != null) {
                    MapboxUtility.zoomOnCoordinate(
                        locationService.getCurrentLocation()!!,
                        mapboxMap
                    )
                }
            }
        }

    }

}