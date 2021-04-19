/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.content.res.Resources
import android.view.KeyEvent
import android.view.View
import android.widget.ToggleButton
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.*
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
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


/**
 * Test for the mapping mission selection activity
 */
@HiltAndroidTest
@UninstallModules(AuthenticationModule::class, MappingMissionDaoModule::class)
class MappingMissionSelectionActivityTest {

    companion object {
        private const val USER_UID = "asdfg"

        private val SHARED_MAPPING_MISSION =
            MappingMission("shared1", listOf(LatLng(10.0, 10.0), LatLng(25.0, 25.0)))
        private val PRIVATE_MAPPING_MISSION = MappingMission("private1", listOf())
        private val PRIVATE_AND_SHARED_MAPPING_MISSION =
            MappingMission("private2", listOf()).apply {
                state = State.PRIVATE_AND_SHARED
            }

        private val SHARED_LIVE_DATA = MutableLiveData(listOf(SHARED_MAPPING_MISSION))
        private val PRIVATE_LIVE_DATA = MutableLiveData(listOf(PRIVATE_MAPPING_MISSION))
        private val SHARED_FILTERED_LIVE_DATA = MutableLiveData(emptyList<MappingMission>())
        private val PRIVATE_FILTERED_LIVE_DATA = MutableLiveData(emptyList<MappingMission>())
    }

    @get:Rule
    var testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(ActivityScenarioRule(MappingMissionSelectionActivity::class.java))

    @BindValue
    val authService: AuthenticationService = mock(AuthenticationService::class.java)

    @BindValue
    val mappingMissionDao: MappingMissionDao = mock(MappingMissionDao::class.java)

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
        `when`(mappingMissionDao.getPrivateFilteredMappingMissions())
            .thenReturn(PRIVATE_FILTERED_LIVE_DATA)
        `when`(mappingMissionDao.getSharedFilteredMappingMissions())
            .thenReturn(SHARED_FILTERED_LIVE_DATA)
        `when`(
            mappingMissionDao.updatePrivateFilteredMappingMissions(
                anyString(),
                anyString()
            )
        ).thenAnswer {
            updateFilteredLiveData(false, it.getArgument(1))
        }
        `when`(mappingMissionDao.updateSharedFilteredMappingMissions(anyString())).thenAnswer {
            updateFilteredLiveData(true, it.getArgument(0))
        }
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
        onView(withId(R.id.mapping_mission_state_toggle)).check { view, _ ->
            isPrivate =
                view.findViewById<ToggleButton>(R.id.mapping_mission_state_toggle).isChecked
        }

        if (!isPrivate)
            onView(withId(R.id.mapping_mission_state_toggle)).perform(click())

