/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

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