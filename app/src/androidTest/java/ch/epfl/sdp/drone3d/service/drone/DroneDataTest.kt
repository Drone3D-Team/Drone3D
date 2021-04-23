/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.drone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.service.api.drone.DroneData
import ch.epfl.sdp.drone3d.service.impl.drone.DroneDataImpl
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.telemetry.Telemetry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Test the DroneData class functionality
 */
@RunWith(AndroidJUnit4::class)
class DroneDataTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun expectedData() {
        DroneInstanceMock.setupDefaultMocks()

        val droneData: DroneData = DroneDataImpl(DroneInstanceMock.mockService())

        assertEquals(0f, droneData.getAbsoluteAltitude().value)
        assertEquals(10f, droneData.getBatteryLevel().value)
        assertEquals(DroneData.CameraResolution(2500, 2000), droneData.getCameraResolution().value)
        assertEquals(45f, droneData.getFocalLength().value)
        assertEquals(DroneData.SensorSize(15f, 10f), droneData.getSensorSize().value)
        assertTrue(posEquals(Telemetry.Position(0.0, 0.0, 0.0f, 0.0f), droneData.getHomeLocation().value))
        assertEquals(true, droneData.isConnected().value)
        assertEquals(true, droneData.isFlying().value)
        assertEquals(false, droneData.isMissionPaused().value)
        assertEquals(LatLng(0.3, 0.0), droneData.getPosition().value)
        assertEquals(0f, droneData.getSpeed().value)
        assertEquals("uri", droneData.getVideoStreamUri().value)
    }

    private fun posEquals(expected: Telemetry.Position, value: Telemetry.Position?): Boolean {
        if (value == null)
            return false

        return expected.latitudeDeg == value.latitudeDeg &&
                expected.longitudeDeg == value.longitudeDeg &&
                expected.absoluteAltitudeM == value.absoluteAltitudeM &&
                expected.relativeAltitudeM == value.relativeAltitudeM
    }
}