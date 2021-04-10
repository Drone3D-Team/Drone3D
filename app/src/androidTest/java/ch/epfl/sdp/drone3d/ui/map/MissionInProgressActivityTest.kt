/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.map

import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.matcher.ToastMatcher
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.*
import org.junit.rules.RuleChain
import java.util.concurrent.CompletableFuture

@HiltAndroidTest
class MissionInProgressActivityTest {

    private val activityRule = ActivityScenarioRule(MissionInProgressActivity::class.java)

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(HiltAndroidRule(this)).around(activityRule)

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
        // Test that the toast is displayed
        val activity = CompletableFuture<MissionInProgressActivity>()
        activityRule.scenario.onActivity {
            activity.complete(it)
        }
        ToastMatcher.onToast(activity.get(), R.string.lost_connection_message)
    }
}