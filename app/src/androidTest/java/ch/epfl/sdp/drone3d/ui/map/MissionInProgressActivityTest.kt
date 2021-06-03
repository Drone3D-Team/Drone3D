/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.map

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.matcher.ToastMatcher
import ch.epfl.sdp.drone3d.model.weather.WeatherReport
import ch.epfl.sdp.drone3d.service.api.drone.DroneData
import ch.epfl.sdp.drone3d.service.api.drone.DroneExecutor
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.api.weather.WeatherService
import ch.epfl.sdp.drone3d.service.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.service.module.DroneModule
import ch.epfl.sdp.drone3d.service.module.LocationModule
import ch.epfl.sdp.drone3d.service.module.WeatherModule
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.mockito.Mockito.*
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Semaphore

/**
 * Test MissionInProgressActivity with a mission as input
 */
@HiltAndroidTest
@UninstallModules(DroneModule::class, LocationModule::class, WeatherModule::class)
class MissionInProgressActivityTest {

    companion object {
        private val GOOD_WEATHER_REPORT = WeatherReport(
            "Clear", "description",
            "01d", 20.0, 20, 5.0, 500, Date(12903)
        )

        private val BAD_WEATHER_REPORT = WeatherReport(
            "RAIN", "description",
            "10d", -1.0, 20, 10.0, 500, Date(12903)
        )
    }

    private val statusLiveData = MutableLiveData<DroneData.DroneStatus>()
    private val videoStreamLiveData = MutableLiveData<String>()
    private var missionEndFuture: CompletableFuture<Any> = CompletableFuture()

    private val activityRule = ActivityScenarioRule(MissionInProgressActivity::class.java)

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val droneService: DroneService = DroneInstanceMock.mockService()

    @BindValue
    val locationService: LocationService = mock(LocationService::class.java)

    @BindValue
    val weatherService: WeatherService = mock(WeatherService::class.java)

