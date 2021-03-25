package ch.epfl.sdp.drone3d.drone
import io.mavsdk.System
import io.mavsdk.mavsdkserver.MavsdkServer

object DroneInstanceProvider {
    private var isConnected = false

    private var isSimulation = true
    private var simIP = "0.0.0.0"
    private var simPort = "0"

    var provide = {
        if (isSimulation) {
            // Works for armeabi-v7a and arm64-v8a (not x86 or x86_64)
            val mavsdkServer = MavsdkServer()
            val cloudSimIPAndPort = "tcp://$simIP:$simPort"
            val mavsdkServerPort = mavsdkServer.run(cloudSimIPAndPort)
            System("localhost", mavsdkServerPort)
        } else {}
    }

    fun setSimIPAndPort(IP: String, port: String) {
        isConnected = true
        isSimulation = true
        simIP = IP
        simPort = port
    }

    fun getIP(): String {
        return simIP
    }

    fun getPort(): String {
        return simPort
    }

    fun isConnected() : Boolean {
        return isConnected
    }

    fun isSimulation(): Boolean {
        return isSimulation
    }

    fun disconnect() {
        isConnected = false
    }
}