/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.widget.ToggleButton
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.auth.AuthenticationModule
import ch.epfl.sdp.drone3d.service.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.auth.UserSession
import ch.epfl.sdp.drone3d.service.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.service.storage.dao.MappingMissionDaoModule
import ch.epfl.sdp.drone3d.service.storage.data.MappingMission
import ch.epfl.sdp.drone3d.service.storage.data.State
import com.google.firebase.auth.FirebaseUser
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito.*


/**
 * Test for the mapping mission selection activity
 */
@HiltAndroidTest
@UninstallModules(AuthenticationModule::class, MappingMissionDaoModule::class)
class MappingMissionSelectionActivityTest {

    companion object {
        private const val USER_UID = "asdfg"

        private val SHARED_MAPPING_MISSION = MappingMission("shared", listOf(LatLng(10.0, 10.0), LatLng(20.0, 20.0)))
        private val PRIVATE_MAPPING_MISSION = MappingMission("private1", listOf())
        private val PRIVATE_AND_SHARED_MAPPING_MISSION = MappingMission("private2", listOf()).apply {
            state = State.PRIVATE_AND_SHARED
        }

        private val SHARED_LIVE_DATA = MutableLiveData(listOf(SHARED_MAPPING_MISSION))
        private val PRIVATE_LIVE_DATA = MutableLiveData(listOf(PRIVATE_MAPPING_MISSION))
    }

    @get:Rule
    var testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(ActivityScenarioRule(MappingMissionSelectionActivity::class.java))

    @BindValue val authService: AuthenticationService = mock(AuthenticationService::class.java)
    @BindValue val mappingMissionDao: MappingMissionDao = mock(MappingMissionDao::class.java)

