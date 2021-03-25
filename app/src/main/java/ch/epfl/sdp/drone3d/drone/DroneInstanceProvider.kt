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
            // Works for armeabi-v7a and arm64-v8a (not x86 or x86_64)
            val mavsdkServer = MavsdkServer()
            val cloudSimIPAndPort = "tcp://$simIP:$simPort"
            val mavsdkServerPort = mavsdkServer.run(cloudSimIPAndPort)
            System("localhost", mavsdkServerPort)
        } else {
            // TODO : write the program needed to connect a drone to the program
        }
    }

    /**
     * Setup the [IP] and [port] of a simulation and connect it to the application
     */
    fun setSimIPAndPort(IP: String, port: String) {
        isConnected = true
        isSimulation = true
        simIP = IP
        simPort = port
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