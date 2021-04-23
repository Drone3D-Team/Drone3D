/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * This interface was taken from the project Fly2Find and adapted for our project
 */

package ch.epfl.sdp.drone3d.map.area

import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * Represent an area delimited by [vertices]
 */
interface Area {
    val vertices: List<LatLng>
}
