/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * This class was taken from the project Fly2Find and adapted for our project
 */

package ch.epfl.sdp.drone3d.drone

import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.mission.Mission.MissionItem
import io.mavsdk.mission.Mission.MissionPlan

object DroneUtils {

    /**
     * Create a MissionPlan by using a [path] represented by a list of coordinates and the [altitude] wanted
     * for the drone
     */
    fun makeDroneMission(path: List<LatLng>, altitude: Float): MissionPlan {
        return MissionPlan(path.map { point ->
            generateMissionItem(point.latitude, point.longitude, altitude)
        })
    }

    /**
     * Create a MissionItem by using the coordinates [latitudeDeg] and [longitudeDeg], and the [altitude] wanted
     * for the drone
     */
    fun generateMissionItem(latitudeDeg: Double, longitudeDeg: Double, altitude: Float): MissionItem {
        return MissionItem(
            latitudeDeg,
            longitudeDeg,
            altitude,
            10f,
            true, Float.NaN, Float.NaN,
            MissionItem.CameraAction.TAKE_PHOTO, Float.NaN,
            1.0)
    }

}