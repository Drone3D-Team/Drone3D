/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.area

import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * Area with 4 vertices representing a Parallelogram
 */
class ParallelogramArea(override val vertices: List<LatLng>) : Area {
}