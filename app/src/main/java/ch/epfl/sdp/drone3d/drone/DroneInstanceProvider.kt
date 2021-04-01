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

object DroneInstanceProvider {
    private var isConnected = false

    private var isSimulation = true
    private var simIP = "0.0.0.0"
    private var simPort = "0"

    /**
     * Provide the information of a drone or a simulation to the application depending on the value of [isSimulation]
     */
    var provide = {
        if (isSimulation) {
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

    /**
     * Setup the [IP] and [port] of a simulation and connect it to the application
     */
    fun setSimulation(IP: String, port: String) {
        isConnected = true
        isSimulation = true
        simIP = IP
        simPort = port
    }

    /**
     * Connect a drone to the application
     */
    fun setDrone() {
        isConnected = true
        isSimulation = false
    }

    /**
     * Returns [simIP]
     */
    fun getIP(): String {
        return simIP
    }

    /**
     * Returns [simPort]
     */
    fun getPort(): String {
        return simPort
    }

    /**
     * Returns [isConnected]
     */
    fun isConnected() : Boolean {
        return isConnected
    }

    /**
     * Returns [isSimulation]
     */
    fun isSimulation(): Boolean {
        return isSimulation
    }

    /**
     * Disconnect a connected drone or simulation
     */
    fun disconnect() {
        isConnected = false
    }
}