/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.drone

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.drone.DroneModule
import ch.epfl.sdp.drone3d.drone.DroneService
import ch.epfl.sdp.drone3d.matcher.ToastMatcher
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.mockito.Mockito.*

@HiltAndroidTest
@UninstallModules(DroneModule::class)
class DroneConnectActivityTest {

    private val activityRule = ActivityTestRule(DroneConnectActivity::class.java)

    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
            .around(activityRule)

    @BindValue
    val droneService: DroneService = DroneInstanceMock.mockService()

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
    fun connectSimulatedDroneWorks() {
        val ip = "1.1.1.1"
        val port = "1111"

        `when`(droneService.setSimulation(anyString(), anyString()))
                .then { an ->
                    assertEquals(ip, an.getArgument(0))
                    assertEquals(port, an.getArgument(1))
                }

        onView(withId(R.id.text_IP_address))
            .perform(typeText(ip))
        onView(isRoot())
            .perform(closeSoftKeyboard())
        onView(withId(R.id.text_port))
            .perform(typeText(port))
        onView(isRoot())
            .perform(closeSoftKeyboard())
        onView(withId(R.id.connect_simulation_button))
            .perform(click())

        verify(droneService).setSimulation(anyString(), anyString())
        Intents.intended(
            hasComponent(hasClassName(ConnectedDroneActivity::class.java.name))
        )
    }

    @Test
    fun connectSimulatedDroneShowToastWhenInvalidIp() {
        onView(withId(R.id.connect_simulation_button))
            .perform(click())

        ToastMatcher.onToast(activityRule.activity, R.string.ip_format_invalid)
    }

    @Test
    fun connectDroneWorks() {
        onView(withId(R.id.connect_drone_button))
            .perform(click())

        verify(droneService).setDrone()
    }
}