/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.drone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.drone.impl.DroneServerFactoryImpl
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
    fun createSimulationWorks() {
        val factory = DroneServerFactoryImpl()
        val instance = factory.createSimulation("ip", "port")

        assertThat(instance.server, notNullValue())
        assertThat(instance.instance, notNullValue())

        instance.server.stop()
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