/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */
/*
 * This class was taken from the project Fly2Find and adapted for our project
 */

package ch.epfl.sdp.drone3d.service.impl.drone

import ch.epfl.sdp.drone3d.service.api.drone.DroneData
import ch.epfl.sdp.drone3d.service.api.drone.DroneExecutor
import ch.epfl.sdp.drone3d.service.api.drone.DroneServerFactory
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import io.mavsdk.System
import io.mavsdk.mavsdkserver.MavsdkServer

class DroneServiceImpl(
    private val droneFactory: DroneServerFactory,
    locationService: LocationService
) : DroneService {

    companion object {
        private const val DEFAULT_IP = "unknown"
        private const val DEFAULT_PORT = "-"
    }

    private val droneData = DroneDataImpl(this)
    private val droneExecutor = DroneExecutorImpl(this, droneData, locationService)

    @Volatile
    private var server: MavsdkServer? = null
    @Volatile
    private var droneInstance: System? = null

    @Volatile
    private var isSimulation = true
    @Volatile
    private var simIP = DEFAULT_IP
    @Volatile
    private var simPort = DEFAULT_PORT

    override fun provideDrone(): System? = droneInstance

    @Synchronized
    override fun setSimulation(IP: String, port: String) {

        disconnect()

        val instanceContainer = droneFactory.createSimulation(IP, port)

        if (instanceContainer != null) {
            droneInstance = instanceContainer.instance
            server = instanceContainer.server
            isSimulation = true
            simIP = IP
            simPort = port
            droneData.refresh()
        }
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

    override fun getData(): DroneData = droneData

    override fun getExecutor(): DroneExecutor = droneExecutor

    override fun reconnect() {
        if (isSimulation)
            setSimulation(simIP, simPort)
        else
            setDrone()
    }
}