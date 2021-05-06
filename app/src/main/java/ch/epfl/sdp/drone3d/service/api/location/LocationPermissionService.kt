/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.api.location

import android.Manifest
import android.app.Activity

/**
 * This service manages the location permission
 */
interface LocationPermissionService {

    companion object {
        /**
         * The permission that this service manages
         */
        const val PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

        /**
         * The request code for the location permission
         */
        const val REQUEST_CODE = 0
    }

    /**
     * Returns true if the location permission is granted.
     */
    fun isPermissionGranted(): Boolean

    /**
     * Requests the location permission on the [activity].
     * Returns true if the permission was requested.
     */
    fun requestPermission(activity: Activity): Boolean

    /**
     * Updates this service with the request results.
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    )

}