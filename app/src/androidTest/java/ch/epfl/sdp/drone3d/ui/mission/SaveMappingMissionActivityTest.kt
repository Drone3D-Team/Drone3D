package ch.epfl.sdp.drone3d.ui.mission

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.SaveMappingMissionActivity
import ch.epfl.sdp.drone3d.service.auth.AuthenticationModule
import ch.epfl.sdp.drone3d.service.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.auth.UserSession
import ch.epfl.sdp.drone3d.service.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.service.storage.dao.MappingMissionDaoModule
import ch.epfl.sdp.drone3d.service.storage.data.MappingMission
import com.google.firebase.auth.FirebaseUser
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers.not
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito.*


@HiltAndroidTest
@UninstallModules(AuthenticationModule::class, MappingMissionDaoModule::class)
class SaveMappingMissionActivityTest {

    companion object {
        private const val USER_UID = "user_id"
    }

    private val activityRule = ActivityScenarioRule(SaveMappingMissionActivity::class.java)

    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val authService: AuthenticationService = mockAuthenticationService()

    @BindValue
    val mappingMissionDao: MappingMissionDao = mock(MappingMissionDao::class.java)

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

        val flightPath = arrayListOf(LatLng(10.1, 12.2), LatLng(1.1, 1.2))

        val expectedMappingMission = MappingMission(
            "Unnamed mission",
            flightPath
        )

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            SaveMappingMissionActivity::class.java
        )
        intent.putExtra("flightPath", flightPath)
        ActivityScenario.launch<Activity>(intent)

        onView(withId(R.id.privateCheckBox)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        verify(mappingMissionDao, times(1)).storeMappingMission(USER_UID, expectedMappingMission)
    }

    @Test
    fun saveMappingMissionToShareCallShare() {

        val flightPath = arrayListOf(LatLng(10.1, 12.2), LatLng(1.1, 1.2))

        val expectedMappingMission = MappingMission("Unnamed mission", flightPath)

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            SaveMappingMissionActivity::class.java
        )
        intent.putExtra("flightPath", flightPath)
        ActivityScenario.launch<Activity>(intent)

        onView(withId(R.id.sharedCheckBox)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        verify(mappingMissionDao, times(1)).shareMappingMission(USER_UID, expectedMappingMission)
    }

    @Test
    fun nameIsCorrect() {

        val flightPath = arrayListOf<LatLng>()
        val name = "My mission"

        val expectedMappingMission = MappingMission(name, flightPath)

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            SaveMappingMissionActivity::class.java
        )
        intent.putExtra("flightPath", flightPath)
        ActivityScenario.launch<Activity>(intent)

        onView(withId(R.id.missionName)).perform(click()).perform(
            typeText(
                name
            )
        ).perform(closeSoftKeyboard())

        onView(withId(R.id.privateCheckBox)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        verify(mappingMissionDao, times(1)).storeMappingMission(USER_UID, expectedMappingMission)
    }


    @Test
    fun checkBoxesEnableSaveButton() {
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