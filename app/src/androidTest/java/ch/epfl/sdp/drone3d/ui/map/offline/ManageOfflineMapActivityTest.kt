/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.map.offline

import android.widget.EditText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.module.LocationModule
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.Style
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@HiltAndroidTest
@UninstallModules(LocationModule::class)
class ManageOfflineMapActivityTest {

    companion object {
        private const val TIMEOUT = 5L
    }

    private val activityRule = ActivityScenarioRule(ManageOfflineMapActivity::class.java)

    @get:Rule
    var testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val locationService: LocationService = mock(LocationService::class.java)

    init {
        `when`(locationService.isLocationEnabled()).thenReturn(true)
        `when`(locationService.getCurrentLocation()).thenReturn(LatLng(0.0, 0.0))
    }

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
    fun cannotEnterEmptyStringForRegionName() {

        Thread.sleep(2000)
        //Used to wait for the map to be downloaded before clicking the button
        val counter = CountDownLatch(1)

        activityRule.scenario.onActivity { activity ->
            activity.mapView.getMapAsync { mapboxMap ->
                mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                    counter.countDown()
                }
            }
        }

        assert(counter.await(TIMEOUT, TimeUnit.SECONDS))

        onView(withId(R.id.buttonToSaveOfflineMap))
            .perform(click())

        onView(
            allOf(
                withId(R.id.input_text),
                isAssignableFrom(EditText::class.java)
            )
        )
            .inRoot(isDialog())
            .perform(typeText(""), closeSoftKeyboard())

        onView(withContentDescription("Positive button"))
            .inRoot(isDialog())
            .perform(click())

        //Check that the dialog is still displayed
        onView(withId((R.id.input_text)))
            .check(matches(isDisplayed()))
    }
}
