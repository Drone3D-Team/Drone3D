/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.model.util

import ch.epfl.sdp.drone3d.model.mission.Parallelogram
import ch.epfl.sdp.drone3d.model.mission.SphereToPlaneProjector
import com.mapbox.mapboxsdk.geometry.LatLng

object GeometryUtils {

    /**
     * Returns the fourth point of a parallelogram opposite to the [origin]. This method is meant to be
     * used for small enough parallelogram on the surface of the Earth, see "SphereToPlaneProjector"
     * for more details
     */
    fun getFourthParallelogramVertex(origin: LatLng, adjacentLatLng1: LatLng, adjacentLatLng2: LatLng): LatLng {

        val projector = SphereToPlaneProjector(origin)
        val originPoint = projector.toPoint(origin)
        val adjacentPoint1 = projector.toPoint(adjacentLatLng1)
        val adjacentPoint2 = projector.toPoint(adjacentLatLng2)
        val fourthPoint = Parallelogram.getFourthPoint(originPoint, adjacentPoint1, adjacentPoint2)

        return projector.toLatLng(fourthPoint)
    }
}