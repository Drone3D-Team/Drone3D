/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.area

import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * Builder to build a parallelogram. The parallelogram area is controlled by three vertices, the last one is automatically generated.
 */
class ParallelogramBuilder: AreaBuilder() {
    override val sizeLowerBound: Int? = 3
    override val sizeUpperBound: Int? = 3
    override val shapeName: String = "Parallelogram"

    override fun buildGivenIsComplete(): ParallelogramArea {
        // Generate the last corner based on the previous ones and create a ParallelogramArea
        TODO("Not yet implemented")
    }

    override fun getShapeVerticesGivenComplete(): List<LatLng> {
        TODO("Not yet implemented")
    }
}