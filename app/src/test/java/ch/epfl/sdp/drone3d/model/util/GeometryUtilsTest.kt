/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.model.util

import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

class GeometryUtilsTest{

    @Test
    fun getFourthParallelogramPointOnSquareIsCorrect(){

        val origin = LatLng(0.0,0.0)
        val topAdjacent =  LatLng(1.0,0.0)
        val rightAdjacent =  LatLng(0.0,1.0)

        val expected= LatLng(1.0,1.0)
        val actual = GeometryUtils.getFourthParallelogramVertex(origin,topAdjacent,rightAdjacent)

        assertEquals(expected.latitude,actual.latitude,0.0001)
        assertEquals(expected.longitude,actual.longitude,0.0001)
    }

    @Test
    fun getFourthParallelogramPointOnBoundarySquareIsCorrect(){

        val origin =  LatLng(0.0,-179.0)
        val rightAdjacent = LatLng(0.0,179.0)
        val topAdjacent =  LatLng(1.0,-179.0)

        val expected = LatLng(1.0,179.0)
        val actual = GeometryUtils.getFourthParallelogramVertex(origin,topAdjacent,rightAdjacent)

        assertEquals(expected.longitude,actual.longitude,0.0001)
        assertEquals(expected.latitude,actual.latitude,0.0001)

    }
}
