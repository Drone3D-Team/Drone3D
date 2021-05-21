/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.map.MapboxUtility
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.api.mission.MappingMissionService
import ch.epfl.sdp.drone3d.service.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.service.module.AuthenticationModule
import ch.epfl.sdp.drone3d.service.module.DroneModule
import ch.epfl.sdp.drone3d.service.module.LocationModule
import ch.epfl.sdp.drone3d.ui.MainActivity
import ch.epfl.sdp.drone3d.ui.auth.LoginActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@HiltAndroidTest
@UninstallModules(AuthenticationModule::class, DroneModule::class, LocationModule::class)
class ItineraryCreateActivityTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(ItineraryCreateActivity::class.java)

    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val authService: AuthenticationService = mock(AuthenticationService::class.java)

    @BindValue
    val droneService = DroneInstanceMock.mockServiceWithDefaultData()

    @BindValue
    val locationService: LocationService = mock(LocationService::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    /**
     * Make sure the context of the app is the right one
     */
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("ch.epfl.sdp.drone3d", appContext.packageName)
    }

    @Test
    fun supportActionBarGoesBackToMainActivity() {
        `when`(authService.hasActiveSession()).thenReturn(true)

        val imageButton = onView(
            Matchers.allOf(
                withContentDescription("Navigate up"),
                isDisplayed()
            )
        )

        imageButton.perform(click())
        Intents.intended(hasComponent(MainActivity::class.java.name))
    }

    @Test
    fun goToLoginDialogWorkWhenNotLogin() {
        `when`(authService.hasActiveSession()).thenReturn(false)

        activityRule.scenario.recreate()

        onView(withText(R.string.go_to_login)).perform(click())

        Intents.intended(
            hasComponent(hasClassName(LoginActivity::class.java.name))
        )
    }

    @Test
    fun goToSaveActivityButtonIsNotEnabledOnStart() {
        `when`(authService.hasActiveSession()).thenReturn(false)

        activityRule.scenario.recreate()
        onView(withText(R.string.no_saving_possible)).perform(click())

        onView(withId(R.id.buttonToSaveActivity))
            .check(matches(not(isEnabled())))
    }

    private fun createMission() {
        var mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mUiDevice.wait(Until.hasObject(By.desc("MAP READY")), 1000L)

        var hasBeenInit = false

        activityRule.scenario.onActivity {
            if (!hasBeenInit) {
                MapboxUtility.zoomOnCoordinate(
                    LatLng(46.518732896473644, 6.5628454889064365),
                    it.mapboxMap
                )
                it.areaBuilder.addVertex(LatLng(46.518732896473644, 6.5628454889064365))
                it.areaBuilder.addVertex(LatLng(46.51874120200868, 6.563415458311842))
                it.areaBuilder.addVertex(LatLng(46.518398828344715, 6.563442280401509))
                hasBeenInit = true
            }

        }
    }

    @Test
    fun buildButtonIsActivatedWhenAreaIsComplete() {
        `when`(authService.hasActiveSession()).thenReturn(true)

        activityRule.scenario.recreate()

        onView(withId(R.id.buildFlightPath))
            .check(matches(not(isEnabled())))

        createMission()

        onView(withId(R.id.buildFlightPath))
            .check(matches(isEnabled()))
    }

    @Test
    fun saveButtonIsActivatedWhenAreaIsComplete() {
        `when`(authService.hasActiveSession()).thenReturn(true)

        activityRule.scenario.recreate()

        onView(withId(R.id.buttonToSaveActivity))
            .check(matches(not(isEnabled())))

        createMission()

        onView(withId(R.id.buttonToSaveActivity))
            .check(matches(isEnabled()))
    }

    @Test
    fun deleteButtonIsEnabledWhenThereIsSomethingToDelete() {
        `when`(authService.hasActiveSession()).thenReturn(true)

        activityRule.scenario.recreate()

        onView(withId(R.id.delete_button))
            .check(matches(not(isEnabled())))

        var mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mUiDevice.wait(Until.hasObject(By.desc("MAP READY")), 1000L)
        onView(withId(R.id.mapView)).perform(click())
        SystemClock.sleep(100L)


        onView(withId(R.id.delete_button))
            .check(matches(isEnabled()))

        onView(withId(R.id.delete_button))
            .perform(click())

        onView(withId(R.id.delete_button))
            .check(matches(not(isEnabled())))
    }

    @Test
    fun changeFlightPathVisibilityButtonIsClickable() {
        `when`(authService.hasActiveSession()).thenReturn(true)

        activityRule.scenario.recreate()

        onView(withId(R.id.showMission))
            .check(matches(isEnabled()))
        onView(withId(R.id.showMission)).perform(click())
        onView(withId(R.id.showMission))
            .check(matches(isEnabled()))
        onView(withId(R.id.showMission)).perform(click())
        onView(withId(R.id.showMission))
            .check(matches(isEnabled()))
    }

    @Test
    fun switchStrategyButtonIsClickable() {
        `when`(authService.hasActiveSession()).thenReturn(true)

        activityRule.scenario.recreate()

        onView(withId(R.id.changeStrategy))
            .check(matches(isEnabled()))
        onView(withId(R.id.changeStrategy)).perform(click())
        onView(withId(R.id.changeStrategy))
            .check(matches(isEnabled()))
        onView(withId(R.id.changeStrategy)).perform(click())
        onView(withId(R.id.changeStrategy))
            .check(matches(isEnabled()))
    }

    @Test
    fun buildButtonIsClickable() {
        `when`(authService.hasActiveSession()).thenReturn(true)

        activityRule.scenario.recreate()

        onView(withId(R.id.buildFlightPath))
            .check(matches(not(isEnabled())))

        createMission()

        onView(withId(R.id.buildFlightPath))
            .check(matches(isEnabled()))

        onView(withId(R.id.buildFlightPath))
            .perform(click())

        onView(withId(R.id.buildFlightPath))
            .check(matches(not(isEnabled())))
    }

    @Test
    fun goToSaveActivityWorkWhenLogin() {
        `when`(authService.hasActiveSession()).thenReturn(true)

        activityRule.scenario.recreate()

        createMission()

        onView(withId(R.id.buildFlightPath))
            .check(matches(isEnabled()))
        onView(withId(R.id.buildFlightPath))
            .perform(click())

        SystemClock.sleep(1000L)

        onView(withId(R.id.buttonToSaveActivity))
            .check(matches(isEnabled()))
        onView(withId(R.id.buttonToSaveActivity)).perform(click())

        Intents.intended(
            hasComponent(hasClassName(SaveMappingMissionActivity::class.java.name))
        )

        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra(ItineraryCreateActivity.FLIGHTHEIGHT_INTENT_PATH) })
        assert(intents.any { it.hasExtra(ItineraryCreateActivity.STRATEGY_INTENT_PATH) })
        assert(intents.any { it.hasExtra(ItineraryCreateActivity.AREA_INTENT_PATH) })
    }

    @Test
    fun goToItineraryShowWorkWhenNotLogin() {
        `when`(authService.hasActiveSession()).thenReturn(false)

        activityRule.scenario.recreate()

        onView(withText(R.string.no_saving_possible)).perform(click())

        createMission()

        onView(withId(R.id.buildFlightPath))
            .check(matches(isEnabled()))
        onView(withId(R.id.buildFlightPath))
            .perform(click())

        SystemClock.sleep(1000L)

        onView(withId(R.id.buttonToSaveActivity))
            .check(matches(isEnabled()))
        onView(withId(R.id.buttonToSaveActivity)).perform(click())

        Intents.intended(
            hasComponent(hasClassName(ItineraryShowActivity::class.java.name))
        )

        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra(MissionViewAdapter.FLIGHTHEIGHT_INTENT_PATH) })
        assert(intents.any { it.hasExtra(MissionViewAdapter.STRATEGY_INTENT_PATH) })
        assert(intents.any { it.hasExtra(MissionViewAdapter.AREA_INTENT_PATH) })
    }


    @Test
    fun saveWithoutModifyingHaveSameOutputThanInput() {
        `when`(authService.hasActiveSession()).thenReturn(true)
        activityRule.scenario.recreate()

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItineraryCreateActivity::class.java
        )
        val flightHeight = 10.0
        val area = listOf(LatLng(1.0,1.0), LatLng(0.0,0.0), LatLng(5.0,5.0))
        val strategy = MappingMissionService.Strategy.DOUBLE_PASS

        intent.putExtra(ItineraryShowActivity.FLIGHTHEIGHT_INTENT_PATH, flightHeight)
        intent.putExtra(ItineraryShowActivity.AREA_INTENT_PATH, ArrayList(area))
        intent.putExtra(ItineraryShowActivity.STRATEGY_INTENT_PATH, strategy)

        ActivityScenario.launch<Activity>(intent)

        var mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mUiDevice.wait(Until.hasObject(By.desc("MAP READY")), 1000L)

        onView(withId(R.id.buttonToSaveActivity))
            .check(matches(isEnabled()))
        onView(withId(R.id.buttonToSaveActivity))
            .perform(click())

        onView(withText(R.string.confirm_save_without_updating))
            .perform(click())

        Intents.intended(
            hasComponent(hasClassName(SaveMappingMissionActivity::class.java.name))
        )
        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra(ItineraryCreateActivity.FLIGHTHEIGHT_INTENT_PATH) })
        assert(intents.any { it.hasExtra(ItineraryCreateActivity.STRATEGY_INTENT_PATH) })
        assert(intents.any { it.hasExtra(ItineraryCreateActivity.AREA_INTENT_PATH) })

        val bundle = intents.get(1).extras!!
        assertThat(bundle.getDouble(ItineraryCreateActivity.FLIGHTHEIGHT_INTENT_PATH), equalTo(flightHeight))
        assertThat((bundle.get(ItineraryCreateActivity.STRATEGY_INTENT_PATH) as MappingMissionService.Strategy)!!, equalTo(strategy))
        assertThat(bundle.getParcelableArrayList(ItineraryCreateActivity.AREA_INTENT_PATH)!!, equalTo(area))


    }
}