    init {
        val executor = mock(DroneExecutor::class.java)

        `when`(droneService.getExecutor()).thenReturn(executor)
        `when`(executor.executeMission(anyObj(Context::class.java)))
            .thenAnswer { Completable.fromFuture(missionEndFuture).subscribeOn(Schedulers.io()) }
        `when`(executor.returnToHomeLocationAndLand(anyObj(Context::class.java)))
            .thenReturn(Completable.complete())
        `when`(executor.returnToUserLocationAndLand(anyObj(Context::class.java)))
            .thenReturn(Completable.complete())

        `when`(droneService.isSimulation()).thenReturn(false)

        `when`(droneService.getData().isConnected()).thenReturn(MutableLiveData(true))
        `when`(droneService.getData().getPosition()).thenReturn(MutableLiveData(LatLng(0.3, 0.0)))
        `when`(droneService.getData().getHomeLocation()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getDroneStatus()).thenReturn(statusLiveData)
        `when`(droneService.getData().isConnected()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getVideoStreamUri()).thenReturn(videoStreamLiveData)
        `when`(droneService.getData().getMission()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getSpeed()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getRelativeAltitude()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getBatteryLevel()).thenReturn(MutableLiveData())
        `when`(droneService.getData().isMissionPaused()).thenReturn(MutableLiveData(true))

        `when`(locationService.isLocationEnabled()).thenReturn(false)
        `when`(locationService.getCurrentLocation()).thenReturn(LatLng(0.0, 0.0))

        `when`(weatherService.getWeatherReport(droneService.getData().getPosition().value!!))
            .thenReturn(MutableLiveData(GOOD_WEATHER_REPORT))
        `when`(weatherService.getWeatherReport(anyObj(LatLng::class.java)))
            .thenReturn(MutableLiveData(GOOD_WEATHER_REPORT))
    }

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun videoShownWhenUirGiven() {
        Espresso.onView(ViewMatchers.withId(R.id.camera_mission_view))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)))

        videoStreamLiveData.postValue("rtsp://")

        Espresso.onView(ViewMatchers.withId(R.id.camera_mission_view))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

        videoStreamLiveData.postValue(null)

        Espresso.onView(ViewMatchers.withId(R.id.camera_mission_view))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)))
    }

    @Test
    fun loosingDroneConnectionShowsToast() {
        val isConnected = MutableLiveData(true)

        `when`(droneService.getData().isConnected()).thenReturn(isConnected)

        // Test that the toast is displayed
        val activity = CompletableFuture<MissionInProgressActivity>()
        activityRule.scenario.onActivity {
            isConnected.value = false
            activity.complete(it)
        }

        ToastMatcher.onToast(activity.get(), R.string.lost_connection_message)
    }

    @Test
    fun goBackOnError() {

        val activity = CompletableFuture<MissionInProgressActivity>()
        activityRule.scenario.onActivity {
            activity.complete(it)
        }

        missionEndFuture.completeExceptionally(Error("test"))

        Thread.sleep(500)
        ToastMatcher.onToast(activity.get(), activity.get().getString(R.string.drone_setup_mission_error, "test"))

        assertThat(activityRule.scenario.state, `is`(Lifecycle.State.DESTROYED))

        missionEndFuture = CompletableFuture() //reset
    }

    @Test
    fun activityFinishesWhenBackToHomePressedANdMissionEnds() {

        val dronePosition = MutableLiveData(LatLng(10.1, 10.1))
        val homePosition = MutableLiveData(LatLng(0.0, 0.0))

        `when`(droneService.getData().getPosition()).thenReturn(dronePosition)
        `when`(droneService.getData().getHomeLocation()).thenReturn(
            MutableLiveData(
                Telemetry.Position(
                    homePosition.value?.latitude,
                    homePosition.value?.longitude,
                    10f,
                    10f
                )
            )
        )
        statusLiveData.postValue(DroneData.DroneStatus.EXECUTING_MISSION)
        `when`(droneService.getData().isConnected()).thenReturn(MutableLiveData(true))

        Espresso.onView(ViewMatchers.withId(R.id.backToHomeButton))
            .perform(ViewActions.click())

        missionEndFuture.complete(Any())

        Thread.sleep(2000)

        assertThat(activityRule.scenario.state, `is`(Lifecycle.State.DESTROYED))


        missionEndFuture = CompletableFuture() //reset
    }

    @Test
    fun activityFinishesWhenBackToUserPressedANdMissionEnds() {

        val dronePosition = MutableLiveData(LatLng(10.1, 10.1))
        val userPosition = MutableLiveData(LatLng(0.0, 0.0))

        `when`(droneService.getData().getPosition()).thenReturn(dronePosition)
        `when`(droneService.getData().getHomeLocation()).thenReturn(
            MutableLiveData(
                Telemetry.Position(
                    userPosition.value?.latitude,
                    userPosition.value?.longitude,
                    10f,
                    10f
                )
            )
        )
        statusLiveData.postValue(DroneData.DroneStatus.EXECUTING_MISSION)
        `when`(droneService.getData().isConnected()).thenReturn(MutableLiveData(true))

        Espresso.onView(ViewMatchers.withId(R.id.backToUserButton))
            .perform(ViewActions.click())

        missionEndFuture.complete(Any())

        Thread.sleep(2000)

        assertThat(activityRule.scenario.state, `is`(Lifecycle.State.DESTROYED))

        missionEndFuture = CompletableFuture() //reset
    }

    @Test
    fun buttonsBecomeVisibleWhenMissionIsExecuted() {
        val mutex = Semaphore(0)

        activityRule.scenario.onActivity {
            statusLiveData.value = DroneData.DroneStatus.IDLE
            statusLiveData.value = DroneData.DroneStatus.ARMING
            mutex.release()
        }

        mutex.acquire()

        Espresso.onView(ViewMatchers.withId(R.id.backToHomeButton))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        Espresso.onView(ViewMatchers.withId(R.id.backToUserButton))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))

        activityRule.scenario.onActivity {
            statusLiveData.value = DroneData.DroneStatus.EXECUTING_MISSION
            mutex.release()
        }

        mutex.acquire()

        Espresso.onView(ViewMatchers.withId(R.id.backToHomeButton))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.backToUserButton))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

        activityRule.scenario.onActivity {
            statusLiveData.value = DroneData.DroneStatus.IDLE
            mutex.release()
        }
    }

    @Test
    fun toastShowedWhenDroneTooFar() {
        val dronePosition = MutableLiveData(LatLng(0.1, 0.1))
        val homePosition = MutableLiveData(LatLng(0.0, 0.0))

        `when`(droneService.getData().getPosition()).thenReturn(dronePosition)
        `when`(droneService.getData().getHomeLocation()).thenReturn(
            MutableLiveData(
                Telemetry.Position(
                    homePosition.value?.latitude,
                    homePosition.value?.longitude,
                    10f,
                    10f
                )
            )
        )
        statusLiveData.postValue(DroneData.DroneStatus.EXECUTING_MISSION)
        `when`(droneService.getData().isConnected()).thenReturn(MutableLiveData(true))
        `when`(droneService.getData().isMissionPaused()).thenReturn(MutableLiveData(false))

        // Test that the toast is displayed
        val activity = CompletableFuture<MissionInProgressActivity>()
        activityRule.scenario.onActivity {
            dronePosition.value = LatLng(10.1, 10.1)
            activity.complete(it)
        }

        ToastMatcher.onToast(activity.get(), R.string.drone_too_far)
    }

    @Test
    fun toastShowedWhenDroneCloseEnoughAgain() {
        val dronePosition = MutableLiveData(LatLng(10.1, 10.1))
        val homePosition = MutableLiveData(LatLng(0.0, 0.0))

        `when`(droneService.getData().getPosition()).thenReturn(dronePosition)
        `when`(droneService.getData().getHomeLocation()).thenReturn(
            MutableLiveData(
                Telemetry.Position(
                    homePosition.value?.latitude,
                    homePosition.value?.longitude,
                    10f,
                    10f
                )
            )
        )
        statusLiveData.postValue(DroneData.DroneStatus.EXECUTING_MISSION)
        `when`(droneService.getData().isConnected()).thenReturn(MutableLiveData(true))
        `when`(droneService.getData().isMissionPaused()).thenReturn(MutableLiveData(true))

        // Test that the toast is displayed
        val activity = CompletableFuture<MissionInProgressActivity>()
        activityRule.scenario.onActivity {
            dronePosition.value = LatLng(0.0, 0.0)
            activity.complete(it)
        }

        ToastMatcher.onToast(activity.get(), R.string.drone_close_again)
    }

    private fun <T> anyObj(type: Class<T>): T = any(type)
}