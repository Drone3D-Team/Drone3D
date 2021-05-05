/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.api.location

import android.app.Activity

interface LocationPermissionService {

    fun isPermissionGranted(): Boolean

    fun requestPermission(activity: Activity): Boolean

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    )

}