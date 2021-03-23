/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d

import android.widget.ToggleButton
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.runner.RunWith

/**
 * Test for the mapping mission selection activity
 */
@RunWith(AndroidJUnit4::class)
class MappingMissionSelectionActivityTest {

    @get:Rule
    var testRule = ActivityScenarioRule(MappingMissionSelectionActivity::class.java)

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
    fun clickOnSwitchChangesPrivateOrSharedMode() {
        var initialState = false

        //Sets initial state to the current value of the toggle button
        onView(withId(R.id.mappingMissionToggleButton)).check { view, _ ->
            initialState =
                view.findViewById<ToggleButton>(R.id.mappingMissionToggleButton).isChecked
        }

        onView(withId(R.id.mappingMissionToggleButton))
            .perform(ViewActions.click())
        onView(withId(R.id.mappingMissionToggleButton))
            .check(matches(if (initialState) isNotChecked() else isChecked()))
        onView(withId(R.id.mappingMissionToggleButton))
            .perform(ViewActions.click())
        onView(withId(R.id.mappingMissionToggleButton))
            .check(matches(if (initialState) isChecked() else isNotChecked()))
    }

    @Test
    fun goToItineraryCreateWorks() {
        onView(withId(R.id.createMappingMissionButton))
            .perform(ViewActions.click())
        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(ItineraryCreateActivity::class.java.name))
        )
    }

}