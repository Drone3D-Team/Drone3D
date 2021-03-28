package ch.epfl.sdp.drone3d

import android.widget.ToggleButton
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.auth.AuthenticationModule
import ch.epfl.sdp.drone3d.auth.AuthenticationService
import ch.epfl.sdp.drone3d.auth.UserSession
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDaoModule
import ch.epfl.sdp.drone3d.storage.data.MappingMission
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

/**
 * Test for the mapping mission selection activity
 */
@HiltAndroidTest
@UninstallModules(AuthenticationModule::class, MappingMissionDaoModule::class)
class MappingMissionSelectionActivityTest {

    companion object {
        private const val USER_UID = "asdfg"
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
        val liveData = MutableLiveData(listOf(MappingMission("name", listOf())))
        `when`(mappingMissionDao.getPrivateMappingMissions(anyString())).thenReturn(liveData)
        `when`(mappingMissionDao.getSharedMappingMissions()).thenReturn(liveData)
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
        var initialState = false

        //Sets initial state to the current value of the toggle button
        onView(withId(R.id.mappingMissionToggleButton)).check { view, _ ->
            initialState =
                view.findViewById<ToggleButton>(R.id.mappingMissionToggleButton).isChecked
        }

        onView(withId(R.id.mappingMissionToggleButton))
            .perform(click())
        onView(withId(R.id.mappingMissionToggleButton))
            .check(matches(if (initialState) isNotChecked() else isChecked()))
        onView(withId(R.id.mappingMissionToggleButton))
            .perform(click())
        onView(withId(R.id.mappingMissionToggleButton))
            .check(matches(if (initialState) isChecked() else isNotChecked()))
    }

    @Test
    fun goToItineraryCreateWorks() {
        onView(withId(R.id.createMappingMissionButton))
            .perform(click())
        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(ItineraryCreateActivity::class.java.name))
        )
    }

}