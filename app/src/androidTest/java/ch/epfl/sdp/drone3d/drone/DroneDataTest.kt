package ch.epfl.sdp.drone3d.drone

import org.junit.Test

/**
 * Test the DroneData class functionality
 */
class DroneDataTest {

    @Test
    fun testInit() {
        val data = DroneData()
        data.dumpOutdatedSubs()
    }
}