/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.drone

import ch.epfl.sdp.drone3d.service.api.drone.DroneServerFactory
import io.mavsdk.System
import io.mavsdk.mavsdkserver.MavsdkServer

class DroneServerFactoryImpl: DroneServerFactory {

    override fun createSimulation(simIP: String, simPort: String): DroneServerFactory.InstanceContainer {
        val mavsdkServer = MavsdkServer()
        val cloudSimIPAndPort = "tcp://${simIP}:${simPort}"
        val mavsdkServerPort = mavsdkServer.run(cloudSimIPAndPort)
        return DroneServerFactory.InstanceContainer(mavsdkServer, System("localhost", mavsdkServerPort))
    }

    override fun createDrone(): DroneServerFactory.InstanceContainer {
        val mavsdkServer = MavsdkServer()
        val mavsdkServerPort = mavsdkServer.run()
        return DroneServerFactory.InstanceContainer(mavsdkServer, System("localhost", mavsdkServerPort))
    }
}