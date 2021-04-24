/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.api.mission

import com.mapbox.mapboxsdk.geometry.LatLng

interface MappingMissionService {

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
}