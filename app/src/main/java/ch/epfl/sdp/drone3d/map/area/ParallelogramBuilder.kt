/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.area

import ch.epfl.sdp.drone3d.model.util.GeometryUtils
import com.mapbox.mapboxsdk.geometry.LatLng

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
        val A = vertices[0]
        val O = vertices[1]
        val B = vertices[2]
        val C = GeometryUtils.getFourthParallelogramVertex(O, A, B)
        return listOf(A, O, B, C)
    }

    override fun getAreaSizeGivenComplete(): Double {
        val A = vertices[0]
        val O = vertices[1]
        val B = vertices[2]

        val distAO = A.distanceTo(O)
        val distOB = O.distanceTo(B)
        val diag = A.distanceTo(B)

        val s = (distAO + distOB + diag) / 2.0
        return 2 * Math.sqrt(s * (s - distAO) * (s - distOB) * (s - diag))
    }
}