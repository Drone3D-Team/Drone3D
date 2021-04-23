/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * This class was taken from the project Fly2Find and adapted for our project
 */

package ch.epfl.sdp.drone3d.map.area

import com.mapbox.mapboxsdk.geometry.LatLng

// TODO To be deleted if no strategy is implemented for it
/**
 * Area represented by at least 3 vertices, with no upper bound
 */
class PolygonArea(override val vertices: List<LatLng>) : Area {
    init {
        require(vertices.size >= 3)
    }
}