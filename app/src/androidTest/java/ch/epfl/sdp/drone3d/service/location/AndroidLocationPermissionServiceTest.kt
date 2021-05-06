/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.location

import android.content.Context
import android.content.pm.PackageManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.service.api.location.LocationPermissionService.Companion.PERMISSION
import ch.epfl.sdp.drone3d.service.api.location.LocationPermissionService.Companion.REQUEST_CODE
import ch.epfl.sdp.drone3d.service.impl.location.AndroidLocationPermissionService
import ch.epfl.sdp.drone3d.ui.TempTestActivity
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock


@RunWith(AndroidJUnit4::class)
class AndroidLocationPermissionServiceTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context = mock(Context::class.java)
    private val permissionService = AndroidLocationPermissionService(context)

    @Test
    fun isPermissionGrantedIsTrueIfContextGrantsPermission() {
        `when`(context.checkSelfPermission(PERMISSION))
            .thenReturn(
                PackageManager.PERMISSION_GRANTED
            )
        Assert.assertTrue(permissionService.isPermissionGranted())
    }

    @Test
    fun isPermissionGrantedIsFalseIfContextDoesNotGrantPermission() {
        `when`(context.checkSelfPermission(PERMISSION))
            .thenReturn(
                PackageManager.PERMISSION_DENIED
            )
        Assert.assertFalse(permissionService.isPermissionGranted())
    }

    @Test
    fun noPermissionIsAskedIfPermissionIsGranted() {
        `when`(context.checkSelfPermission(PERMISSION))
            .thenReturn(
                PackageManager.PERMISSION_GRANTED
            )

        // Launch an activity and request permission
        ActivityScenario.launch(TempTestActivity::class.java).onActivity { activity ->
            Assert.assertFalse(permissionService.requestPermission(activity))
        }
    }

    @Test
    fun noPermissionIsAskedTwice() {
        `when`(context.checkSelfPermission(PERMISSION))
            .thenReturn(
                PackageManager.PERMISSION_DENIED
            )

        // Simulate the first request for permission by getting the permission results
        permissionService.onRequestPermissionsResult(
            REQUEST_CODE,
            arrayOf(PERMISSION),
            intArrayOf(PackageManager.PERMISSION_DENIED)
        )

        // Launch an activity and request permission
        ActivityScenario.launch(TempTestActivity::class.java).onActivity { activity ->
            Assert.assertFalse(permissionService.requestPermission(activity))
        }
    }

    @Test
    fun requestPermissionWorksOnActivity() {
        `when`(context.checkSelfPermission(PERMISSION))
            .thenReturn(
                PackageManager.PERMISSION_DENIED
            )

        // Launch an activity and request permission
        ActivityScenario.launch(TempTestActivity::class.java).onActivity { activity ->
            Assert.assertTrue(permissionService.requestPermission(activity))
        }
    }


}