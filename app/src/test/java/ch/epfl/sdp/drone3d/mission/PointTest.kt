package ch.epfl.sdp.drone3d.mission

import ch.epfl.sdp.drone3d.mission.Point.Companion.distance
import ch.epfl.sdp.drone3d.mission.Point.Companion.middle
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sqrt

class PointTest {

    @Test
    fun pointVectorAdditionReturnsExpected(){
        val expected1 = Point(1.0,2.0)
        val result1 = Point(0.5,1.0)+Vector(0.5,1.0)
        assertEquals(expected1,result1)
        val expected2 = Point(0.0,0.0)
        val result2 = Point(0.5,1.0)+Vector(-0.5,-1.0)
        assertEquals(expected2,result2)
    }

    @Test
    fun pointVectorSubtractionReturnsExpected(){
        val expected1 = Point(0.0,2.0)
        val result1 = Point(0.5,1.0)-Vector(0.5,-1.0)
        assertEquals(expected1,result1)
        val expected2 = Point(0.5,1.5)
        val result2 = Point(0.5,1.0)-Vector(0.0,-0.5)
        assertEquals(expected2,result2)
    }

    @Test
    fun pointDistanceReturnsExpected(){
        val expected1 = 4.0
        val result1 = distance(Point(0.0,0.0),Point(0.0,4.0))
        assertEquals(expected1,result1,0.00001)
        val expected2 = sqrt(2.0)
        val result2 = distance(Point(1.0,1.0),Point(2.0,2.0))
        assertEquals(expected2,result2,0.00001)
    }

    @Test
    fun pointDistanceIsSymmetric(){
        assertEquals(distance(Point(2.0,2.0),Point(1.0,1.0)),distance(Point(1.0,1.0),Point(2.0,2.0)),0.00001)
        assertEquals(distance(Point(-2.0,-2.0),Point(-1.0,-1.0)),distance(Point(1.0,1.0),Point(2.0,2.0)),0.00001)
    }

    @Test
    fun middlePointReturnsExpected(){
        val expected1 = Point(0.0,0.0)
        val result1 = middle(Point(-1.0,-2.0),Point(1.0,2.0))
        assertEquals(expected1,result1)

        val expected2 = Point(0.5,0.5)
        val result2 = middle(Point(0.5,-2.0),Point(0.5,3.0))
        assertEquals(expected2,result2)
    }

    @Test
    fun pointDirectionReturnsExpected(){
        val expected1 = Vector(sqrt(2.0)/2, sqrt(2.0)/2)
        val result1 = Point(0.0,0.0).direction(Point(1.0,1.0))
        assertEquals(expected1.x,result1.x,0.0001)
        assertEquals(expected1.y,result1.y,0.0001)

        val expected2 = Vector(1.0, 0.0)
        val result2 = Point(1.0,0.0).direction(Point(2.0,0.0))
        assertEquals(expected2.x,result2.x,0.0001)
        assertEquals(expected2.y,result2.y,0.0001)
    }
}