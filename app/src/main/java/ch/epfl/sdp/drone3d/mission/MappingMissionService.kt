/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.mission

import ch.epfl.sdp.drone3d.drone.DroneService

class MappingMissionService constructor(val droneService: DroneService) {

    private val cameraAngle = 0.0 // Suppose the drone is looking down

    fun buildSinglePassMappingMission(startingPoint: Point, area: Parallelogram, flightHeight: Double): List<Point>? {
        val groundImageDimension = computeGroundImageDimension(flightHeight)
        return if (groundImageDimension != null) {
            ParallelogramMissionBuilder.buildSinglePassMappingMission(startingPoint, area, cameraAngle,
                flightHeight,
                groundImageDimension
            )
        } else {
            null
        }
    }

    fun buildDoublePassMappingMission(startingPoint: Point, area: Parallelogram, flightHeight: Double): List<Point>? {
        val groundImageDimension = computeGroundImageDimension(flightHeight)
        return if (groundImageDimension != null) {
            ParallelogramMissionBuilder.buildDoublePassMappingMission(startingPoint, area, cameraAngle, flightHeight, groundImageDimension)
        } else {
            null
        }
    }

    /**
     * Returns the dimension of the projected image to the ground using [flightHeight] in meters
     * and the camera properties provided by the drone
     * If a property is not provided by the drone, returns null
     */

    fun computeGroundImageDimension(flightHeight: Double): GroundImageDim? {

        val sensorWidth = droneService.getData().getSensorSize().value?.horizontalSize //millimeters
        val focalLength = droneService.getData().getFocalLength().value // millimeters
        val imageWidth = droneService.getData().getCameraResolution().value?.width // pixels
        val imageHeight = droneService.getData().getCameraResolution().value?.height // pixels
        return if (sensorWidth != null && focalLength != null && imageWidth != null && imageHeight != null) {
            // Ground Sampling Distance in meters/pixel
            val GSD = (sensorWidth * flightHeight) / (focalLength * imageWidth)
            val groundImageWidth = GSD * imageWidth // meters
            val groundImageHeight = GSD * imageHeight // meters
            GroundImageDim(groundImageWidth, groundImageHeight)
        } else {
            null
        }
    }
}