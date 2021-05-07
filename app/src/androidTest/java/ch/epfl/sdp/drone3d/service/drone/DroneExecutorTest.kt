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
import ch.epfl.sdp.drone3d.service.api.drone.DroneData
import ch.epfl.sdp.drone3d.service.api.drone.DroneDataEditable
import ch.epfl.sdp.drone3d.service.api.drone.DroneExecutor
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.impl.drone.DroneDataImpl
import ch.epfl.sdp.drone3d.service.impl.drone.DroneExecutorImpl
import ch.epfl.sdp.drone3d.service.impl.drone.DroneUtils
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.mission.Mission
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.lang.IllegalStateException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

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
    fun startMissionIdleUpdatesLiveData() {

        val future = CompletableFuture<Mission.MissionProgress>()

        DroneInstanceMock.setupDefaultMocks()
        `when`(DroneInstanceMock.droneMission.missionProgress)
            .thenReturn(Flowable.fromFuture(future).subscribeOn(Schedulers.io()))

        val locationService = mock(LocationService::class.java)
        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val droneData = DroneDataImpl(droneService)
        droneData.getMutableDroneStatus().postValue(DroneData.DroneStatus.IDLE)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)

        executor.startMission(
            InstrumentationRegistry.getInstrumentation().targetContext,
            DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE)
        ).subscribe({
            assertThat(droneData.getMission().value, nullValue())
        }, {
            it.printStackTrace()
        })

        assertThat(droneData.getMission().value?.isEmpty(), `is`(false))
        //End mission
        future.complete(Mission.MissionProgress(4, 4))

        verify(DroneInstanceMock.droneAction, atLeastOnce()).arm()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).takeoff()
        verify(DroneInstanceMock.droneMission, atLeastOnce()).startMission()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).land()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).disarm()
    }

    @Test
    fun startMissionTakingOffUpdatesLiveData() {

        val future = CompletableFuture<Mission.MissionProgress>()

        DroneInstanceMock.setupDefaultMocks()
        `when`(DroneInstanceMock.droneMission.missionProgress)
                .thenReturn(Flowable.fromFuture(future).subscribeOn(Schedulers.io()))

        val locationService = mock(LocationService::class.java)
        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val droneData = DroneDataImpl(droneService)
        droneData.getMutableDroneStatus().postValue(DroneData.DroneStatus.TAKING_OFF)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)

        executor.startMission(
                InstrumentationRegistry.getInstrumentation().targetContext,
                DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE)
        ).subscribe({
            assertThat(droneData.getMission().value, nullValue())
        }, {
            it.printStackTrace()
        })

        assertThat(droneData.getMission().value?.isEmpty(), `is`(false))
        //End mission
        future.complete(Mission.MissionProgress(4, 4))

        verify(DroneInstanceMock.droneAction, atLeastOnce()).takeoff()
        verify(DroneInstanceMock.droneMission, atLeastOnce()).startMission()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).land()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).disarm()
    }

    @Test
    fun startMissionStartingUpdatesLiveData() {

        val future = CompletableFuture<Mission.MissionProgress>()

        DroneInstanceMock.setupDefaultMocks()
        `when`(DroneInstanceMock.droneMission.missionProgress)
                .thenReturn(Flowable.fromFuture(future).subscribeOn(Schedulers.io()))

        val locationService = mock(LocationService::class.java)
        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val droneData = DroneDataImpl(droneService)
        droneData.getMutableDroneStatus().postValue(DroneData.DroneStatus.STARTING_MISSION)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)

        executor.startMission(
                InstrumentationRegistry.getInstrumentation().targetContext,
                DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE)
        ).subscribe({
            assertThat(droneData.getMission().value, nullValue())
        }, {
            it.printStackTrace()
        })

        assertThat(droneData.getMission().value?.isEmpty(), `is`(false))
        //End mission
        future.complete(Mission.MissionProgress(4, 4))

        verify(DroneInstanceMock.droneMission, atLeastOnce()).startMission()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).land()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).disarm()
    }

    @Test
    fun startMissionDuringMissionFails() {

        val future = CompletableFuture<Mission.MissionProgress>()

        DroneInstanceMock.setupDefaultMocks()
        `when`(DroneInstanceMock.droneMission.missionProgress)
                .thenReturn(Flowable.fromFuture(future).subscribeOn(Schedulers.io()))

        val locationService = mock(LocationService::class.java)
        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val droneData = DroneDataImpl(droneService)
        droneData.getMutableDroneStatus().postValue(DroneData.DroneStatus.EXECUTING_MISSION)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)

        assertThrows(IllegalStateException::class.java) {
            executor.startMission(
                    InstrumentationRegistry.getInstrumentation().targetContext,
                    DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE)
            ).subscribe({
                assertThat(droneData.getMission().value, nullValue())
            }, {
                it.printStackTrace()
            })
        }

        future.complete(Mission.MissionProgress(4, 4))
    }

    @Test
    fun canStartMissionAndReturnHome() {
        val expectedLatLng = LatLng(47.397428, 8.545369) //Position of the drone before take off
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val future = CompletableFuture<Mission.MissionProgress>()

        DroneInstanceMock.setupDefaultMocks()
        `when`(DroneInstanceMock.droneMission.missionProgress)
                .thenReturn(Flowable.fromFuture(future).subscribeOn(Schedulers.io()))

        val locationService = mock(LocationService::class.java)
        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val droneData = mock(DroneDataEditable::class.java)
        val missionLiveData = MutableLiveData<List<Mission.MissionItem>>()
        `when`(droneData.getPosition()).thenReturn(MutableLiveData(expectedLatLng))
        `when`(droneData.getMutableMissionPaused()).thenReturn(MutableLiveData())
        `when`(droneData.getHomeLocation()).thenReturn(
            MutableLiveData(
                Telemetry.Position(expectedLatLng.latitude, expectedLatLng.longitude, 400f, 50f)
            )
        )
        `when`(droneData.getMutableMission()).thenReturn(missionLiveData)
        `when`(droneData.getDroneStatus())
            .thenReturn(MutableLiveData(DroneData.DroneStatus.IDLE))
        `when`(droneData.getMutableDroneStatus())
            .thenReturn(MutableLiveData(DroneData.DroneStatus.IDLE))

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)

        executor.startMission(
            context,
            DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE)
        ).subscribe({}, { it.printStackTrace() })

        `when`(DroneInstanceMock.droneMission.uploadMission(ArgumentMatchers.any(Mission.MissionPlan::class.java)))
            .thenAnswer {
                val plan = it.getArgument<Mission.MissionPlan>(0)
                assertThat(plan, notNullValue())
                assertThat(plan.missionItems.size, `is`(1))

                val goal = plan.missionItems[0]

                //compare both position
                assertThat(goal.latitudeDeg, closeTo(expectedLatLng.latitude, EPSILON))
                assertThat(goal.longitudeDeg, closeTo(expectedLatLng.longitude, EPSILON))

                Completable.complete()
            }

        executor.returnToHomeLocationAndLand(context)
            .subscribe({}, { it.printStackTrace() })

        assertThat(missionLiveData.value, nullValue())
    }

    @Test
    fun canPauseAndResumeMission() {
        val future = CompletableFuture<Mission.MissionProgress>()

        DroneInstanceMock.setupDefaultMocks()
        `when`(DroneInstanceMock.droneMission.missionProgress)
                .thenReturn(Flowable.fromFuture(future).subscribeOn(Schedulers.io()))

        val locationService = mock(LocationService::class.java)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val droneData = DroneDataImpl(droneService)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)

        executor.startMission(
            context,
            DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE)
        ).subscribe({}, { it.printStackTrace() })

        assertThat(droneData.getMutableMission().value?.isEmpty(), `is`(false))
        assertThat(droneData.isMissionPaused().value, `is`(false))

        executor.pauseMission(context).subscribe()
        assertThat(droneData.isMissionPaused().value, `is`(true))

        executor.resumeMission(context).subscribe()
        assertThat(droneData.isMissionPaused().value, `is`(false))
    }

    @Test
    fun canStartMissionAndReturnToUser() {
        val locationService = mock(LocationService::class.java)
        `when`(locationService.isLocationEnabled()).thenReturn(true)
        `when`(locationService.getCurrentLocation()).thenReturn(LatLng(0.0, 0.0))

        val userPosition = locationService.getCurrentLocation()!!
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        DroneInstanceMock.setupDefaultMocks()

        `when`(DroneInstanceMock.droneMission.missionProgress)
            .thenReturn(Flowable.fromArray(Mission.MissionProgress(0, 4))
                .delay(10, TimeUnit.SECONDS))

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
        `when`(droneData.getDroneStatus())
            .thenReturn(MutableLiveData(DroneData.DroneStatus.IDLE))
        `when`(droneData.getMutableDroneStatus())
                .thenReturn(MutableLiveData(DroneData.DroneStatus.IDLE))

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)

        executor.startMission(
            context,
            DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE)
        ).subscribe({}, { it.printStackTrace() })

        `when`(DroneInstanceMock.droneMission.uploadMission(ArgumentMatchers.any(Mission.MissionPlan::class.java)))
            .thenAnswer {
                val plan = it.getArgument<Mission.MissionPlan>(0)
                assertThat(plan, notNullValue())
                assertThat(plan.missionItems.size, `is`(1))

                val goal = plan.missionItems[0]

                //compare both position
                assertThat(goal.latitudeDeg, closeTo(userPosition.latitude, EPSILON))
                assertThat(goal.longitudeDeg, closeTo(userPosition.longitude, EPSILON))
                Completable.complete()
            }

        executor.returnToUserLocationAndLand(context)
            .subscribe({}, { it.printStackTrace() })

        assertThat(missionLiveData.value, nullValue())
    }
}