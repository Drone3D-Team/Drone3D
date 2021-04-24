/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.api.drone

import io.mavsdk.System
import io.mavsdk.mavsdkserver.MavsdkServer

interface DroneServerFactory {

    data class InstanceContainer(val server: MavsdkServer, val instance: System)

    fun createSimulation(simIP: String, simPort: String): InstanceContainer

    fun createDrone(): InstanceContainer
}