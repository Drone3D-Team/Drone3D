/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission


import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.matcher.ToastMatcher
import ch.epfl.sdp.drone3d.service.auth.AuthenticationModule
import ch.epfl.sdp.drone3d.service.auth.AuthenticationService
import ch.epfl.sdp.drone3d.ui.MainActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.hamcrest.Matchers.not
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock


@HiltAndroidTest
@UninstallModules(AuthenticationModule::class)
class ItineraryCreateActivityTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(ItineraryCreateActivity::class.java)

    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val authService: AuthenticationService = mock(AuthenticationService::class.java)

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
    fun sendLocationPermissionAllowedEnablesLocation() {
        var locationEnabled = true

        activityRule.scenario.onActivity {
            it.onRequestPermissionsResult(
                0,
                arrayOf("android.permission.ACCESS_FINE_LOCATION"),
                intArrayOf(0)
            )
            locationEnabled =
                it.locationComponentManager.mapboxMap.locationComponent.isLocationComponentEnabled
        }
        Assert.assertTrue(locationEnabled)
    }

    @Test
    fun onExplanationNeededShowsToast() {
        lateinit var activity: Activity
        activityRule.scenario.onActivity {
            activity = it
            it.locationComponentManager.onExplanationNeeded(mutableListOf("android.permission.ACCESS_FINE_LOCATION"))
        }
        ToastMatcher.onToast(activity, R.string.user_location_permission_request)
            .check(matches(isDisplayed()))
    }

    @Test
    fun goToSaveActivityButtonIsNotEnabledWhenUserNotLogin() {
        `when`(authService.hasActiveSession()).thenReturn(false)

        activityRule.scenario.recreate()

        onView(withId(R.id.buttonToSaveActivity))
            .check(matches(not(isEnabled())))
    }

    @Test
    fun goToSaveActivityWork() {
        `when`(authService.hasActiveSession()).thenReturn(true)

        activityRule.scenario.recreate()

        onView(withId(R.id.buttonToSaveActivity))
            .check(matches(isEnabled()))
        onView(withId(R.id.buttonToSaveActivity)).perform(click())

        Intents.intended(
            hasComponent(hasClassName(SaveMappingMissionActivity::class.java.name))
        )

        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra("flightPath") })
    }

    @Test
    fun createBasicMissionWork() {
        `when`(authService.hasActiveSession()).thenReturn(true)
        activityRule.scenario.recreate()

        var mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mUiDevice.wait(Until.hasObject(By.desc("MAP READY")), 1000L)


        for (i in 1..4) {
            onView(withId(R.id.mapView)).perform(click())
            SystemClock.sleep(1000);
        }

        onView(withId(R.id.buttonToSaveActivity))
            .check(matches(isEnabled()))
        onView(withId(R.id.buttonToSaveActivity)).perform(click())

        Intents.intended(
            hasComponent(hasClassName(SaveMappingMissionActivity::class.java.name))
        )

        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra("flightPath") && (it.extras?.getSerializable("flightPath") as List<LatLng>).size == 4 })
    }
}