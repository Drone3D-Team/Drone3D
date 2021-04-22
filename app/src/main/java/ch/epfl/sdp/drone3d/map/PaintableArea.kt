/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * This interface was taken from the project Fly2Find and adapted for our project
 */

package ch.epfl.sdp.drone3d.map

import com.mapbox.mapboxsdk.geometry.LatLng

interface PaintableArea {
    fun getControlVertices(): List<LatLng>
    fun getShapeVertices(): List<LatLng>?
}