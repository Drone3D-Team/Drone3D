package ch.epfl.sdp.drone3d

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.runner.RunWith

/**
 * Test for the register activity
 */
@RunWith(AndroidJUnit4::class)
class RegisterActivityTest {

    @get:Rule
    var testRule = ActivityScenarioRule(RegisterActivity::class.java)

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
    fun progressBarVisibleWhenClickLoginButton() {
        Espresso.onView(ViewMatchers.withId(R.id.registerButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.progressBar))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun startsLoginActivity() {
        Espresso.onView(ViewMatchers.withId(R.id.loginButton)).perform(ViewActions.click())
        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(LoginActivity::class.java.name))
        )
    }

    @Test
    fun startsMainActivity() {
        Espresso.onView(ViewMatchers.withId(R.id.backButton)).perform(ViewActions.click())
        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(MainActivity::class.java.name))
        )
    }
}