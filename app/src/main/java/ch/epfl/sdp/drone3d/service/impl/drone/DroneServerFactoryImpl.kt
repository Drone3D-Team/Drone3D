/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.drone

import ch.epfl.sdp.drone3d.service.api.drone.DroneServerFactory
import io.mavsdk.System
import io.mavsdk.mavsdkserver.MavsdkServer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * This factory provide the create and launch the server that connects to the drone whether it is a simulation or not
 */
class DroneServerFactoryImpl : DroneServerFactory {

    override fun createSimulation(simIP: String, simPort: String): DroneServerFactory.InstanceContainer? {
        val mavsdkServer = MavsdkServer()
        val cloudSimIPAndPort = "tcp://${simIP}:${simPort}"
        var mavsdkServerPort = 0

        //Tries to setup the server for 3 seconds
        GlobalScope.launch {
            launch {
                mavsdkServerPort = mavsdkServer.run(cloudSimIPAndPort)
            }
        }
        Thread.sleep(3000)

        return if (mavsdkServerPort != 0) {
            DroneServerFactory.InstanceContainer(mavsdkServer, System("localhost", mavsdkServerPort))
        } else {
            null
        }
    }

    override fun createDrone(): DroneServerFactory.InstanceContainer {
        val mavsdkServer = MavsdkServer()
        val mavsdkServerPort = mavsdkServer.run()
        return DroneServerFactory.InstanceContainer(mavsdkServer, System("localhost", mavsdkServerPort))
    }
}