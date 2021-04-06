/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */
/*
 * This class was taken from the project Fly2Find and adapted for our project
 */

package ch.epfl.sdp.drone3d.drone

import io.mavsdk.System
import io.mavsdk.mavsdkserver.MavsdkServer

object DroneProviderImpl : DroneProvider {

    private var isConnected = false

    private var isSimulation = true
    private var simIP = "0.0.0.0"
    private var simPort = "0"

    override fun provideDrone(): System {
        return if (isSimulation) {
            val mavsdkServer = MavsdkServer()
            val cloudSimIPAndPort = "tcp://$simIP:$simPort"
            val mavsdkServerPort = mavsdkServer.run(cloudSimIPAndPort)
            System("localhost", mavsdkServerPort)
        } else {
            val mavsdkServer = MavsdkServer()
            val mavsdkServerPort = mavsdkServer.run()
            System("localhost", mavsdkServerPort)
        }
    }

    override fun setSimulation(IP: String, port: String) {
        isConnected = true
        isSimulation = true
        simIP = IP
        simPort = port
    }

    override fun setDrone() {
        isConnected = true
        isSimulation = false
    }

    override fun getIP(): String {
        return simIP
    }

    override fun getPort(): String {
        return simPort
    }

    override fun isConnected() : Boolean {
        return isConnected
    }

    override fun isSimulation(): Boolean {
        return isSimulation
    }

    override fun disconnect() {
        isConnected = false
    }
}