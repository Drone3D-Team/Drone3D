/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.map

import android.app.Activity
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.drone.DroneData
import ch.epfl.sdp.drone3d.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.matcher.ToastMatcher
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.*
import java.util.concurrent.CompletableFuture

@HiltAndroidTest
class MissionInProgressActivityTest {

    @get:Rule
    val intentsTestRule = ActivityScenarioRule(MissionInProgressActivity::class.java)

    @Before
    fun before() {
        DroneInstanceMock.setupDefaultMocks()
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
    fun loosingDroneConnectionShowsToast() {
        val droneData = DroneData(DroneInstanceMock.mockProvider)

        droneData.isFlying.value = true
        droneData.isConnected.value = false

        // Test that the toast is displayed
        testToast { activity -> ToastMatcher.onToast(activity, R.string.lost_connection_message) }
    }

    private fun testToast(matcher: (Activity) -> ViewInteraction) {
        val activity = CompletableFuture<Activity>()
        intentsTestRule.scenario.onActivity {
            activity.complete(it)
        }

        matcher.invoke(activity.get()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

}