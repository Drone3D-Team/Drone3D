package ch.epfl.sdp.drone3d

import org.junit.Assert.assertEquals
import org.junit.Test

class Drone3DTest {

    @Test
    fun singletonWorks() {
        val app = Drone3D()
        assertEquals(app, Drone3D.getInstance())
    }
}