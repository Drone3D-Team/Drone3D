package ch.epfl.sdp.drone3d

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val intentsTestRule = ActivityScenarioRule(MainActivity::class.java)

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
        Espresso.onView(ViewMatchers.withId(R.id.log_in_button)).perform(ViewActions.click())
        Intents.intended(
            hasComponent(hasClassName(LoginActivity::class.java.name))
        )
    }


    @Test
    fun goToItineraryCreateWorks() {
        Espresso.onView(ViewMatchers.withId(R.id.create_itinerary_button))
            .perform(ViewActions.click())
        Intents.intended(
            hasComponent(hasClassName(ItineraryCreateActivity::class.java.name))
        )
    }

    /**
     * TODO : replace TempTestActivity by ItineraryBrowseActivity once it exists
     */
    @Test
    fun goToItineraryBrowseWorks() {
        Espresso.onView(ViewMatchers.withId(R.id.browse_itinerary_button))
            .perform(ViewActions.click())
        Intents.intended(
            hasComponent(hasClassName(MappingMissionSelectionActivity::class.java.name))
        )
    }

    /**
     * TODO : replace TempTestActivity by DroneConnectActivity once it exists
     */
    @Test
    fun goToDroneConnectWorks() {
        Espresso.onView(ViewMatchers.withId(R.id.connect_drone_button)).perform(ViewActions.click())
        Intents.intended(
            hasComponent(hasClassName(TempTestActivity::class.java.name))
        )
    }
}