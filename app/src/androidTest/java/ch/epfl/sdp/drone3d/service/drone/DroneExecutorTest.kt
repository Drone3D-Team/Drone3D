/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.drone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.service.api.drone.DroneData.DroneStatus.*
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
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class DroneExecutorTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val armedPublisher: PublishSubject<Boolean> = PublishSubject.create()
    private val flightModePublisher: PublishSubject<Telemetry.FlightMode> = PublishSubject.create()
    private val missionProgressPublisher: PublishSubject<Mission.MissionProgress> = PublishSubject.create()
    private val inAirPublisher: PublishSubject<Boolean> = PublishSubject.create()

    companion object {
        private const val EPSILON = 1e-5
        private const val ALTITUDE = 10f
        private const val CAMERA_PITCH = 0f
        val someLocationsList = listOf(
            LatLng(47.398979, 8.543434),
            LatLng(47.398279, 8.543934),
            LatLng(47.397426, 8.544867),
            LatLng(47.397026, 8.543067)
        )
    }

    private fun setupOwnMocks() {
        DroneInstanceMock.setupDefaultMocks()

        `when`(DroneInstanceMock.droneTelemetry.armed)
                .thenReturn(armedPublisher.toFlowable(BackpressureStrategy.BUFFER)
                        .cacheWithInitialCapacity(1))
        `when`(DroneInstanceMock.droneTelemetry.flightMode)
                .thenReturn(flightModePublisher.toFlowable(BackpressureStrategy.BUFFER)
                        .cacheWithInitialCapacity(1))
        `when`(DroneInstanceMock.droneMission.missionProgress)
                .thenReturn(missionProgressPublisher.toFlowable(BackpressureStrategy.BUFFER)
                        .cacheWithInitialCapacity(1))
        `when`(DroneInstanceMock.droneTelemetry.inAir)
                .thenReturn(inAirPublisher.toFlowable(BackpressureStrategy.BUFFER)
                        .cacheWithInitialCapacity(1))
    }

    @Test
    fun startMissionNotArmedUpdatesLiveData() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val locationService = mock(LocationService::class.java)

        val droneData = DroneDataImpl(droneService)
        droneData.getMutableDroneStatus().postValue(IDLE)

        setupDroneAsserts(droneData)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)
        val mutex = Semaphore(0)

        armedPublisher.onNext(false)
        inAirPublisher.onNext(false)
        flightModePublisher.onNext(Telemetry.FlightMode.TAKEOFF)

        executor.startMission(
            InstrumentationRegistry.getInstrumentation().targetContext,
            DroneUtils.makeDroneMission(someLocationsList, ALTITUDE, CAMERA_PITCH)
        ).subscribe({
            assertThat(droneData.getMission().value, nullValue())
            mutex.release()
        }, {
            throw it
        })

        missionProgressPublisher.onNext(Mission.MissionProgress(0, 4))
        missionProgressPublisher.onNext(Mission.MissionProgress(3, 4))

        dataBecomes(droneData.getDroneStatus(), GOING_BACK)

        //End mission
        missionProgressPublisher.onNext(Mission.MissionProgress(4, 4))
        inAirPublisher.onNext(false)

        assertThat(mutex.tryAcquire(100, TimeUnit.MILLISECONDS), `is`(true))

        verify(DroneInstanceMock.droneAction, atLeastOnce()).arm()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).takeoff()
        verify(DroneInstanceMock.droneMission, atLeastOnce()).startMission()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).land()
    }


    @Test
    fun startMissionTakingOffUpdatesLiveData() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val locationService = mock(LocationService::class.java)

        val droneData = DroneDataImpl(droneService)
        droneData.getMutableDroneStatus().postValue(IDLE)

        setupDroneAsserts(droneData)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)
        val mutex = Semaphore(0)

        armedPublisher.onNext(true)
        inAirPublisher.onNext(false)
        flightModePublisher.onNext(Telemetry.FlightMode.LAND)

        executor.startMission(
                InstrumentationRegistry.getInstrumentation().targetContext,
                DroneUtils.makeDroneMission(someLocationsList, ALTITUDE,CAMERA_PITCH)
        ).subscribe({
            assertThat(droneData.getMission().value, nullValue())
            mutex.release()
        }, {
            throw it
        })

        missionProgressPublisher.onNext(Mission.MissionProgress(0, 4))
        missionProgressPublisher.onNext(Mission.MissionProgress(3, 4))

        dataBecomes(droneData.getDroneStatus(), GOING_BACK)

        //End mission
        missionProgressPublisher.onNext(Mission.MissionProgress(4, 4))
        inAirPublisher.onNext(false)

        assertThat(mutex.tryAcquire(100, TimeUnit.MILLISECONDS), `is`(true))

        verify(DroneInstanceMock.droneAction, never()).arm()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).takeoff()
        verify(DroneInstanceMock.droneMission, atLeastOnce()).startMission()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).land()
    }

    @Test
    fun startMissionStartingUpdatesLiveData() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val locationService = mock(LocationService::class.java)

        val droneData = DroneDataImpl(droneService)
        droneData.getMutableDroneStatus().postValue(IDLE)

        setupDroneAsserts(droneData)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)
        val mutex = Semaphore(0)

        armedPublisher.onNext(true)
        inAirPublisher.onNext(true)
        flightModePublisher.onNext(Telemetry.FlightMode.READY)

        executor.startMission(
                InstrumentationRegistry.getInstrumentation().targetContext,
                DroneUtils.makeDroneMission(someLocationsList, ALTITUDE,
                    CAMERA_PITCH)
        ).subscribe({
            assertThat(droneData.getMission().value, nullValue())
            mutex.release()
        }, {
            throw it
        })

        missionProgressPublisher.onNext(Mission.MissionProgress(0, 4))
        missionProgressPublisher.onNext(Mission.MissionProgress(3, 4))

        dataBecomes(droneData.getDroneStatus(), GOING_BACK)

        //End mission
        missionProgressPublisher.onNext(Mission.MissionProgress(4, 4))
        inAirPublisher.onNext(false)

        assertThat(mutex.tryAcquire(100, TimeUnit.MILLISECONDS), `is`(true))

        verify(DroneInstanceMock.droneAction, never()).arm()
        verify(DroneInstanceMock.droneAction, never()).takeoff()
        verify(DroneInstanceMock.droneMission, atLeastOnce()).startMission()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).land()
    }

    @Test
    fun startMissionAtHoldWorks() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val locationService = mock(LocationService::class.java)

        val droneData = DroneDataImpl(droneService)
        droneData.getMutableDroneStatus().postValue(IDLE)

        setupDroneAsserts(droneData)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)
        val mutex = Semaphore(0)

        armedPublisher.onNext(true)
        inAirPublisher.onNext(true)
        flightModePublisher.onNext(Telemetry.FlightMode.HOLD)

        executor.startMission(
                InstrumentationRegistry.getInstrumentation().targetContext,
                DroneUtils.makeDroneMission(someLocationsList, ALTITUDE, CAMERA_PITCH)
        ).subscribe({
            assertThat(droneData.getMission().value, nullValue())
            mutex.release()
        }, {
            throw it
        })

        missionProgressPublisher.onNext(Mission.MissionProgress(0, 4))
        missionProgressPublisher.onNext(Mission.MissionProgress(3, 4))

        dataBecomes(droneData.getDroneStatus(), GOING_BACK)

        //End mission
        missionProgressPublisher.onNext(Mission.MissionProgress(4, 4))
        inAirPublisher.onNext(false)

        assertThat(mutex.tryAcquire(100, TimeUnit.MILLISECONDS), `is`(true))

        verify(DroneInstanceMock.droneAction, never()).arm()
        verify(DroneInstanceMock.droneAction, never()).takeoff()
        verify(DroneInstanceMock.droneMission, atLeastOnce()).startMission()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).land()
    }

    @Test
    fun startMissionDuringMissionWorks() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val locationService = mock(LocationService::class.java)

        val droneData = DroneDataImpl(droneService)
        droneData.getMutableDroneStatus().postValue(IDLE)

        setupDroneAsserts(droneData)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)
        val mutex = Semaphore(0)

        armedPublisher.onNext(true)
        inAirPublisher.onNext(true)
        flightModePublisher.onNext(Telemetry.FlightMode.MISSION)

        executor.startMission(
                InstrumentationRegistry.getInstrumentation().targetContext,
                DroneUtils.makeDroneMission(someLocationsList, ALTITUDE,
                    CAMERA_PITCH)
        ).subscribe({
            assertThat(droneData.getMission().value, nullValue())
            mutex.release()
        }, {
            throw it
        })

        missionProgressPublisher.onNext(Mission.MissionProgress(0, 4))
        missionProgressPublisher.onNext(Mission.MissionProgress(3, 4))

        dataBecomes(droneData.getDroneStatus(), GOING_BACK)

        //End mission
        missionProgressPublisher.onNext(Mission.MissionProgress(4, 4))
        inAirPublisher.onNext(false)

        assertThat(mutex.tryAcquire(100, TimeUnit.MILLISECONDS), `is`(true))

        verify(DroneInstanceMock.droneAction, never()).arm()
        verify(DroneInstanceMock.droneAction, never()).takeoff()
        verify(DroneInstanceMock.droneMission, never()).startMission()
        verify(DroneInstanceMock.droneAction, atLeastOnce()).land()
    }



    @Test
    fun startMissionFailsOnInvalidState() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val locationService = mock(LocationService::class.java)

        val droneData = DroneDataImpl(droneService)
        droneData.getMutableDroneStatus().postValue(IDLE)

        setupDroneAsserts(droneData)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)

        armedPublisher.onNext(true)
        flightModePublisher.onNext(Telemetry.FlightMode.UNKNOWN)

        executor.startMission(
                InstrumentationRegistry.getInstrumentation().targetContext,
                DroneUtils.makeDroneMission(someLocationsList, ALTITUDE,
                    CAMERA_PITCH)
        ).subscribe({
            assertThat(true, `is`(false))
        }, {
            assertThat(it, `is`(instanceOf(IllegalStateException::class.java)))
        })
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
            .thenReturn(MutableLiveData(IDLE))
        `when`(droneData.getMutableDroneStatus())
            .thenReturn(MutableLiveData(IDLE))

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)

        executor.startMission(
            context,
            DroneUtils.makeDroneMission(someLocationsList, ALTITUDE, CAMERA_PITCH)
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
        setupOwnMocks()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val locationService = mock(LocationService::class.java)

        val droneData = DroneDataImpl(droneService)
        droneData.getMutableDroneStatus().postValue(IDLE)

        setupDroneAsserts(droneData)

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)

        armedPublisher.onNext(true)
        inAirPublisher.onNext(true)
        flightModePublisher.onNext(Telemetry.FlightMode.HOLD)

        executor.startMission(
                InstrumentationRegistry.getInstrumentation().targetContext,
                DroneUtils.makeDroneMission(someLocationsList, ALTITUDE,
                    CAMERA_PITCH)
        ).subscribe({
            assertThat(droneData.getMission().value, nullValue())
        }, {
            throw it
        })

        missionProgressPublisher.onNext(Mission.MissionProgress(0, 4))

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
            .thenReturn(MutableLiveData(IDLE))
        `when`(droneData.getMutableDroneStatus())
                .thenReturn(MutableLiveData(IDLE))

        val executor: DroneExecutor = DroneExecutorImpl(droneService, droneData, locationService)

        executor.startMission(
            context,
            DroneUtils.makeDroneMission(someLocationsList, ALTITUDE, CAMERA_PITCH)
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

    private fun <T> dataBecomes(data: LiveData<T>, expected: T) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            var i = 0
            while (i++ < 10 && data.value != expected)
                Thread.sleep(10)

            assertThat(data.value, `is`(expected))
        }
    }

    private fun setupDroneAsserts(droneData: DroneDataImpl) {
        // Check the state that should be there on each call
        `when`(DroneInstanceMock.droneAction.arm()).thenAnswer {
            Completable.fromCallable {
                dataBecomes(droneData.getDroneStatus(), ARMING)
                flightModePublisher.onNext(Telemetry.FlightMode.READY)
                armedPublisher.onNext(true)
            }
        }

        `when`(DroneInstanceMock.droneAction.takeoff()).thenAnswer {
            Completable.fromCallable {
                dataBecomes(droneData.getDroneStatus(), TAKING_OFF)
                inAirPublisher.onNext(true)
                flightModePublisher.onNext(Telemetry.FlightMode.HOLD)
            }
        }

        `when`(DroneInstanceMock.droneMission.startMission()).thenAnswer {
            Completable.fromCallable {
                dataBecomes(droneData.getDroneStatus(), SENDING_ORDER)
                flightModePublisher.onNext(Telemetry.FlightMode.MISSION)
            }
        }

        `when`(DroneInstanceMock.droneAction.land()).thenAnswer {
            Completable.fromCallable {
                dataBecomes(droneData.getDroneStatus(), LANDING)
                flightModePublisher.onNext(Telemetry.FlightMode.LAND)
            }
        }
    }
}