    init {
        // Mock authService
        val user = mock(FirebaseUser::class.java)
        `when`(user.uid).thenReturn(USER_UID)
        val userSession = UserSession(user)
        `when`(authService.getCurrentSession()).thenReturn(userSession)

        // Mock mapping mission dao
        `when`(mappingMissionDao.getPrivateMappingMissions(anyString()))
                .thenReturn(PRIVATE_LIVE_DATA)
        `when`(mappingMissionDao.getSharedMappingMissions())
                .thenReturn(SHARED_LIVE_DATA)
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
    fun clickOnSwitchChangesPrivateOrSharedMode() {
        var isPrivate = false

        //Sets initial state to shared
        onView(withId(R.id.mappingMissionToggleButton)).check { view, _ ->
            isPrivate =
                view.findViewById<ToggleButton>(R.id.mappingMissionToggleButton).isChecked
        }

        if (isPrivate)
            onView(withId(R.id.mappingMissionToggleButton)).perform(click())

        // Shared state
        onView(withId(R.id.mappingMissionToggleButton))
            .check(matches(isNotChecked()))

        val curShared = SHARED_LIVE_DATA.value
        onView(withId(R.id.mappingMissionList))
                .check(matches(hasChildCount(curShared?.size ?: 0)))

        curShared?.forEach {
            m -> onView(withId(R.id.mappingMissionList))
                .check(matches(withChild(withText(buttonName(true, m)))))
        }

        // Check the box
        onView(withId(R.id.mappingMissionToggleButton))
            .perform(click())

        // Private state
        onView(withId(R.id.mappingMissionToggleButton))
            .check(matches(isChecked()))

        val curPrivate = PRIVATE_LIVE_DATA.value
        onView(withId(R.id.mappingMissionList))
                .check(matches(hasChildCount(curPrivate?.size ?: 0)))

        curPrivate?.forEach {
            m -> onView(withId(R.id.mappingMissionList))
                .check(matches(withChild(withText(buttonName(false, m)))))
        }
    }

    @Test
    fun goToItineraryCreateWorks() {

        onView(withId(R.id.createMappingMissionButton))
            .perform(click())
        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(ItineraryCreateActivity::class.java.name))
        )
    }

    @Test
    fun goToShowItineraryWorksWithPrivateButton() {

        // Make sure the current state is shared
        var isPrivate = false

        //Sets initial state to private
        onView(withId(R.id.mappingMissionToggleButton)).check { view, _ ->
            isPrivate =
                view.findViewById<ToggleButton>(R.id.mappingMissionToggleButton).isChecked
        }

        if (!isPrivate)
            onView(withId(R.id.mappingMissionToggleButton)).perform(click())

        onView(
            allOf(
                withTagValue(`is`("button private1" as Any)),
                isDisplayed()
            )
        ).perform(click())
        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(ItineraryShowActivity::class.java.name))
        )

    }

    @Test
    fun goToShowItineraryWorksWithSharedButton() {

        // Make sure the current state is shared
        var isPrivate = false

        //Sets initial state to shared
        onView(withId(R.id.mappingMissionToggleButton)).check { view, _ ->
            isPrivate =
                view.findViewById<ToggleButton>(R.id.mappingMissionToggleButton).isChecked
        }

        if (isPrivate)
            onView(withId(R.id.mappingMissionToggleButton)).perform(click())

        onView(
            allOf(
                withTagValue(`is`("button shared" as Any)),
                isDisplayed()
            )
        ).perform(click())
        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(ItineraryShowActivity::class.java.name))
        )
    }

    @Test
    fun changeInLiveDataHasEffect() {
        // Make sure the current state is shared
        var isPrivate = false

        //Sets initial state to shared
        onView(withId(R.id.mappingMissionToggleButton)).check { view, _ ->
            isPrivate =
                    view.findViewById<ToggleButton>(R.id.mappingMissionToggleButton).isChecked
        }

        if (isPrivate)
            onView(withId(R.id.mappingMissionToggleButton)).perform(click())

        // Shared
        var curShared = SHARED_LIVE_DATA.value
        onView(withId(R.id.mappingMissionList))
                .check(matches(hasChildCount(curShared?.size ?: 0)))

        curShared?.forEach {
            m -> onView(withId(R.id.mappingMissionList))
                .check(matches(withChild(withText(buttonName(true, m)))))
        }

        curShared = listOf(SHARED_MAPPING_MISSION, PRIVATE_AND_SHARED_MAPPING_MISSION)
        SHARED_LIVE_DATA.postValue(curShared)

        onView(withId(R.id.mappingMissionList))
                .check(matches(hasChildCount(curShared.size)))

        curShared.forEach {
            m -> onView(withId(R.id.mappingMissionList))
                .check(matches(withChild(withText(buttonName(true, m)))))
        }

        //switch state
        onView(withId(R.id.mappingMissionToggleButton)).perform(click())

        // private
        var curPrivate = PRIVATE_LIVE_DATA.value
        onView(withId(R.id.mappingMissionList))
                .check(matches(hasChildCount(curPrivate?.size ?: 0)))

        curPrivate?.forEach {
            m -> onView(withId(R.id.mappingMissionList))
                .check(matches(withChild(withText(buttonName(false, m)))))
        }

        curPrivate = listOf(PRIVATE_MAPPING_MISSION, PRIVATE_AND_SHARED_MAPPING_MISSION)

        PRIVATE_LIVE_DATA.postValue(curPrivate)
        onView(withId(R.id.mappingMissionList))
                .check(matches(hasChildCount(curPrivate.size)))

        curPrivate.forEach {
            m -> onView(withId(R.id.mappingMissionList))
                .check(matches(withChild(withText(buttonName(false, m)))))
        }

        // Reset LIVE DATA
        SHARED_LIVE_DATA.postValue(listOf(SHARED_MAPPING_MISSION))
        PRIVATE_LIVE_DATA.postValue(listOf(PRIVATE_MAPPING_MISSION))
    }

    private fun buttonName(shared: Boolean, m: MappingMission): String {
        return if (!shared && m.state == State.PRIVATE_AND_SHARED) m.name + "- S"
        else m.name
    }
}