package ch.epfl.sdp.drone3d.drone

import io.mavsdk.System

interface DroneProvider {

    /**
     * Provide the information of a drone or a simulation to the application
     * depending on the value of [isSimulation]
     */
    fun provideDrone(): System

    /**
     * Setup the [IP] and [port] of a simulation and connect it to the application
     */
    fun setSimIPAndPort(IP: String, port: String)

    fun getIP(): String

    fun getPort(): String

    fun isConnected() : Boolean

    fun isSimulation(): Boolean

    fun disconnect()
}