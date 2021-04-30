/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.drone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.service.api.drone.DroneServerFactory
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.impl.drone.DroneServiceImpl
import io.mavsdk.core.Core
import io.mavsdk.mavsdkserver.MavsdkServer
import io.reactivex.Flowable
import org.hamcrest.Matchers.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class DroneServiceTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun setSimulationAndDisconnectWorks() {
        val locationService = mock(LocationService::class.java)
        val factory = mock(DroneServerFactory::class.java)
        val server = mock(MavsdkServer::class.java)
        val drone = DroneInstanceMock.droneSystem

        DroneInstanceMock.setupDefaultMocks()

        // force isConnected() to be true during the simulation setup
        `when`(drone.core.connectionState).thenReturn(
            Flowable.fromArray(
                Core.ConnectionState(0L, true)
            )
        )

        `when`(factory.createSimulation(anyString(), anyString()))
            .thenReturn(DroneServerFactory.InstanceContainer(server, drone))

        val service = DroneServiceImpl(factory, locationService)

        val ip = "ip"
        val port = "port"

        service.setSimulation(ip, port)

        assertThat(service.getIP(), `is`(ip))
        assertThat(service.getPort(), `is`(port))
        assertThat(service.isSimulation(), `is`(true))

        verify(factory).createSimulation(ip, port)

        service.disconnect()

        assertThat(service.isConnected(), `is`(false))
    }

    @Test
    fun setDroneAndDisconnectWorks() {
        val locationService = mock(LocationService::class.java)
        val factory = mock(DroneServerFactory::class.java)
        val server = mock(MavsdkServer::class.java)
        val drone = DroneInstanceMock.droneSystem

        DroneInstanceMock.setupDefaultMocks()

        `when`(factory.createDrone())
            .thenReturn(DroneServerFactory.InstanceContainer(server, drone))

        val service = DroneServiceImpl(factory, locationService)

        service.setDrone()

        assertThat(service.isSimulation(), `is`(false))

        verify(factory).createDrone()

        service.disconnect()

        assertThat(service.isConnected(), `is`(false))
    }
}