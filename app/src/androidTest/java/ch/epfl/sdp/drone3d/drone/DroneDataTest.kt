package ch.epfl.sdp.drone3d.drone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.service.storage.data.LatLong
import io.mavsdk.telemetry.Telemetry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
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

    @Before
    fun before() {
        DroneInstanceMock.setupDefaultMocks()
    }

    @Test
    fun expectedData() {
        val droneData = DroneDataImpl(DroneInstanceMock.mockProvider)

        assertEquals(0f, droneData.getAbsoluteAltitude().value)
        assertEquals(10f, droneData.getBatteryLevel().value)
        assertEquals(DroneData.CameraResolution(2500, 2500), droneData.getCameraResolution().value)
        assertTrue(posEquals(Telemetry.Position(0.0, 0.0, 0.0f, 0.0f), droneData.getHomeLocation().value))
        assertEquals(true, droneData.isConnected().value)
        assertEquals(true, droneData.isFlying().value)
        assertEquals(false, droneData.isMissionPaused().value)
        assertEquals(LatLong(0.3, 0.0), droneData.getPosition().value)
        assertEquals(0f, droneData.getSpeed().value)
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