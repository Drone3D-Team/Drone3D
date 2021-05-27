/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.area

import ch.epfl.sdp.drone3d.model.util.GeometryUtils
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlin.math.sqrt

/**
 * Builder to build a parallelogram. The parallelogram area is controlled by three vertices, the last one is automatically generated.
 */
class ParallelogramBuilder : AreaBuilder() {
    override val sizeLowerBound: Int = 3
    override val sizeUpperBound: Int = 3
    override val shapeName: String = "Parallelogram"

    override fun buildGivenIsComplete(): ParallelogramArea {
        return ParallelogramArea(getShapeVerticesGivenComplete())
    }

    override fun getShapeVerticesGivenComplete(): List<LatLng> {
        val a = vertices[0]
        val o = vertices[1]
        val b = vertices[2]
        val c = GeometryUtils.getFourthParallelogramVertex(o, a, b)
        return listOf(a, o, b, c)
    }

    override fun getAreaSizeGivenComplete(): Double {
        val a = vertices[0]
        val o = vertices[1]
        val b = vertices[2]

        val distAO = a.distanceTo(o)
        val distOB = o.distanceTo(b)
        val diag = a.distanceTo(b)

        val s = (distAO + distOB + diag) / 2.0
        return 2 * sqrt(s * (s - distAO) * (s - distOB) * (s - diag))
    }
}