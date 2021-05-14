/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.matcher.ToastMatcher
import ch.epfl.sdp.drone3d.model.auth.UserSession
import ch.epfl.sdp.drone3d.model.weather.WeatherReport
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.api.drone.DroneExecutor
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.api.mission.MappingMissionService
import ch.epfl.sdp.drone3d.service.api.weather.WeatherService
import ch.epfl.sdp.drone3d.service.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.service.module.AuthenticationModule
import ch.epfl.sdp.drone3d.service.module.DroneModule
import ch.epfl.sdp.drone3d.service.module.LocationModule
import ch.epfl.sdp.drone3d.service.module.WeatherModule
import ch.epfl.sdp.drone3d.ui.map.MissionInProgressActivity
import ch.epfl.sdp.drone3d.ui.weather.WeatherInfoActivity
import com.google.firebase.auth.FirebaseUser
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mavsdk.mission.Mission
import io.reactivex.Completable
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*

@HiltAndroidTest
@UninstallModules(
    DroneModule::class,
    AuthenticationModule::class,
    LocationModule::class,
    WeatherModule::class
)
class ItineraryShowActivityTest {

    companion object{
        private val GOOD_WEATHER_REPORT = WeatherReport(
            "Clear", "description",
            20.0, 20, 5.0, 500, Date(12903)
        )

        private val BAD_WEATHER_REPORT = WeatherReport(
            "RAIN", "description",
            -1.0, 20, 10.0, 500, Date(12903)
        )

        private val USER_UID = "asdfg"

        private val bayland_area = arrayListOf(
            LatLng(37.41253570576311, -121.99694775011824),
            LatLng(37.412496825414046, -121.99683107403213),
            LatLng(37.41243024942702, -121.99686795440418)
        )
    }



    private val activityRule = ActivityScenarioRule<ItineraryShowActivity>(
        Intent(
            ApplicationProvider.getApplicationContext(),
            ItineraryShowActivity::class.java
        ).apply {
            putExtras(Bundle().apply {
                putSerializable(MissionViewAdapter.AREA_INTENT_PATH, bayland_area)
                putSerializable(MissionViewAdapter.FLIGHTHEIGHT_INTENT_PATH, 50.0)
                putSerializable(MissionViewAdapter.STRATEGY_INTENT_PATH, MappingMissionService.Strategy.SINGLE_PASS)
            })
        }
    )

    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val droneService: DroneService = DroneInstanceMock.mockService()

    @BindValue
    val authService: AuthenticationService = Mockito.mock(AuthenticationService::class.java)

    @BindValue
    val weatherService: WeatherService = Mockito.mock(WeatherService::class.java)

    @BindValue
    val locationService: LocationService = Mockito.mock(LocationService::class.java)

    init {
        `when`(droneService.getData().isConnected()).thenReturn(MutableLiveData(true))
        `when`(droneService.getData().getPosition()).thenReturn(MutableLiveData(LatLng(70.1, 40.3)))
        `when`(droneService.getData().getHomeLocation()).thenReturn(MutableLiveData())
        `when`(droneService.getData().isFlying()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getDroneStatus()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getVideoStreamUri()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getMission()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getSpeed()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getRelativeAltitude()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getBatteryLevel()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getSensorSize()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getFocalLength()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getCameraResolution()).thenReturn(MutableLiveData())

        `when`(weatherService.getWeatherReport(bayland_area[0])).thenReturn(
            MutableLiveData(
                GOOD_WEATHER_REPORT
            )
        )

        val executor = Mockito.mock(DroneExecutor::class.java)
        `when`(droneService.getExecutor()).thenReturn(executor)
        `when`(
            executor.startMission(
                anyObj(Context::class.java),
                anyObj(Mission.MissionPlan::class.java)
            )
        )
            .thenReturn(Completable.never())
        `when`(executor.returnToHomeLocationAndLand(anyObj(Context::class.java)))
            .thenReturn(Completable.complete())
        `when`(executor.returnToUserLocationAndLand(anyObj(Context::class.java)))
            .thenReturn(Completable.complete())
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
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("ch.epfl.sdp.drone3d", appContext.packageName)
    }

    @Test
    fun goToMissionInProgressActivityShowToastWhenDroneIsNotConnected() {
        `when`(droneService.isConnected()).thenReturn(false)
        `when`(
            droneService.getData()
                .getPosition()
        ).thenReturn(MutableLiveData(bayland_area[0]))
        `when`(weatherService.getWeatherReport(bayland_area[0])).thenReturn(
            MutableLiveData(
                GOOD_WEATHER_REPORT
            )
        )

        activityRule.scenario.recreate()
        activityRule.scenario.onActivity {
            ToastMatcher.onToast(it, R.string.launch_no_drone)
        }

        onView(withId(R.id.buttonToMissionInProgressActivity)).perform(click())
    }


    @Test
    fun goToMissionProgressActivityShowToastWhenDroneTooFar() {
        `when`(droneService.isConnected()).thenReturn(true)
        `when`(droneService.getData().getPosition()).thenReturn(MutableLiveData(LatLng(70.1, 40.3)))
        `when`(weatherService.getWeatherReport(bayland_area[0])).thenReturn(
            MutableLiveData(
                GOOD_WEATHER_REPORT
            )
        )

        activityRule.scenario.recreate()
        activityRule.scenario.onActivity {
            ToastMatcher.onToast(it, R.string.drone_too_far_from_start)
        }

        onView(withId(R.id.buttonToMissionInProgressActivity)).perform(click())
    }

