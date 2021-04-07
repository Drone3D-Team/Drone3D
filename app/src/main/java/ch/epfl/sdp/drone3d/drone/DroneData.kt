/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.drone

import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.service.storage.data.LatLong
import io.mavsdk.telemetry.Telemetry

/**
 * This interface aims to link the connected Drone with the app by providing live data of its status
 */
interface DroneData {

    /**
     * A simple data class holding the values in pixel of camera's resolution
     */
    data class CameraResolution(val width: Int, val height: Int)

    /**
     * Returns a [LiveData] containing the current position of the drone
     */
    fun getPosition(): LiveData<LatLong>

    /**
     * Returns a [LiveData] containing the current battery level of the drone
     */
    fun getBatteryLevel(): LiveData<Float>

    /**
     * Returns a [LiveData] containing the current altitude from the sea level
     */
    fun getAbsoluteAltitude(): LiveData<Float>

    /**
     * Returns a [LiveData] containing the current speed of the drone
     */
    fun getSpeed(): LiveData<Float>

    /**
     * Returns a [LiveData] containing the current home position
     */
    fun getHomeLocation(): LiveData<Telemetry.Position>

    /**
     * Returns a [LiveData] containing the a [Boolean] that is true if the drone is currently flying
     */
    fun isFlying(): LiveData<Boolean>

    /**
     * Returns a [LiveData] containing the a [Boolean] that is true if the drone is connected
     */
    fun isConnected(): LiveData<Boolean>

    /**
     * Returns a [LiveData] containing the a [Boolean] that is true if
     * the current mission is paused
     */
    fun isMissionPaused(): LiveData<Boolean>

    /**
     * Returns a [LiveData] containing the resolution in pixel of the camera
     */
    fun getCameraResolution(): LiveData<CameraResolution>

    /**
     * Refresh the drone instance and its subscriptions
     *
     * As the LiveData are generated by subscribing to drone changes, some of them might get lost
     * and this would create memory leaks.
     *
     * To prevent this, this function remove reset all subscriptions to default thus releasing
     * outdated ones.
     *
     * This should only be used once we are sure that the discarded subscriptions
     * will not be used anymore.
     */
    fun refresh()
}