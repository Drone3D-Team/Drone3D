/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.drone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.service.impl.drone.DroneServerFactoryImpl
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DroneFactoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun createSimulationWithWrongDataFails() {
        val factory = DroneServerFactoryImpl()
        val instance = factory.createSimulation("1.1.1.1", "90")

        assertNull(instance?.server)
        assertNull(instance?.instance)

        instance?.server?.stop()
    }

    @Test
    fun createDroneWorks() {
        val factory = DroneServerFactoryImpl()
        val instance = factory.createDrone()

        assertThat(instance.server, notNullValue())
        assertThat(instance.instance, notNullValue())

        instance.server.stop()
    }
}