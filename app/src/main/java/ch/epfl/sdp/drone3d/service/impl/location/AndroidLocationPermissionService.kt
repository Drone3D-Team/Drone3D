/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.location

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.api.location.LocationPermissionService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidLocationPermissionService @Inject constructor(@ApplicationContext private val context: Context) :
    LocationPermissionService {

    private val permission = Manifest.permission.ACCESS_FINE_LOCATION
    private val permissionRequestCode = 0
    private var isPermissionGranted =
        ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    private var isPermissionDenied = false

    override fun isPermissionGranted(): Boolean {
        return isPermissionGranted
    }

    override fun requestPermission(activity: Activity) {
        if (!isPermissionGranted() && !isPermissionDenied) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                val askPermissionAlert: AlertDialog.Builder = AlertDialog.Builder(activity)
                askPermissionAlert.setTitle(R.string.permission_location_enable)
                    .setMessage(R.string.permission_location_rationale)
                    .setPositiveButton(R.string.permission_accept_button) { _, _ ->
                        requestPermissions(activity, arrayOf(permission), permissionRequestCode)
                    }
                    .setNegativeButton(R.string.permission_deny_button) { _, _ ->
                        isPermissionDenied = true
                    }

                askPermissionAlert.show()
            } else {
                requestPermissions(activity, arrayOf(permission), permissionRequestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionRequestCode && permissions.size == 1 && permissions[0] == permission) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> isPermissionGranted = true
                PackageManager.PERMISSION_DENIED -> isPermissionDenied = true
            }
        }
    }

}