package ch.epfl.sdp.drone3d.mission

import ch.epfl.sdp.drone3d.mission.ParallelogramMissionBuilder.Companion.buildDoublePassMappingMission
import ch.epfl.sdp.drone3d.mission.ParallelogramMissionBuilder.Companion.buildSinglePassMappingMission
import org.junit.Test

class ParallelogramMissionBuilderTest {
//    @Test
//    fun printSingleMappingMissionBuilderSquare(){
//        val startingPoint = Point(6.0,6.0)
//        val area = Parallelogram(Point(0.0,0.0),Vector(1.0,0.0),Vector(0.0,1.0))
//        val cameraAngle = 0.0
//        val droneHeight = 5.0
//        val projectedImageWidth = 2.0
//        val projectedImageHeight = 2.0
//
//        val mappingMission = buildSinglePassMappingMission(startingPoint,area,cameraAngle,droneHeight,projectedImageWidth,projectedImageHeight)
//
//        print(mappingMission)
//    }
    @Test
    fun printDoubleMappingMissionBuilderSquare(){
        val startingPoint = Point(0.0,0.0)
        val area = Parallelogram(Point(0.0,0.0),Vector(1.0,0.0),Vector(0.0,1.0))
        val cameraAngle = 0.0
        val droneHeight = 5.0
        val projectedImageWidth = 2.0
        val projectedImageHeight = 2.0

        val mappingMission = buildDoublePassMappingMission(startingPoint,area,cameraAngle,droneHeight,projectedImageWidth,projectedImageHeight)

        print(mappingMission)
    }

//    @Test
//    fun printMappingMissionBuilderRhombus(){
//        val startingPoint = Point(6.0,6.0)
//        val area = Parallelogram(Point(3.0,-3.0),Vector(4.0,-1.0),Vector(1.0,-4.0))
//        val cameraAngle = 0.0
//        val droneHeight = 1.0
//        val projectedImageWidth = 2.0
//        val projectedImageHeight = 2.0
//
//        val mappingMission = buildSinglePassMappingMission(startingPoint,area,cameraAngle,droneHeight,projectedImageWidth,projectedImageHeight)
//
//        print(mappingMission)
//    }
}