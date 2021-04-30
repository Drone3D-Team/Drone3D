/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.gps

import android.app.Activity
import android.widget.Toast
import androidx.core.content.ContextCompat
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.map.MapboxUtility
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

/**
 * The manager for map phone location
 */
class LocationComponentManager(private val activity: Activity, val mapboxMap: MapboxMap, val locationService: LocationService) :
    PermissionsListener {

    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    private var style: Style? = null

    /**
     * Enables the location on the map given the loadedMapStyle
     */
    fun enableLocationComponent(loadedMapStyle: Style) {
        style = loadedMapStyle
        // Check if permissions are enabled and if not request
        if (checkAndRequestLocationPermission()) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(activity)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(activity, R.color.blue))
                .build()

            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(activity, style!!)
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

            if (locationService.isLocationEnabled()) {
                if(locationService.getCurrentLocation() != null){
                    MapboxUtility.zoomOnCoordinate(locationService.getCurrentLocation()!!, mapboxMap)
                }

            }
        }
    }

    /**
     * Sends the permissions results to the permission manager
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkAndRequestLocationPermission(): Boolean {
        if (PermissionsManager.areLocationPermissionsGranted(activity)) {
            return true
        }
        permissionsManager = PermissionsManager(this)
        permissionsManager.requestLocationPermissions(activity)
        return false
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(activity, R.string.user_location_permission_request, Toast.LENGTH_LONG)
            .show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(style!!)
        } else {
            Toast.makeText(
                activity,
                R.string.user_location_permission_not_granted,
                Toast.LENGTH_LONG
            ).show()
        }
    }

}