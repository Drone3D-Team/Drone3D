/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.mission

import android.util.Log
import ch.epfl.sdp.drone3d.model.mission.*
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.mission.MappingMissionService
import com.mapbox.mapboxsdk.geometry.LatLng
import java.lang.IllegalArgumentException
import javax.inject.Inject

class ParallelogramMappingMissionService @Inject constructor(val droneService: DroneService): MappingMissionService {

    companion object{
        private val cameraAngle = 0.0 // Suppose the drone is looking down
        //Those default data correspond to the Freefly Astro Quadrotor properties
        private val defaultSensorWidth = 7.82f
        private val defaultFocalLength = 24.0f
        private val defaultImageWidth = 1280
        private val defaultImageHeight = 720
    }
   

    override fun buildSinglePassMappingMission(vertices:List<LatLng>,flightHeight:Double): List<LatLng> {

        return buildMappingMission(
            vertices,
            flightHeight,
            ParallelogramMissionBuilder.Companion::buildSinglePassMappingMission
        )
    }

    override fun buildDoublePassMappingMission(vertices:List<LatLng>,flightHeight:Double): List<LatLng> {
        return buildMappingMission(
            vertices,
            flightHeight,
            ParallelogramMissionBuilder.Companion::buildDoublePassMappingMission
        )
    }

    private fun buildMappingMission(
        vertices: List<LatLng>,
        flightHeight: Double,
        mappingFunction: (
                startingPoint: Point, area: Parallelogram, cameraAngle: Double,
                flightHeight: Double, groundImageDimension: GroundImageDim
        ) -> List<Point>
    ): List<LatLng> {

        if(vertices.size!=3){
            throw IllegalArgumentException("A parallelogram should be determined by 3 vertices")
        }

        val groundImageDimension = computeGroundImageDimension(flightHeight)

            val projector = SphereToPlaneProjector(vertices[0])
            val parallelogram = Parallelogram(projector.toPoint(vertices[1]), projector.toPoint(vertices[0]),
                projector.toPoint(vertices[2]))
            return projector.toLatLngs(mappingFunction(projector.toPoint(vertices[0]),
                parallelogram, cameraAngle, flightHeight, groundImageDimension))
    }

    /**
     * Returns the dimension of the projected image to the ground using [flightHeight] in meters
     * and the camera properties provided by the drone
     * If a property is not provided by the drone, returns null
     */
    fun computeGroundImageDimension(flightHeight: Double): GroundImageDim {

        val sensorWidth = droneService.getData().getSensorSize().value?.horizontalSize?:defaultSensorWidth //millimeters
        val focalLength = droneService.getData().getFocalLength().value?:defaultFocalLength // millimeters
        val imageWidth = droneService.getData().getCameraResolution().value?.width?:defaultImageWidth // pixels
        val imageHeight = droneService.getData().getCameraResolution().value?.height?:defaultImageHeight // pixels

        // Ground Sampling Distance in meters/pixel
        val GSD = (sensorWidth * flightHeight) / (focalLength * imageWidth)
        val groundImageWidth = GSD * imageWidth // meters
        val groundImageHeight = GSD * imageHeight // meters
        return GroundImageDim(groundImageWidth, groundImageHeight)
    }
}