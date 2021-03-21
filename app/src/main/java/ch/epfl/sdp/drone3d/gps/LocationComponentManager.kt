package ch.epfl.sdp.drone3d.gps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ch.epfl.sdp.drone3d.R
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

class LocationComponentManager(private val activity: Activity, private val mapboxMap: MapboxMap) :
    PermissionsListener {

    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    @ColorRes
    private var accuracyColor = R.color.blue
    private var style: Style? = null

    fun setCustomAccuracyColor(@ColorRes color: Int) {
        accuracyColor = color
    }

    fun enableLocationComponent(loadedMapStyle: Style) {
        style = loadedMapStyle
        // Check if permissions are enabled and if not request
        if (checkAndRequestLocationPermission()) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(activity)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(activity, accuracyColor))
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
        }
    }

    private fun checkAndRequestLocationPermission(): Boolean {
        if (PermissionsManager.areLocationPermissionsGranted(activity)) {
            Log.d(null, "permission granted")
            return true
        }
        Log.d(null, "permission not granted")
        permissionsManager = PermissionsManager(this)
        permissionsManager.requestLocationPermissions(activity)
        return false
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(activity, R.string.user_location_permission_request, Toast.LENGTH_LONG)
            .show()
    }

    //TODO: Bug never called. Check if works in activity class
    override fun onPermissionResult(granted: Boolean) {
        Log.d(null, "results coming")
        if (granted) {
            Log.d(null, "permission becomes granted")
            enableLocationComponent(style!!)
        } else {
            Log.d(null, "permission becomes not granted")
            Toast.makeText(
                activity,
                R.string.user_location_permission_not_granted,
                Toast.LENGTH_LONG
            ).show()
        }
    }

}