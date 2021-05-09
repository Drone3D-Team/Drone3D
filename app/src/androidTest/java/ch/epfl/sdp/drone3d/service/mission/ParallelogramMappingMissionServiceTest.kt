/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.mission

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.service.api.drone.DroneData
import ch.epfl.sdp.drone3d.service.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.model.mission.*
import ch.epfl.sdp.drone3d.service.impl.mission.ParallelogramMappingMissionService
import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class ParallelogramMappingMissionServiceTest {

    companion object {
        val droneService = DroneInstanceMock.mockService()
        val cameraResolution = MutableLiveData(DroneData.CameraResolution(200, 200))
        val focalLength = MutableLiveData(4f)
        val sensorSize = MutableLiveData(DroneData.SensorSize(2f, 2f))

        val mappingMissionService = ParallelogramMappingMissionService(droneService)
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun before() {
        `when`(droneService.getData().getCameraResolution()).thenReturn(cameraResolution)
        `when`(droneService.getData().getSensorSize()).thenReturn(sensorSize)
        `when`(droneService.getData().getFocalLength()).thenReturn(focalLength)
    }

    @Test
    fun computeGroundImageDimensionReturnsExpected() {

        val flightHeight = 10.0
        val groundImageDimension = mappingMissionService.computeGroundImageDimension(flightHeight)!!

        val expected = GroundImageDim(5.0, 5.0)
        assertEquals(expected, groundImageDimension)
    }


    @Test
    fun buildSinglePassMappingMissionReturnsExpected() {
        val projector = SphereToPlaneProjector(LatLng(0.0,0.0))
        val vertices = listOf(Point(0.0, 0.0), Point(1.0, 0.0), Point(0.0, 1.0))
        val latLngVertices = projector.toLatLngs(vertices)
        val area = Parallelogram(vertices[1], vertices[0], vertices[2])
        val cameraAngle = mappingMissionService.cameraPitch
        val flightHeight = 100.0
        val groundImageDim = mappingMissionService.computeGroundImageDimension(flightHeight)!!

        val expected = projector.toLatLngs(ParallelogramMissionBuilder.buildSinglePassMappingMission(
            vertices[0],
            area,
            cameraAngle,
            flightHeight,
            groundImageDim
        ).map { pair-> pair.first })
        val obtained = mappingMissionService.buildSinglePassMappingMission(latLngVertices, flightHeight)!!
        for (i in expected.indices){
            assertEquals(expected[i].latitude,obtained[i].latitude,0.0001)
            assertEquals(expected[i].longitude,obtained[i].longitude,0.0001)
            assertEquals(expected[i].altitude,obtained[i].altitude,0.0001)
        }
    }

    @Test
    fun buildDoublePassMappingMissionReturnsExpected() {
        val projector = SphereToPlaneProjector(LatLng(0.0, 0.0))
        val vertices = listOf(Point(0.0, 0.0), Point(1.0, 0.0), Point(0.0, 1.0))
        val latLngVertices = projector.toLatLngs(vertices)
        val area = Parallelogram(vertices[1], vertices[0], vertices[2])
        val cameraAngle =mappingMissionService.cameraPitch
        val flightHeight = 100.0
        val groundImageDim = mappingMissionService.computeGroundImageDimension(flightHeight)!!

        val expected = projector.toLatLngs(
            ParallelogramMissionBuilder.buildDoublePassMappingMission(
                vertices[0],
                area,
                cameraAngle,
                flightHeight,
                groundImageDim
            ).map { pair-> pair.first }
        )
        val obtained =
            mappingMissionService.buildDoublePassMappingMission(latLngVertices, flightHeight)!!
        for (i in expected.indices) {
            assertEquals(expected[i].latitude, obtained[i].latitude, 0.0001)
            assertEquals(expected[i].longitude, obtained[i].longitude, 0.0001)
            assertEquals(expected[i].altitude, obtained[i].altitude, 0.0001)
        }
    }
}