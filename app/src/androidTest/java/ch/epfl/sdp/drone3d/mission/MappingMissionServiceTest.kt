/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.mission

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.drone.api.DroneData
import ch.epfl.sdp.drone3d.drone.DroneInstanceMock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class MappingMissionServiceTest {

    companion object {
        val droneService = DroneInstanceMock.mockService()
        val cameraResolution = MutableLiveData(DroneData.CameraResolution(200, 200))
        val focalLength = MutableLiveData(4f)
        val sensorSize = MutableLiveData(DroneData.SensorSize(2f, 2f))


        val mappingMissionService = MappingMissionService(droneService)

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
        val startingPoint = Point(0.0, 0.0)
        val area = Parallelogram(Point(0.0, 0.0), Point(1.0, 0.0), Point(0.0, 1.0))
        val cameraAngle = 0.0
        val flightHeight = 100.0
        val groundImageDim = mappingMissionService.computeGroundImageDimension(flightHeight)!!
        val expected = ParallelogramMissionBuilder.buildSinglePassMappingMission(
            startingPoint,
            area,
            cameraAngle,
            flightHeight,
            groundImageDim
        )
        val obtained =
            mappingMissionService.buildSinglePassMappingMission(startingPoint, area, flightHeight)

        assertEquals(expected, obtained)
    }

    @Test
    fun buildDoublePassMappingMissionReturnsExpected() {
        val startingPoint = Point(1.0, 1.0)
        val area = Parallelogram(Point(0.0, 0.0), Point(1.0, 1.0), Point(0.0, 1.0))
        val cameraAngle = 0.0
        val flightHeight = 100.0
        val groundImageDim = mappingMissionService.computeGroundImageDimension(flightHeight)!!
        val expected = ParallelogramMissionBuilder.buildDoublePassMappingMission(
            startingPoint,
            area,
            cameraAngle,
            flightHeight,
            groundImageDim
        )
        val obtained =
            mappingMissionService.buildDoublePassMappingMission(startingPoint, area, flightHeight)

        assertEquals(expected, obtained)
    }

}