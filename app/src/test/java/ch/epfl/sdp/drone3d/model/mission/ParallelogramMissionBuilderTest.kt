/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.model.mission

import ch.epfl.sdp.drone3d.model.mission.ParallelogramMissionBuilder.Companion.buildDoublePassMappingMission
import ch.epfl.sdp.drone3d.model.mission.ParallelogramMissionBuilder.Companion.buildSinglePassMappingMission
import org.junit.Assert.assertEquals
import org.junit.Test

class ParallelogramMissionBuilderTest {
    @Test
    fun verifyUnitSquareSingleMappingMissionBuilderUpperCorner(){
        val startingPoint = Point(6.0,6.0)
        val area = Parallelogram(Point(0.0,0.0),Vector(1.0,0.0),Vector(0.0,1.0))
        val cameraAngle = 0.0
        val droneHeight = 1.0
        val groundImageDimension = GroundImageDim(6.0,6.0)

        val actualMappingMission = buildSinglePassMappingMission(startingPoint,area,cameraAngle,droneHeight,groundImageDimension)
        val expectedMappingMission = listOf(Point(1.0,1.0),Point(1.0,0.0),Point(0.0,0.0),Point(0.0,1.0))
        for(i in actualMappingMission.indices){
            assertEquals(actualMappingMission[i].x,expectedMappingMission[i].x,0.0001)
            assertEquals(actualMappingMission[i].y,expectedMappingMission[i].y,0.0001)
        }
    }
    @Test
    fun verifyUnitSquareSingleMappingMissionBuilderLowerCorner(){
        val startingPoint = Point(-4.0,-4.0)
        val area = Parallelogram(Point(0.0,0.0),Vector(1.0,0.0),Vector(0.0,1.0))
        val cameraAngle = 0.0
        val droneHeight = 1.0
        val groundImageDimension = GroundImageDim(6.0,6.0)

        val actualMappingMission = buildSinglePassMappingMission(startingPoint,area,cameraAngle,droneHeight,groundImageDimension)
        val expectedMappingMission = listOf(Point(0.0,0.0),Point(0.0,1.0),Point(1.0,1.0),Point(1.0,0.0))
        for(i in actualMappingMission.indices){
            assertEquals(actualMappingMission[i].x,expectedMappingMission[i].x,0.0001)
            assertEquals(actualMappingMission[i].y,expectedMappingMission[i].y,0.0001)
        }
    }
    @Test
    fun verifyDoubleMappingMissionBuilderUnitSquare(){
        val startingPoint = Point(0.0,0.0)
        val area = Parallelogram(Point(0.0,0.0),Vector(1.0,0.0),Vector(0.0,1.0))
        val cameraAngle = 0.0
        val droneHeight = 5.0
        val groundImageDimension = GroundImageDim(3.0,3.0)

        val actualMappingMission = buildDoublePassMappingMission(startingPoint,area,cameraAngle,droneHeight,groundImageDimension)
        val expectedMappingMission =
            listOf(
                Point(0.0,0.0),
                Point(0.0,0.6),
                Point(0.0,1.0),
                Point(0.9,1.0),
                Point(0.9,0.4),
                Point(0.9,0.0),
                Point(1.0,0.0),
                Point(1.0,0.6),
                Point(1.0,1.0),
                Point(1.0,1.0),
                Point(0.4,1.0),
                Point(0.0,1.0),
                Point(0.0,0.1),
                Point(0.6,0.1),
                Point(1.0,0.1),
                Point(1.0,0.0),
                Point(0.4,0.0),
                Point(0.0,0.0))
        for(i in actualMappingMission.indices){
            assertEquals(actualMappingMission[i].x,expectedMappingMission[i].x,0.0001)
            assertEquals(actualMappingMission[i].y,expectedMappingMission[i].y,0.0001)
        }
    }
}