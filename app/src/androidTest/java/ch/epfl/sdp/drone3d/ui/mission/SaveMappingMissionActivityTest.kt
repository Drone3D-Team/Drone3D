/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.model.auth.UserSession
import ch.epfl.sdp.drone3d.model.mission.MappingMission
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.api.mission.MappingMissionService
import ch.epfl.sdp.drone3d.service.api.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.service.module.AuthenticationModule
import ch.epfl.sdp.drone3d.service.module.MappingMissionDaoModule
import com.google.firebase.auth.FirebaseUser
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito
import org.mockito.Mockito.*

@HiltAndroidTest
@UninstallModules(AuthenticationModule::class, MappingMissionDaoModule::class)
class SaveMappingMissionActivityTest {

    companion object {
        private const val USER_UID = "user_id"
    }

    private val activityRule = ActivityScenarioRule<SaveMappingMissionActivity>(
        Intent(
            ApplicationProvider.getApplicationContext(),
            SaveMappingMissionActivity::class.java
        ).apply {
            putExtras(Bundle().apply {
                putSerializable(ItineraryCreateActivity.FLIGHTHEIGHT_INTENT_PATH, 0.0)
                putSerializable(
                    ItineraryCreateActivity.STRATEGY_INTENT_PATH,
                    MappingMissionService.Strategy.SINGLE_PASS
                )
                putSerializable(ItineraryCreateActivity.AREA_INTENT_PATH, arrayListOf<LatLng>())
            })
        }
    )

    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val authService: AuthenticationService = mockAuthenticationService()

    @BindValue
    val mappingMissionDao: MappingMissionDao = mockMappingMissionDao()

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    private fun mockMappingMissionDao(): MappingMissionDao {
        val mappingMissionDao = mock(MappingMissionDao::class.java)

        `when`(
            mappingMissionDao.shareMappingMission(
                anyString(),
                any(MappingMission::class.java)
            )
        ).thenReturn(MutableLiveData(true))
        `when`(
            mappingMissionDao.storeMappingMission(
                anyString(),
                any(MappingMission::class.java)
            )
        ).thenReturn(MutableLiveData(true))

        return mappingMissionDao
    }

    private fun mockAuthenticationService(): AuthenticationService {
        val authService = mock(AuthenticationService::class.java)

        val user = mock(FirebaseUser::class.java)
        `when`(user.uid).thenReturn(USER_UID)
        val userSession = UserSession(user)
        `when`(authService.getCurrentSession()).thenReturn(userSession)

        return authService
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
    fun saveMappingMissionToPrivateCallStore() {
        activityRule.scenario.recreate()

        val expectedMappingMission = MappingMission(
            "Unnamed mission"
        )

        onView(withId(R.id.privateCheckBox)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        assertThat(
            activityRule.scenario.state.toString(),
            equalTo(Lifecycle.State.DESTROYED.toString())
        )

        verify(mappingMissionDao, times(1)).storeMappingMission(USER_UID, expectedMappingMission)
    }

    @Test
    fun saveMappingMissionToShareCallShare() {
        activityRule.scenario.recreate()

        val expectedMappingMission = MappingMission("Unnamed mission")

        onView(withId(R.id.sharedCheckBox)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        assertThat(
            activityRule.scenario.state.toString(),
            equalTo(Lifecycle.State.DESTROYED.toString())
        )

        verify(mappingMissionDao, times(1)).shareMappingMission(USER_UID, expectedMappingMission)
    }

    @Test
    fun saveMappingMissionToShareAndPrivateCallShareAndStore() {
        activityRule.scenario.recreate()

        val expectedMappingMission = MappingMission("Unnamed mission")

        onView(withId(R.id.sharedCheckBox)).perform(click())
        onView(withId(R.id.privateCheckBox)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        assertThat(
            activityRule.scenario.state.toString(),
            equalTo(Lifecycle.State.DESTROYED.toString())
        )

        verify(mappingMissionDao, times(1)).shareMappingMission(USER_UID, expectedMappingMission)
        verify(mappingMissionDao, times(1)).storeMappingMission(USER_UID, expectedMappingMission)
    }

    @Test
    fun nameIsCorrect() {
        activityRule.scenario.recreate()

        val name = "My mission"

        val expectedMappingMission = MappingMission(name)

        onView(withId(R.id.missionName)).perform(click()).perform(
            typeText(
                name
            )
        ).perform(closeSoftKeyboard())

        onView(withId(R.id.privateCheckBox)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        assertThat(
            activityRule.scenario.state.toString(),
            equalTo(Lifecycle.State.DESTROYED.toString())
        )

        verify(mappingMissionDao, times(1)).storeMappingMission(USER_UID, expectedMappingMission)
    }


    @Test
    fun checkBoxesEnableSaveButton() {
        activityRule.scenario.recreate()

        onView(withId(R.id.privateCheckBox))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.sharedCheckBox))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.saveButton))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.saveButton))
            .check(matches(not(isEnabled())))


        // Check private box
        onView(withId(R.id.privateCheckBox)).perform(click())
        onView(withId(R.id.saveButton))
            .check(matches(isEnabled()))

        // Uncheck private box
        onView(withId(R.id.privateCheckBox)).perform(click())
        onView(withId(R.id.saveButton))
            .check(matches(not(isEnabled())))

        // Check share box
        onView(withId(R.id.sharedCheckBox)).perform(click())
        onView(withId(R.id.saveButton))
            .check(matches(isEnabled()))

        // Uncheck share box
        onView(withId(R.id.sharedCheckBox)).perform(click())
        onView(withId(R.id.saveButton))
            .check(matches(not(isEnabled())))

        // Check both boxes
        onView(withId(R.id.privateCheckBox)).perform(click())
        onView(withId(R.id.sharedCheckBox)).perform(click())
        onView(withId(R.id.saveButton))
            .check(matches(isEnabled()))

        // Uncheck share box
        onView(withId(R.id.sharedCheckBox)).perform(click())
        onView(withId(R.id.saveButton))
            .check(matches(isEnabled()))

        // Uncheck private box
        onView(withId(R.id.privateCheckBox)).perform(click())
        onView(withId(R.id.saveButton))
            .check(matches(not(isEnabled())))
    }
}