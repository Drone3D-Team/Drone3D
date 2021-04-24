/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * This interface was taken from the project Fly2Find and adapted for our project
 */

package ch.epfl.sdp.drone3d.map

import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * A drawableArea has two types of vertex:
 * - control vertices used to control the shape and dimension of the area
 * - shape vertices used to draw the border of the area
 */
interface DrawableArea {

    fun getControlVertices(): List<LatLng>

    fun getShapeVertices(): List<LatLng>?
}