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

object DroneServiceImpl : DroneService {

    private val droneData = DroneDataImpl(this)

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
        isSimulation = true
        simIP = IP
        simPort = port

        droneData.refresh()
    }

    override fun setDrone() {
        isSimulation = false

        droneData.refresh()
    }

    override fun getIP(): String {
        return simIP
    }

    override fun getPort(): String {
        return simPort
    }

    override fun isConnected() : Boolean {
        return droneData.isConnected().value ?: false
    }

    override fun isSimulation(): Boolean {
        return isSimulation
    }

    override fun disconnect() {
        droneData.disconnect()
    }

    override fun getData(): DroneData {
        return droneData
    }
}