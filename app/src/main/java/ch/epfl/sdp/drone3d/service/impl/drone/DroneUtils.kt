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

    //TODO: replace takePhoto=true by takePhoto parameters provided by a mapping mission: this function
    // could take a mapping mission
    /**
     * Create a MissionPlan by using a [path] represented by a list of coordinates and the [altitude] wanted
     * for the drone
     */
    fun makeDroneMission(path: List<LatLng>, altitude: Float, cameraPitch:Float): MissionPlan {
        return MissionPlan(path.map { point ->
            generateMissionItem(point.latitude, point.longitude, altitude,cameraPitch,true)
        })
    }

    /**
     * Create a MissionItem by using the coordinates [latitudeDeg] and [longitudeDeg], the [altitude] and the [forwardCameraAngle] wanted
     * for the drone
     */
    fun generateMissionItem(latitudeDeg: Double, longitudeDeg: Double, altitude: Float,cameraPitch:Float,takePhoto:Boolean): MissionItem {
        return MissionItem(
            latitudeDeg,
            longitudeDeg,
            altitude,
            10f,
            true, cameraPitch, Float.NaN,
            if(takePhoto)MissionItem.CameraAction.TAKE_PHOTO
                    else MissionItem.CameraAction.NONE,
            Float.NaN, 1.0)
    }
}