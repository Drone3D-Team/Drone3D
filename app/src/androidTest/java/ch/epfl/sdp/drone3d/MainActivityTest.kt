package ch.epfl.sdp.drone3d

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.auth.AuthenticationModule
import ch.epfl.sdp.drone3d.auth.AuthenticationService
import ch.epfl.sdp.drone3d.auth.UserSession
import ch.epfl.sdp.drone3d.drone.DroneInstanceProvider.isConnected
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDaoModule
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito.*

@HiltAndroidTest
@UninstallModules(AuthenticationModule::class, MappingMissionDaoModule::class)
class MainActivityTest {

    private val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
            .around(activityRule)

    @BindValue val authService: AuthenticationService = mockAuthenticationService()
    @BindValue val mappingMissionDao: MappingMissionDao = mockMappingMissionDao()

    private fun mockAuthenticationService(): AuthenticationService {
        val service = mock(AuthenticationService::class.java)
        `when`(service.hasActiveSession()).thenReturn(false)

        return service
    }

    private fun mockMappingMissionDao(): MappingMissionDao {
        val mappingMission = mock(MappingMissionDao::class.java)
        val liveData = MutableLiveData(listOf(MappingMission("name", listOf())))
        `when`(mappingMission.getPrivateMappingMissions(anyString())).thenReturn(liveData)
        `when`(mappingMission.getSharedMappingMissions()).thenReturn(liveData)

        return mappingMission
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

        onView(ViewMatchers.withId(R.id.log_in_button))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(ViewMatchers.withId(R.id.log_in_button)).perform(ViewActions.click())
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

        onView(ViewMatchers.withId(R.id.log_out_button))
                    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(ViewMatchers.withId(R.id.log_out_button)).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.log_in_button))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        verify(authService).signOut()
    }

    @Test
    fun goToItineraryCreateWorks() {
        onView(ViewMatchers.withId(R.id.create_itinerary_button))
                .perform(ViewActions.click())
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

        onView(ViewMatchers.withId(R.id.browse_itinerary_button))
                .perform(ViewActions.click())
        Intents.intended(
                hasComponent(hasClassName(MappingMissionSelectionActivity::class.java.name))
        )
    }

    @Test
    fun goToMappingMissionSelectionWorksWithoutActiveSession() {
        `when`(authService.hasActiveSession()).thenReturn(false)

        onView(ViewMatchers.withId(R.id.browse_itinerary_button))
                .perform(ViewActions.click())
        Intents.intended(
                hasComponent(hasClassName(LoginActivity::class.java.name))
        )
    }

    @Test
    fun goToDroneConnectWorks() {
        onView(ViewMatchers.withId(R.id.connect_drone_button)).perform(ViewActions.click())
        if(isConnected()) {
            Intents.intended(
                hasComponent(hasClassName(ConnectedDroneActivity::class.java.name))
            )
        } else {
            Intents.intended(
                hasComponent(hasClassName(DroneConnectActivity::class.java.name))
            )
        }
    }
}