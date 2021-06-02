/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.weather

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.model.weather.WeatherReport
import ch.epfl.sdp.drone3d.service.api.weather.WeatherService
import ch.epfl.sdp.drone3d.service.module.WeatherModule
import ch.epfl.sdp.drone3d.ui.map.MissionInProgressActivity
import ch.epfl.sdp.drone3d.ui.mission.ItineraryShowActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers.not
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*

/**
 * Test WeatherInfoActivity
 */
@HiltAndroidTest
@UninstallModules(WeatherModule::class)
class WeatherInfoActivityTest {
    companion object {

        private val GOOD_VISIBILITY_REPORT = WeatherReport("Clear", "description",
            "01d",20.0, 20, 5.0, 10000, Date(12903))

        private val BAD_VISIBILITY_REPORT = WeatherReport("RAIN", "description",
            "10d", -1.0, 20, 10.0, 100, Date(12903))

        private val location = LatLng(47.398979, 8.543434)

    }

    private val activityRule = ActivityScenarioRule<MissionInProgressActivity>(
        Intent(ApplicationProvider.getApplicationContext(), WeatherInfoActivity::class.java).apply {
            putExtras(
                Bundle().apply {
                    putExtra(ItineraryShowActivity.LOCATION_INTENT_PATH, location)
                }
            )
        }
    )

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val weatherService: WeatherService = Mockito.mock(WeatherService::class.java)

    init {
        `when`(weatherService.getWeatherReport(location)).thenReturn(MutableLiveData(GOOD_VISIBILITY_REPORT))
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
    fun visibilityTextAndColorCorrectWhenGoodVisibility() {
        `when`(weatherService.getWeatherReport(location)).thenReturn(MutableLiveData(GOOD_VISIBILITY_REPORT))

        activityRule.scenario.recreate()

        onView(withId(R.id.infoVisibility)).check(matches(withText("Visibility limit : 10 km")))
        onView(withId(R.id.infoVisibility)).check(matches(not(hasTextColor(R.color.red))))
    }

    @Test
    fun visibilityTextAndColorCorrectWhenBadVisibility() {
        `when`(weatherService.getWeatherReport(location)).thenReturn(MutableLiveData(BAD_VISIBILITY_REPORT))

        activityRule.scenario.recreate()

        onView(withId(R.id.infoVisibility)).check(matches(withText("Visibility limit : 100 m")))
        onView(withId(R.id.infoVisibility)).check(matches(hasTextColor(R.color.red)))
    }
}
