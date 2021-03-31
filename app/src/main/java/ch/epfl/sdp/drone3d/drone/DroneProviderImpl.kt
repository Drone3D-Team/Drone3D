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
        if (isSimulation) {
            // Works for armeabi-v7a and arm64-v8a (not x86 or x86_64)
            val mavsdkServer = MavsdkServer()
            val cloudSimIPAndPort = "tcp://$simIP:$simPort"
            val mavsdkServerPort = mavsdkServer.run(cloudSimIPAndPort)
            return System("localhost", mavsdkServerPort)
        } else {
            // TODO : write the program needed to connect a drone to the program
            throw NotImplementedError()
        }
    }

    override fun setSimIPAndPort(IP: String, port: String) {
        isConnected = true
        isSimulation = true
        simIP = IP
        simPort = port
    }

    /**
     * Returns [simIP]
     */
    override fun getIP(): String {
        return simIP
    }

    /**
     * Returns [simPort]
     */
    override fun getPort(): String {
        return simPort
    }

    /**
     * Returns [isConnected]
     */
    override fun isConnected() : Boolean {
        return isConnected
    }

    /**
     * Returns [isSimulation]
     */
    override fun isSimulation(): Boolean {
        return isSimulation
    }

    /**
     * Disconnect a connected drone or simulation
     */
    override fun disconnect() {
        isConnected = false
    }
}