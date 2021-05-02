/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission


import android.app.Activity
import android.os.SystemClock
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
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
import ch.epfl.sdp.drone3d.service.module.AuthenticationModule
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.api.drone.DroneData
import ch.epfl.sdp.drone3d.service.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.service.impl.mission.ParallelogramMappingMissionService
import ch.epfl.sdp.drone3d.service.mission.ParallelogramMappingMissionServiceTest
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

    companion object {
        val droneService = DroneInstanceMock.mockService()
        val cameraResolution = MutableLiveData(DroneData.CameraResolution(200, 200))
        val focalLength = MutableLiveData(4f)
        val sensorSize = MutableLiveData(DroneData.SensorSize(2f, 2f))
    }

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
        `when`(ParallelogramMappingMissionServiceTest.droneService.getData().getCameraResolution()).thenReturn(
            cameraResolution
        )
        `when`(ParallelogramMappingMissionServiceTest.droneService.getData().getSensorSize()).thenReturn(
            sensorSize
        )
        `when`(ParallelogramMappingMissionServiceTest.droneService.getData().getFocalLength()).thenReturn(
            focalLength
        )
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
    fun goToSaveActivityButtonIsNotEnabledOnStart() {
        `when`(authService.hasActiveSession()).thenReturn(false)

        activityRule.scenario.recreate()

        onView(withId(R.id.buttonToSaveActivity))
            .check(matches(not(isEnabled())))
    }

    private fun createMission(){
        var mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mUiDevice.wait(Until.hasObject(By.desc("MAP READY")), 1000L)

        val sleepingTime = 100L
        for(i in 0..10){
            onView(withId(R.id.mapView)).perform(doubleClick())
        }

        SystemClock.sleep(sleepingTime);
        onView(withId(R.id.mapView)).perform(click())
        SystemClock.sleep(sleepingTime);
        onView(withId(R.id.mapView)).perform(swipeLeft())
        SystemClock.sleep(sleepingTime);
        onView(withId(R.id.mapView)).perform(click())
        SystemClock.sleep(sleepingTime);
        onView(withId(R.id.mapView)).perform(swipeDown())
        SystemClock.sleep(sleepingTime);
        onView(withId(R.id.mapView)).perform(click())
        SystemClock.sleep(sleepingTime);
    }

    @Test
    fun buildButtonIsActivatedWhenAreaIsComplete() {
        activityRule.scenario.recreate()

        onView(withId(R.id.buildFlightPath))
            .check(matches(not(isEnabled())))

        createMission()

        onView(withId(R.id.buildFlightPath))
            .check(matches(isEnabled()))
    }

    @Test
    fun deleteButtonIsEnabledWhenThereIsSomethingToDelete() {
        activityRule.scenario.recreate()

        onView(withId(R.id.delete_button))
            .check(matches(not(isEnabled())))

        var mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mUiDevice.wait(Until.hasObject(By.desc("MAP READY")), 1000L)
        onView(withId(R.id.mapView)).perform(click())
        SystemClock.sleep(100L);


        onView(withId(R.id.delete_button))
            .check(matches(isEnabled()))

        onView(withId(R.id.delete_button))
            .perform(click())

        onView(withId(R.id.delete_button))
            .check(matches(not(isEnabled())))
    }

    @Test
    fun changeFlightPathVisibilityButtonIsClickable() {
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


}