package ch.epfl.sdp.drone3d.drone

import io.mavsdk.System

interface DroneService {

    /**
     * Provide the information of a drone or a simulation to the application
     * depending on the value of [isSimulation]
     */
    fun provideDrone(): System

    /**
     * Setup the [IP] and [port] of a simulation and connect it to the application
     */
    fun setSimulation(IP: String, port: String)

    /**
     * Connect a drone to the application
     */
    fun setDrone()

    /**
     * Returns the drone connection ip
     */
    fun getIP(): String

    /**
     * Returns the drone connection port
     */
    fun getPort(): String

    /**
     * Returns true if the drone is currently connected
     */
    fun isConnected() : Boolean

    /**
     * Returns true if the drone is simulated
     */
    fun isSimulation(): Boolean

    /**
     * Disconnect the current drone
     */
    fun disconnect()

    /**
     * Returns the object holding [androidx.lifecycle.LiveData] of the information of the drone
     */
    fun getData(): DroneData
}