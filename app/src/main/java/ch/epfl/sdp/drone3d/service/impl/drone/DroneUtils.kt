/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * This class was taken from the project Fly2Find and adapted for our project
 */

package ch.epfl.sdp.drone3d.service.impl.drone

import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.mission.Mission.MissionItem
import io.mavsdk.mission.Mission.MissionPlan

object DroneUtils {

    //Drone speed in meters per second
    const val DRONE_SPEED = 10f

    //Camera photo interval to use after this mission item (in seconds)
    const val CAMERA_PHOTO_INTERVAL = 1.0

    //Indicates if the drone takes the photo without stopping
    const val IS_FLY_THROUGH = false

    /**
     * Create a MissionPlan by using a [path] represented by a list of coordinates and the [altitude] wanted
     * for the drone
     */
    fun makeDroneMission(path: List<LatLng>, altitude: Float, cameraPitch: Float?): MissionPlan {
        return MissionPlan(path.map { point ->
            generateMissionItem(point.latitude, point.longitude, altitude, cameraPitch)
        })
    }

    /**
     * Create a MissionItem by using the coordinates [latitudeDeg] and [longitudeDeg], the [altitude] and the [cameraPitch]
     * wanted for the drone
     */
    fun generateMissionItem(
        latitudeDeg: Double,
        longitudeDeg: Double,
        altitude: Float,
        cameraPitch: Float? = null
    ): MissionItem {
        return MissionItem(
            latitudeDeg,
            longitudeDeg,
            altitude,
            DRONE_SPEED,
            IS_FLY_THROUGH, cameraPitch ?: Float.NaN, Float.NaN,
            if (cameraPitch != null) MissionItem.CameraAction.TAKE_PHOTO else MissionItem.CameraAction.NONE, Float.NaN,
            CAMERA_PHOTO_INTERVAL
        )
    }

}