    @Test
    fun goToMissionProgressActivityButtonIsEnabledWhenWeatherTooBad() {
        `when`(droneService.isConnected()).thenReturn(true)
        `when`(
            droneService.getData()
                .getPosition()
        ).thenReturn(MutableLiveData(bayland_area[0]))
        `when`(weatherService.getWeatherReport(bayland_area[0])).thenReturn(
            MutableLiveData(
                BAD_WEATHER_REPORT
            )
        )

        activityRule.scenario.recreate()

        onView(withId(R.id.buttonToMissionInProgressActivity))
            .check(matches(isEnabled()))
    }

    @Test
    fun goToMissionInProgressActivityWork() {
        `when`(droneService.isConnected()).thenReturn(true)
        `when`(droneService.getData().isConnected()).thenReturn(MutableLiveData(true))
        `when`(locationService.isLocationEnabled()).thenReturn(false)
        `when`(
            droneService.getData()
                .getPosition()
        ).thenReturn(MutableLiveData(bayland_area[0]))
        `when`(weatherService.getWeatherReport(bayland_area[0])).thenReturn(
            MutableLiveData(
                GOOD_WEATHER_REPORT
            )
        )

        activityRule.scenario.recreate()

        onView(withId(R.id.buttonToMissionInProgressActivity))
            .check(matches(isEnabled()))
        onView(withId(R.id.buttonToMissionInProgressActivity)).perform(click())

        Intents.intended(
            hasComponent(hasClassName(MissionInProgressActivity::class.java.name))
        )

        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra(ItineraryShowActivity.FLIGHTPATH_INTENT_PATH) })
    }

    @Test
    fun editButtonWork() {
        activityRule.scenario.recreate()

        onView(withId(R.id.editButton))
            .check(matches(isEnabled()))
        onView(withId(R.id.editButton)).perform(click())

        Intents.intended(
            hasComponent(hasClassName(ItineraryCreateActivity::class.java.name))
        )

        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra(ItineraryCreateActivity.AREA_INTENT_PATH) })
        assert(intents.any { it.hasExtra(ItineraryCreateActivity.FLIGHTHEIGHT_INTENT_PATH) })
        assert(intents.any { it.hasExtra(ItineraryCreateActivity.STRATEGY_INTENT_PATH) })
    }

    @Test
    fun goToMissionInProgressWorksWhenBadWeather() {
        `when`(droneService.isConnected()).thenReturn(true)
        `when`(locationService.isLocationEnabled()).thenReturn(false)
        `when`(
            droneService.getData()
                .getPosition()
        ).thenReturn(MutableLiveData(bayland_area[0]))
        `when`(weatherService.getWeatherReport(bayland_area[0])).thenReturn(
            MutableLiveData(
                BAD_WEATHER_REPORT
            )
        )

        activityRule.scenario.recreate()

        onView(withId(R.id.buttonToMissionInProgressActivity))
            .check(matches(isEnabled()))
        onView(withId(R.id.buttonToMissionInProgressActivity)).perform(click())

        onView(withText(R.string.launch_mission_confirmation))
            .check(matches(isDisplayed()))
        onView(withText(R.string.confirm_launch))
            .perform(click())

        Intents.intended(
            hasComponent(hasClassName(MissionInProgressActivity::class.java.name))
        )

        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra(ItineraryShowActivity.FLIGHTPATH_INTENT_PATH) })

    }

    @Test
    fun deleteButtonNotVisibleWhenWrongUser() {
        val user = Mockito.mock(FirebaseUser::class.java)
        `when`(user.uid).thenReturn(USER_UID)

        activityRule.scenario.recreate()

        onView(withId(R.id.mission_delete))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun deleteMissionBringBackToMissionSelection() {
        val user = Mockito.mock(FirebaseUser::class.java)
        `when`(user.uid).thenReturn(USER_UID)
        val userSession = UserSession(user)
        `when`(authService.getCurrentSession()).thenReturn(userSession)

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItineraryShowActivity::class.java
        )
        intent.putExtra(MissionViewAdapter.OWNER_ID_INTENT_PATH, USER_UID)
        intent.putExtra(MissionViewAdapter.AREA_INTENT_PATH, bayland_area)
        intent.putExtra(MissionViewAdapter.FLIGHTHEIGHT_INTENT_PATH, 50.0)
        intent.putExtra(MissionViewAdapter.STRATEGY_INTENT_PATH, MappingMissionService.Strategy.SINGLE_PASS)

        ActivityScenario.launch<ItineraryShowActivity>(intent).use { _ ->
            onView(withId(R.id.mission_delete))
                .perform(click())
            onView(withText(R.string.delete_confirmation))
                .check(matches(isDisplayed()))
            onView(withText(R.string.confirm_delete))
                .perform(click())
            Intents.intended(
                hasComponent(hasClassName(MappingMissionSelectionActivity::class.java.name))
            )
        }
    }

    @Test
    fun goToWeatherInfoWorks() {
        onView(withId(R.id.weatherInfoButton))
            .perform(click())
        Intents.intended(
            hasComponent(hasClassName(WeatherInfoActivity::class.java.name))
        )
    }

    private fun <T> anyObj(type: Class<T>): T = Mockito.any(type)
}