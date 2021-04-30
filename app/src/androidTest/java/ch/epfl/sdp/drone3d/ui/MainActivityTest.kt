/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui

import androidx.lifecycle.MutableLiveData
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
import ch.epfl.sdp.drone3d.service.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.service.module.DroneModule
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.module.AuthenticationModule
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.model.auth.UserSession
import ch.epfl.sdp.drone3d.model.mission.MappingMission
import ch.epfl.sdp.drone3d.service.api.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.service.module.MappingMissionDaoModule
import ch.epfl.sdp.drone3d.ui.auth.LoginActivity
import ch.epfl.sdp.drone3d.ui.drone.ConnectedDroneActivity
import ch.epfl.sdp.drone3d.ui.drone.DroneConnectActivity
import ch.epfl.sdp.drone3d.ui.mission.ItineraryCreateActivity
import ch.epfl.sdp.drone3d.ui.mission.MappingMissionSelectionActivity
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito.*

@HiltAndroidTest
@UninstallModules(AuthenticationModule::class, MappingMissionDaoModule::class, DroneModule::class)
class MainActivityTest {

    private val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
            .around(activityRule)

    @BindValue val authService: AuthenticationService = mock(AuthenticationService::class.java)
    @BindValue val mappingMissionDao: MappingMissionDao = mock(MappingMissionDao::class.java)
    @BindValue val droneService: DroneService = DroneInstanceMock.mockService()

    init {
        // Default mocks needed at creation of the activity
        `when`(authService.hasActiveSession()).thenReturn(false)

        val liveData = MutableLiveData(listOf(MappingMission("name", listOf())))
        `when`(mappingMissionDao.getPrivateMappingMissions(anyString())).thenReturn(liveData)
        `when`(mappingMissionDao.getSharedMappingMissions()).thenReturn(liveData)
        `when`(mappingMissionDao.getPrivateFilteredMappingMissions()).thenReturn(liveData)
        `when`(mappingMissionDao.getSharedFilteredMappingMissions()).thenReturn(liveData)
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
    fun goToLoginWorks() {
        `when`(authService.hasActiveSession()).thenReturn(false)

        // Recreate the activity to apply the update
        activityRule.scenario.recreate()

        onView(withId(R.id.log_in_button))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.log_in_button)).perform(click())
        Intents.intended(
                hasComponent(hasClassName(LoginActivity::class.java.name))
        )
    }

    @Test
    fun logoutWorks() {
        `when`(authService.hasActiveSession()).thenReturn(true)
        `when`(authService.signOut()).thenAnswer{
            `when`(authService.hasActiveSession()).thenReturn(false)
        }

        // Recreate the activity to apply the update
        activityRule.scenario.recreate()

        onView(withId(R.id.log_out_button))
                    .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.log_out_button)).perform(click())
        onView(withId(R.id.log_in_button))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        verify(authService).signOut()
    }

    @Test
    fun goToItineraryCreateWorks() {
        onView(withId(R.id.create_itinerary_button))
                .perform(click())
        Intents.intended(
                hasComponent(hasClassName(ItineraryCreateActivity::class.java.name))
        )
    }

    @Test
    fun goToMappingMissionSelectionWorksWhenActiveSession() {
        val user = mock(FirebaseUser::class.java)

        `when`(user.uid).thenReturn("id")
        `when`(authService.hasActiveSession()).thenReturn(true)
        `when`(authService.getCurrentSession()).thenReturn(UserSession(user))

        onView(withId(R.id.browse_itinerary_button))
                .perform(click())
        Intents.intended(
                hasComponent(hasClassName(MappingMissionSelectionActivity::class.java.name))
        )
    }

    @Test
    fun goToMappingMissionSelectionWorksWithoutActiveSession() {
        `when`(authService.hasActiveSession()).thenReturn(false)

        // Refresh
        onView(withId(R.id.browse_itinerary_button))
                .perform(click())
        Intents.intended(
                hasComponent(hasClassName(MappingMissionSelectionActivity::class.java.name))
        )
    }

    @Test
    fun goToDroneConnectWorks() {
        `when`(droneService.isConnected()).thenReturn(false)

        // Refresh
        activityRule.scenario.recreate()

        onView(withId(R.id.go_connect_drone_button)).perform(click())
        Intents.intended(
                hasComponent(hasClassName(DroneConnectActivity::class.java.name))
        )
    }

    @Test
    fun goToDroneDisconnectWorks() {
        `when`(droneService.isConnected()).thenReturn(true)

        activityRule.scenario.recreate()

        onView(withId(R.id.go_disconnect_drone_button)).perform(click())
        Intents.intended(
                hasComponent(hasClassName(ConnectedDroneActivity::class.java.name))
        )
    }
}