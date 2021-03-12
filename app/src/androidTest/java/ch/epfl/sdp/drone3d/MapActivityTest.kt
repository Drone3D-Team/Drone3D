package ch.epfl.sdp.drone3d
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matchers


/**
 * Test for the map activity
 */
@RunWith(AndroidJUnit4::class)
class MapActivityTest {

    @get:Rule
    var testRule = ActivityScenarioRule(MapActivity::class.java)

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
    fun supportActionBarGoesBackToMainActivity() {
        Intents.init()
        val imageButton = onView(
            Matchers.allOf(
                withContentDescription("Navigate up"),
                isDisplayed()
            )
        )

        imageButton.perform(click())
        intended(hasComponent(MainActivity::class.java.name))
        Intents.release()
    }

}