/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.api.mission

import com.mapbox.mapboxsdk.geometry.LatLng

interface MappingMissionService {

    enum class Strategy {
        SINGLE_PASS, DOUBLE_PASS
    }
    /**
     * Returns the coordinates where the drone should take pictures on a single pass mapping mission of altitude [flightHeight] in meters.
     * A single pass mapping mission is sufficient when the area to map has low terrain features such as a landscape or a field.
     * For more vertical 3D mappings such a cities, see "buildDoublePassMappingMission"
     */
    fun buildSinglePassMappingMission(vertices:List<LatLng>,flightHeight:Double):List<LatLng>?

    /**
     * Returns the picture coordinates of the drone double pass mapping mission of altitude [flightHeight] in meters.
     * Use this function for high resolution vertical 3D mappings such a cities.
     */
    fun buildDoublePassMappingMission(vertices:List<LatLng>,flightHeight:Double):List<LatLng>?

    /**
     * Returns the angle of the drone's camera with respect to the horizontal axis in degree, where
     * a value of 0 corresponds to the camera looking forward and 90 to the camera looking down
     */
    fun getCameraPitch():Float
}