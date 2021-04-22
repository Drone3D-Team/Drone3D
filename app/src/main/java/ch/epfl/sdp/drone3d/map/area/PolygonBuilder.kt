/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * This class was taken from the project Fly2Find and adapted for our project
 */

package ch.epfl.sdp.drone3d.map.area

import com.mapbox.mapboxsdk.geometry.LatLng

class PolygonBuilder : AreaBuilder() {
    override val sizeLowerBound: Int? = 3
    override val sizeUpperBound: Int? = null
    override val shapeName: String = "Polygon"
    override fun buildGivenIsComplete(): PolygonArea = PolygonArea(vertices)
    override fun getShapeVerticesGivenComplete(): List<LatLng> = vertices
}