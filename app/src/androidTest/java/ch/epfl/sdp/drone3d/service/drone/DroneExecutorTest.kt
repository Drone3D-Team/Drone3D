/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.drone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.service.api.drone.DroneDataEditable
import ch.epfl.sdp.drone3d.service.api.drone.DroneExecutor
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.impl.drone.DroneDataImpl
import ch.epfl.sdp.drone3d.service.impl.drone.DroneExecutorImpl
import ch.epfl.sdp.drone3d.service.impl.drone.DroneUtils
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.mission.Mission
import io.mavsdk.telemetry.Telemetry
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.closeTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class DroneExecutorTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        private const val EPSILON = 1e-5
        private const val DEFAULT_ALTITUDE = 10f
        val someLocationsList = listOf(
                LatLng(47.398979, 8.543434),
                LatLng(47.398279, 8.543934),
                LatLng(47.397426, 8.544867),
                LatLng(47.397026, 8.543067)
        )
    }

    @Test
    fun startMissionUpdatesLiveData() {

        DroneInstanceMock.setupDefaultMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val droneData = DroneDataImpl(droneService)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData)

        executor.startMission(InstrumentationRegistry.getInstrumentation().targetContext,
                DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE))

        // This assert prevent the app to crash in case the mission has not been updated
        assertThat(droneData.getMutableMission().value, `is`(notNullValue()))
        assertThat(droneData.getMutableMission().value?.isEmpty(), `is`(false))
    }

    @Test
    fun canStartMissionAndReturnHome() {
        val expectedLatLng = LatLng(47.397428, 8.545369) //Position of the drone before take off
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        DroneInstanceMock.setupDefaultMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val droneData = mock(DroneDataEditable::class.java)
        val missionLiveData = MutableLiveData<List<Mission.MissionItem>>()
        `when`(droneData.getPosition()).thenReturn(MutableLiveData(expectedLatLng))
        `when`(droneData.getMutableMissionPaused()).thenReturn(MutableLiveData())
        `when`(droneData.getHomeLocation()).thenReturn(MutableLiveData(
                Telemetry.Position(expectedLatLng.latitude, expectedLatLng.longitude, 400f, 50f)))
        `when`(droneData.getMutableMission()).thenReturn(missionLiveData)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData)

        executor.startMission(context, DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE))

        executor.returnToHomeLocationAndLand(context)

        assertThat(missionLiveData.value?.isEmpty(), `is`(false))
        val returnToUserMission = missionLiveData.value?.get(0)
        val currentLat = returnToUserMission?.latitudeDeg
        val currentLong = returnToUserMission?.longitudeDeg

        assertThat(currentLat, `is`(notNullValue()))
        assertThat(currentLong, `is`(notNullValue()))

        //compare both position
        assertThat(currentLat, closeTo(expectedLatLng.latitude, EPSILON))
        assertThat(currentLong, closeTo(expectedLatLng.longitude, EPSILON))
    }

    @Test
    fun canPauseAndResumeMission() {
        DroneInstanceMock.setupDefaultMocks()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val droneData = DroneDataImpl(droneService)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData)

        executor.startMission(context,
                DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE))

        assertThat(droneData.getMutableMission().value, `is`(notNullValue()))
        assertThat(droneData.getMutableMission().value?.isEmpty(), `is`(false))

        assertThat(droneData.isMissionPaused().value, `is`(notNullValue()))
        assertThat(droneData.isMissionPaused().value, `is`(false))

        executor.pauseMission(context)

        assertThat(droneData.isMissionPaused().value, `is`(notNullValue()))
        assertThat(droneData.isMissionPaused().value, `is`(true))

        executor.resumeMission(context)

        assertThat(droneData.isMissionPaused().value, `is`(notNullValue()))
        assertThat(droneData.isMissionPaused().value, `is`(false))
    }

    @Test
    fun canStartMissionAndReturnToUser() {
        val userPosition = LatLng(.0, .0) //TODO when User location service is up, change this

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        DroneInstanceMock.setupDefaultMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val droneData = mock(DroneDataEditable::class.java)
        val missionLiveData = MutableLiveData<List<Mission.MissionItem>>()
        val positionLiveData = MutableLiveData(LatLng(47.397428, 8.545369))
        val speedLiveData = MutableLiveData(10f)

        `when`(droneData.getPosition()).thenReturn(positionLiveData)
        `when`(droneData.getSpeed()).thenReturn(speedLiveData)
        `when`(droneData.getMutableMissionPaused()).thenReturn(MutableLiveData())
        `when`(droneData.getHomeLocation()).thenReturn(MutableLiveData(
            Telemetry.Position(47.397428, 8.545369, 400f, 50f)))
        `when`(droneData.getMutableMission()).thenReturn(missionLiveData)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData)

        executor.startMission(context, DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE))

        executor.returnToUserLocationAndLand(context)

        assertThat(missionLiveData.value?.isEmpty(), `is`(false))
        val returnToUserMission = missionLiveData.value?.get(0)
        val currentLat = returnToUserMission?.latitudeDeg
        val currentLong = returnToUserMission?.longitudeDeg

        assertThat(currentLat, `is`(notNullValue()))
        assertThat(currentLong, `is`(notNullValue()))

        //compare both position
        assertThat(currentLat, closeTo(userPosition.latitude, EPSILON))
        assertThat(currentLong, closeTo(userPosition.longitude, EPSILON))
    }
}