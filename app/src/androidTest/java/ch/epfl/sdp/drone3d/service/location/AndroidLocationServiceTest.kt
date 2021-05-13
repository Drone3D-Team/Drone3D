/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.location

import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.service.api.location.LocationPermissionService
import ch.epfl.sdp.drone3d.service.impl.location.AndroidLocationService
import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class AndroidLocationServiceTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val locationManager = mock(LocationManager::class.java)
    private val locationCriteria = mock(Criteria::class.java)
    private val locationPermission = mock(LocationPermissionService::class.java)

    private val androidLocationService =
        AndroidLocationService(
            locationManager,
            LOCATION_PROVIDER,
            locationCriteria,
            locationPermission
        )

    companion object {
        private const val LOCATION_PROVIDER = "provider"
        private val CUSTOM_LOCATION = Location(LOCATION_PROVIDER)
    }

    @Before
    fun before() {
        `when`(locationManager.getLastKnownLocation(anyString())).thenReturn(
            CUSTOM_LOCATION
        )
        setLocation(CUSTOM_LOCATION, 0.0, 0.0)
    }

    @Test
    fun isLocationEnabledReturnsFalseIfPermissionNotGranted() {
        `when`(locationPermission.isPermissionGranted()).thenReturn(false)
        assertFalse(androidLocationService.isLocationEnabled())
    }

    @Test
    fun isLocationEnabledReturnsTrueIfPermissionIsGranted() {
        `when`(locationPermission.isPermissionGranted()).thenReturn(true)
        assertTrue(androidLocationService.isLocationEnabled())
    }

    @Test
    fun getCurrentLocationIsNullIfPermissionNotGranted() {
        `when`(locationPermission.isPermissionGranted()).thenReturn(false)
        assertNull(androidLocationService.getCurrentLocation())
    }

    @Test
    fun getCurrentLocationReturnsCorrectLocationIfPermissionGranted() {
        `when`(locationPermission.isPermissionGranted()).thenReturn(true)

        val loc = androidLocationService.getCurrentLocation()
        assertNotNull(loc)
        assertLocationEquals(CUSTOM_LOCATION, loc!!)
    }

    @Test
    fun subscribeToLocationUpdatesIsNullIfPermissionNotGranted() {
        `when`(locationPermission.isPermissionGranted()).thenReturn(false)
        assertNull(androidLocationService.subscribeToLocationUpdates({}, 10, 10f))
    }

    @Test
    fun subscribeToLocationUpdatesIfLocationChanges() {
        `when`(locationPermission.isPermissionGranted()).thenReturn(true)
        var listener: LocationListener? = null
        `when`(
            locationManager.requestLocationUpdates(
                eq(LOCATION_PROVIDER), anyLong(), anyFloat(), any(
                    LocationListener::class.java
                )
            )
        ).thenAnswer {
            listener = it.getArgument(3)
            listener
        }
        val consumer: (LatLng) -> Unit = { latLng -> assertLocationEquals(CUSTOM_LOCATION, latLng) }
        val id: Int? = androidLocationService.subscribeToLocationUpdates(consumer, 10, 10f)
        assertNotNull(id)
        assertNotNull(listener)

        setLocation(CUSTOM_LOCATION, 10.0, 10.0)
        listener!!.onLocationChanged(CUSTOM_LOCATION)


        setLocation(CUSTOM_LOCATION, 50.0, 10.0)
        listener!!.onLocationChanged(CUSTOM_LOCATION)


        setLocation(CUSTOM_LOCATION, 10.0, 80.0)
        listener!!.onLocationChanged(CUSTOM_LOCATION)

        assertTrue(androidLocationService.unsubscribeFromLocationUpdates(id!!))
    }

    @Test
    fun unsubscribeFromLocationUpdatesWithoutValidIdDoesNotRaiseException() {
        assertFalse(androidLocationService.unsubscribeFromLocationUpdates(10))
    }

    private fun assertLocationEquals(expected: Location, actual: LatLng) {
        assertEquals(expected.longitude, actual.longitude, 0.01)
        assertEquals(expected.latitude, actual.latitude, 0.01)
    }

    private fun setLocation(location: Location, longitude: Double, latitude: Double) {
        location.longitude = longitude
        location.latitude = latitude
    }


}