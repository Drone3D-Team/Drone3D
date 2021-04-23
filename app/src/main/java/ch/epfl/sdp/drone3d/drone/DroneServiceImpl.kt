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

private const val DEFAULT_IP = "unknown"
private const val DEFAULT_PORT = "-"

class DroneServiceImpl(private val droneFactory: DroneServerFactory) : DroneService {

    private val droneData = DroneDataImpl(this)

    private var server: MavsdkServer? = null
    private var droneInstance: System? = null

    private var isSimulation = true
    private var simIP = DEFAULT_IP
    private var simPort = DEFAULT_PORT

    override fun provideDrone(): System? = droneInstance

    @Synchronized
    override fun setSimulation(IP: String, port: String) {

        disconnect()

        droneFactory.createSimulation(IP, port).also {
            server = it.server
            droneInstance = it.instance
        }

        isSimulation = true
        simIP = IP
        simPort = port

        droneData.refresh()
    }

    @Synchronized
    override fun setDrone() {

        disconnect()

        droneFactory.createDrone().also {
            server = it.server
            droneInstance = it.instance
        }

        simIP = DEFAULT_IP
        simPort = DEFAULT_PORT
        isSimulation = false

        droneData.refresh()
    }

    override fun getIP(): String {
        return simIP
    }

    override fun getPort(): String {
        return simPort
    }

    @Synchronized
    override fun isConnected(): Boolean {
        return droneData.isConnected().value ?: false
    }

    override fun isSimulation(): Boolean {
        return isSimulation
    }

    @Synchronized
    override fun disconnect() {
        server?.stop()
        droneInstance?.dispose()

        server = null
        droneInstance = null

        droneData.refresh()
    }

    override fun getData(): DroneData {
        return droneData
    }
}