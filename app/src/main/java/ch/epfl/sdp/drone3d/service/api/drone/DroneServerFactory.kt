/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.api.drone

import io.mavsdk.System
import io.mavsdk.mavsdkserver.MavsdkServer

/**
 * An interface allowing the application to connect to a drone
 */
interface DroneServerFactory {

    /**
     * A class containing the [server] and the [instance] of a connected drone
     */
    data class InstanceContainer(val server: MavsdkServer, val instance: System)

    /**
     * Connect a simulation of a drone using [simIP] and [simPort] to the application
     */
    fun createSimulation(simIP: String, simPort: String): InstanceContainer?

    /**
     * Connect a drone to the application
     */
    fun createDrone(): InstanceContainer
}