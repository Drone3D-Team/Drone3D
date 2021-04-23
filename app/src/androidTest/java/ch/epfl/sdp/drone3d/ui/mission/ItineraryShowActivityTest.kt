/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.drone.DroneModule
import ch.epfl.sdp.drone3d.drone.api.DroneService
import ch.epfl.sdp.drone3d.service.auth.AuthenticationModule
import ch.epfl.sdp.drone3d.service.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.auth.UserSession
import ch.epfl.sdp.drone3d.ui.map.MissionInProgressActivity
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito
import org.mockito.Mockito.`when`

@HiltAndroidTest
@UninstallModules(DroneModule::class, AuthenticationModule::class)
class ItineraryShowActivityTest {

    private val USER_UID = "asdfg"

    private var activityRule = ActivityScenarioRule(ItineraryShowActivity::class.java)

    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val droneService: DroneService = DroneInstanceMock.mockServiceWithDefaultData()
    @BindValue
    val authService: AuthenticationService = Mockito.mock(AuthenticationService::class.java)


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

        onView(withId(R.id.buttonToMissionInProgressActivity))
                .check(matches(Matchers.not(isEnabled())))
    }

    @Test
    fun goToMissionInProgressActivityWork() {
        `when`(droneService.isConnected()).thenReturn(true)

        activityRule.scenario.recreate()

        onView(withId(R.id.buttonToMissionInProgressActivity))
            .check(matches(isEnabled()))
        onView(withId(R.id.buttonToMissionInProgressActivity)).perform(click())

        Intents.intended(
            hasComponent(hasClassName(MissionInProgressActivity::class.java.name))
        )

        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra(MissionViewAdapter.MISSION_PATH) })
    }

    @Test
    fun deleteMissionBringBackToMissionSelection() {
        val user = Mockito.mock(FirebaseUser::class.java)
        `when`(user.uid).thenReturn(USER_UID)
        val userSession = UserSession(user)
        `when`(authService.getCurrentSession()).thenReturn(userSession)

        onView(withId(R.id.mission_delete))
            .perform(click())
        onView(withText("Are you sure you want to delete this mission ?"))
            .check(matches(isDisplayed()))
        onView(withText("Delete"))
            .perform(click())
        Intents.intended(
            hasComponent(hasClassName(MappingMissionSelectionActivity::class.java.name))
        )
    }
}