        onView(
            allOf(
                withText(buttonName(false, PRIVATE_MAPPING_MISSION)),
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
        onView(withId(R.id.mapping_mission_state_toggle)).check { view, _ ->
            isPrivate =
                view.findViewById<ToggleButton>(R.id.mapping_mission_state_toggle).isChecked
        }

        if (isPrivate)
            onView(withId(R.id.mapping_mission_state_toggle)).perform(click())

        onView(
            allOf(
                withText(buttonName(true, SHARED_MAPPING_MISSION)),
                isDisplayed()
            )
        ).perform(click())
        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(ItineraryShowActivity::class.java.name))
        )
    }

    @Test
    fun changeInLiveDataHasEffect() {

        // Semaphore used to wait for live data to be dispatched
        val mutex = Semaphore(0)
        // Make sure the current state is shared
        var isPrivate = false

        //Sets initial state to shared
        onView(withId(R.id.mapping_mission_state_toggle)).check { view, _ ->
            isPrivate =
                view.findViewById<ToggleButton>(R.id.mapping_mission_state_toggle).isChecked
        }

        if (isPrivate)
            onView(withId(R.id.mapping_mission_state_toggle)).perform(click())

        // Shared
        var curShared = SHARED_LIVE_DATA.value

        onView(withId(R.id.shared_mission_list_view)).check(matchCount(curShared?.size ?: 0))
        onView(withId(R.id.shared_mission_list_view)).check(
            matches(
                withEffectiveVisibility(
                    Visibility.VISIBLE
                )
            )
        )

        curShared?.forEach { m ->
            onView(withText(buttonName(true, m)))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        }

        curShared = listOf(SHARED_MAPPING_MISSION, PRIVATE_AND_SHARED_MAPPING_MISSION)

        // Post new value to live data and wait for it to be dispatched
        onView(withId(R.id.shared_mission_list_view)).perform(ReleaseOnChange(mutex))
        SHARED_LIVE_DATA.postValue(curShared)
        mutex.tryAcquire(100, TimeUnit.MILLISECONDS)

        onView(withId(R.id.shared_mission_list_view)).check(matchCount(curShared.size))
        onView(withId(R.id.shared_mission_list_view)).check(
            matches(
                withEffectiveVisibility(
                    Visibility.VISIBLE
                )
            )
        )

        //switch state
        onView(withId(R.id.mapping_mission_state_toggle)).perform(click())

        // private
        var curPrivate = PRIVATE_LIVE_DATA.value

        onView(withId(R.id.private_mission_list_view)).check(matchCount(curPrivate?.size ?: 0))
        onView(withId(R.id.private_mission_list_view)).check(
            matches(
                withEffectiveVisibility(
                    Visibility.VISIBLE
                )
            )
        )

        curPrivate?.forEach { m ->
            onView(withText(buttonName(false, m)))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        }

        curPrivate = listOf(PRIVATE_MAPPING_MISSION, PRIVATE_AND_SHARED_MAPPING_MISSION)

        // Post new value to live data and wait for it to be dispatched
        mutex.drainPermits()
        onView(withId(R.id.private_mission_list_view)).perform(ReleaseOnChange(mutex))
        PRIVATE_LIVE_DATA.postValue(curPrivate)
        mutex.tryAcquire(100, TimeUnit.MILLISECONDS)

        onView(withId(R.id.private_mission_list_view)).check(matchCount(curPrivate.size))
        onView(withId(R.id.private_mission_list_view)).check(
            matches(
                withEffectiveVisibility(
                    Visibility.VISIBLE
                )
            )
        )

        // Reset LIVE DATA
        SHARED_LIVE_DATA.postValue(listOf(SHARED_MAPPING_MISSION))
        PRIVATE_LIVE_DATA.postValue(listOf(PRIVATE_MAPPING_MISSION))
    }

    @Test
    fun searchingChangesMissionsAndAreUpdatedWithLiveData() {
        // Semaphore used to wait for live data to be dispatched
        val mutex = Semaphore(0)
        // Make sure the current state is shared
        var isPrivate = false
        // The filter that will be entered in the search bar
        var filter = "filter"

        //Sets initial state to shared
        onView(withId(R.id.mapping_mission_state_toggle)).check { view, _ ->
            isPrivate =
                view.findViewById<ToggleButton>(R.id.mapping_mission_state_toggle).isChecked
        }

        if (isPrivate)
            onView(withId(R.id.mapping_mission_state_toggle)).perform(click())

        // Update live data and wait for it to be dispatched
        onView(withId(R.id.searchView)).perform(
            click(),
            typeText(filter),
            pressKey(KeyEvent.KEYCODE_ENTER)
        )

        var currentData = SHARED_FILTERED_LIVE_DATA.value

        liveDataShowsToUser(true, true, currentData)

        currentData =
            listOf(MappingMission("shared filtered 1"), MappingMission("shared filtered 2"))

        // Post new value to live data and wait for it to be dispatched
        onView(withId(R.id.shared_filtered_mission_list_view)).perform(ReleaseOnChange(mutex))
        SHARED_FILTERED_LIVE_DATA.postValue(currentData)
        mutex.tryAcquire(100, TimeUnit.MILLISECONDS)


        liveDataShowsToUser(true, true, currentData)

        //switch state
        onView(withId(R.id.mapping_mission_state_toggle)).perform(click())

        // private
        currentData = PRIVATE_FILTERED_LIVE_DATA.value


        liveDataShowsToUser(false, true, currentData)

        currentData =
            listOf(MappingMission("private filtered 1"), MappingMission("private filtered 2"))

        // Post new value to live data and wait for it to be dispatched
        mutex.drainPermits()
        onView(withId(R.id.private_filtered_mission_list_view)).perform(ReleaseOnChange(mutex))
        PRIVATE_FILTERED_LIVE_DATA.postValue(currentData)
        mutex.tryAcquire(100, TimeUnit.MILLISECONDS)

        liveDataShowsToUser(false, true, currentData)

        // Reset LIVE DATA
        SHARED_FILTERED_LIVE_DATA.postValue(emptyList())
        PRIVATE_FILTERED_LIVE_DATA.postValue(emptyList())
    }

    @Test
    fun clearTextInSearchBarShowsMissionWithoutFilter() {
        // Make sure the current state is shared
        var isPrivate = false
        // The filter that will be entered in the search bar
        var filter = "filter"

        //Sets initial state to shared
        onView(withId(R.id.mapping_mission_state_toggle)).check { view, _ ->
            isPrivate =
                view.findViewById<ToggleButton>(R.id.mapping_mission_state_toggle).isChecked
        }

        if (isPrivate)
            onView(withId(R.id.mapping_mission_state_toggle)).perform(click())

        // Launch a search, update live data and wait for it to be dispatched
        onView(withId(R.id.searchView)).perform(
            click(),
            typeText(filter),
            pressKey(KeyEvent.KEYCODE_ENTER)
        )

        var currentData = SHARED_FILTERED_LIVE_DATA.value


        liveDataShowsToUser(true, true, currentData)

        //clear search
        onView(
            withId(
                Resources.getSystem().getIdentifier(
                    "search_close_btn",
                    "id", "android"
                )
            )
        ).perform(click())

        //now all shared missions are visible
        currentData = SHARED_LIVE_DATA.value


        liveDataShowsToUser(true, false, currentData)

        //switch state to private
        onView(withId(R.id.mapping_mission_state_toggle)).perform(click())

        // Launch a search, update live data and wait for it to be dispatched
        onView(withId(R.id.searchView)).perform(
            click(),
            typeText(filter),
            pressKey(KeyEvent.KEYCODE_ENTER)
        )

        currentData = PRIVATE_FILTERED_LIVE_DATA.value


        liveDataShowsToUser(false, true, currentData)

        //clear search
        onView(
            withId(
                Resources.getSystem().getIdentifier(
                    "search_close_btn",
                    "id", "android"
                )
            )
        ).perform(click())

        //now all shared missions are visible
        currentData = PRIVATE_LIVE_DATA.value

        liveDataShowsToUser(false, false, currentData)

        // Reset LIVE DATA
        SHARED_FILTERED_LIVE_DATA.postValue(emptyList())
        PRIVATE_FILTERED_LIVE_DATA.postValue(emptyList())
    }

    private class ReleaseOnChange(private val mutex: Semaphore) : ViewAction {

        override fun getConstraints(): Matcher<View> =
            allOf(isDisplayed(), isAssignableFrom(RecyclerView::class.java))

        override fun getDescription(): String =
            "Wait for an updated to be dispatched and then release the given mutex"

        override fun perform(uiController: UiController?, view: View?) {
            val recyclerView = view as RecyclerView
            val adapter = recyclerView.adapter as ListAdapter<*, *>
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() =
                    mutex.release()

                override fun onItemRangeChanged(positionStart: Int, itemCount: Int) =
                    mutex.release()

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) =
                    mutex.release()

                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) =
                    mutex.release()

                override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) =
                    mutex.release()
            }
            )
        }
    }

    private fun liveDataShowsToUser(
        shared: Boolean,
        filter: Boolean,
        currentData: List<MappingMission>?
    ) {
        val id =
            if (shared && filter) R.id.shared_filtered_mission_list_view
            else if (shared && !filter) R.id.shared_mission_list_view
            else if (!shared && filter) R.id.private_filtered_mission_list_view
            else R.id.private_mission_list_view

        onView(withId(id)).check(matchCount(currentData?.size ?: 0))
        onView(withId(id)).check(
            matches(
                withEffectiveVisibility(
                    Visibility.VISIBLE
                )
            )
        )

        currentData?.forEach { m ->
            onView(withText(buttonName(shared, m)))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        }
    }

    private fun updateFilteredLiveData(shared: Boolean, filter: String) {
        (if (shared) SHARED_FILTERED_LIVE_DATA else PRIVATE_FILTERED_LIVE_DATA).postValue(
            listOf(
                MappingMission("mission $filter $shared")
            )
        )
    }

    private fun buttonName(shared: Boolean, m: MappingMission): String =
        if (m.state == State.PRIVATE_AND_SHARED)
            if (shared)
                m.name + " - P"
            else
                m.name + " - S"
        else m.name

    private fun matchCount(expectedCount: Int): ViewAssertion =
        ViewAssertion { view, noViewFoundException ->
            if (noViewFoundException != null) {
                throw noViewFoundException
            }
            val recyclerView = view as RecyclerView
            val adapter = recyclerView.adapter
            assertThat(adapter!!.itemCount, `is`(expectedCount))
        }
}