package ch.epfl.sdp.drone3d.mission

import ch.epfl.sdp.drone3d.drone.DroneService
import kotlin.math.PI

class MappingMissionService constructor(val droneService: DroneService){

    val cameraAngle = PI/6//30degrees

    fun buildSinglePassMappingMission(startingPoint:Point,area:Parallelogram, flightHeight:Double):List<Point> {
        val groundImageDimension = computeGroundImageDimension(flightHeight)
        return ParallelogramMissionBuilder.buildSinglePassMappingMission(startingPoint,area,cameraAngle,flightHeight,groundImageDimension)
    }
    fun buildDoublePassMappingMission(startingPoint:Point,area:Parallelogram, flightHeight:Double):List<Point> {
        val groundImageDimension = computeGroundImageDimension(flightHeight)
        return ParallelogramMissionBuilder.buildDoublePassMappingMission(startingPoint,area,cameraAngle,flightHeight,groundImageDimension)
    }

    //Flight height in meters, must be height relative to the ground
    private fun computeGroundImageDimension(flightHeight:Double):GroundImageDim {
        val sensorWidth = 0.0 //millimeters
        val focalLength = 0.0// millimeters
        val imageWidth = 0.0 // pixels
        val imageHeight = 0.0 // pixels
        val GSD = (sensorWidth * flightHeight) / focalLength * imageWidth // Ground Samping distance in meters/pixel
        val groundImageWidth = (GSD * imageWidth) // meters
        val groundImageHeight = (GSD * imageHeight) // meters
        return GroundImageDim(groundImageWidth, groundImageHeight)
    }
}