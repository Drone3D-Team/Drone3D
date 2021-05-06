/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.location

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.api.location.LocationPermissionService
import ch.epfl.sdp.drone3d.service.api.location.LocationPermissionService.Companion.PERMISSION
import ch.epfl.sdp.drone3d.service.api.location.LocationPermissionService.Companion.REQUEST_CODE
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Location permission service using android permissions
 */
class AndroidLocationPermissionService @Inject constructor(@ApplicationContext private val context: Context) :
    LocationPermissionService {

    private var isPermissionDenied = false

    override fun isPermissionGranted(): Boolean {
        return context.checkSelfPermission(PERMISSION) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermission(activity: Activity): Boolean {
        if (isPermissionGranted() || isPermissionDenied) {
            return false
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, PERMISSION)) {
            val askPermissionAlert: AlertDialog.Builder = AlertDialog.Builder(activity)
            askPermissionAlert.setTitle(R.string.permission_location_enable)
                .setMessage(R.string.permission_location_rationale)
                .setPositiveButton(R.string.permission_accept_button) { _, _ ->
                    requestPermissions(activity, arrayOf(PERMISSION), REQUEST_CODE)
                }
                .setNegativeButton(R.string.permission_deny_button) { _, _ ->
                    isPermissionDenied = true
                }

            askPermissionAlert.show()
        } else {
            requestPermissions(activity, arrayOf(PERMISSION), REQUEST_CODE)
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE && permissions.size == 1 && permissions[0] == PERMISSION) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_DENIED -> isPermissionDenied = true
            }
        }
    }

}