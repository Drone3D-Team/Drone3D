/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.drone

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.drone.DroneProvider
import ch.epfl.sdp.drone3d.drone.DroneProviderModule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.RuleChain
import org.mockito.Mockito.verify

@HiltAndroidTest
@UninstallModules(DroneProviderModule::class)
class ConnectedDroneActivityTest {

    @get:Rule
    val activityRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
            .around(ActivityScenarioRule(ConnectedDroneActivity::class.java))

    @BindValue
    val droneProvider: DroneProvider = DroneInstanceMock.mockProvider

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
        assertEquals("ch.epfl.sdp.drone3d", appContext.packageName)
    }

    @Test
    fun disconnectDroneWorks() {
        onView(ViewMatchers.withId(R.id.disconnect_simulation))
            .perform(ViewActions.click())

        verify(droneProvider).disconnect()

        Intents.intended(
            hasComponent(hasClassName(DroneConnectActivity::class.java.name))
        )
    }
}