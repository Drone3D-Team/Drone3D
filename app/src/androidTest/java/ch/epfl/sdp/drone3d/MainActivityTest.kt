package ch.epfl.sdp.drone3d

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.auth.AuthenticationModule
import ch.epfl.sdp.drone3d.auth.AuthenticationService
import ch.epfl.sdp.drone3d.auth.UserSession
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDaoModule
import ch.epfl.sdp.drone3d.storage.data.LatLong
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

    @get:Rule
    var testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
            .around(ActivityScenarioRule(MainActivity::class.java))


    private fun mappingMissionDaoMock(): MappingMissionDao {
        val mappingMission = mock(MappingMissionDao::class.java)
        val liveData = MutableLiveData(listOf(MappingMission("name", listOf<LatLong>())))
        `when`(mappingMission.getPrivateMappingMissions(anyString())).thenReturn(liveData)
        `when`(mappingMission.getSharedMappingMissions()).thenReturn(liveData)

        return mappingMission
    }

    @BindValue
    val authService: AuthenticationService = mock(AuthenticationService::class.java)

    @BindValue
    val mappingMissionDao: MappingMissionDao = mappingMissionDaoMock()

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

    /**
     */
    @Test
    fun goToLoginWorks() {
        onView(ViewMatchers.withId(R.id.log_in_button)).perform(ViewActions.click())
        Intents.intended(
                hasComponent(hasClassName(LoginActivity::class.java.name))
        )
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

    /**
     * TODO : replace TempTestActivity by DroneConnectActivity once it exists
     */
    @Test
    fun goToDroneConnectWorks() {
        onView(ViewMatchers.withId(R.id.connect_drone_button)).perform(ViewActions.click())
        Intents.intended(
                hasComponent(hasClassName(TempTestActivity::class.java.name))
        )
    }
}