/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.drone.DroneModule
import ch.epfl.sdp.drone3d.drone.DroneService
import ch.epfl.sdp.drone3d.ui.map.MissionInProgressActivity
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito.`when`

@HiltAndroidTest
@UninstallModules(DroneModule::class)
class ItineraryShowActivityTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(ItineraryShowActivity::class.java)

    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val droneService: DroneService = DroneInstanceMock.mockServiceWithDefaultData()

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
    fun goToMissionInProgressActivityButtonIsNotEnabledWhenDroneIsNotConnected() {
        `when`(droneService.isConnected()).thenReturn(false)

        activityRule.scenario.recreate()

        onView(ViewMatchers.withId(R.id.buttonToMissionInProgressActivity))
                .check(matches(Matchers.not(ViewMatchers.isEnabled())))
    }

    @Test
    fun goToMissionInProgressActivityWork() {
        `when`(droneService.isConnected()).thenReturn(true)

        activityRule.scenario.recreate()

        onView(ViewMatchers.withId(R.id.buttonToMissionInProgressActivity))
            .check(matches(ViewMatchers.isEnabled()))
        onView(ViewMatchers.withId(R.id.buttonToMissionInProgressActivity)).perform(ViewActions.click())

        Intents.intended(
            IntentMatchers.hasComponent(
                ComponentNameMatchers.hasClassName(
                    MissionInProgressActivity::class.java.name
                )
            )
        )

        val intents = Intents.getIntents()
        assert(intents.any{it.hasExtra(MissionViewAdapter.MISSION_PATH)})
    }
}