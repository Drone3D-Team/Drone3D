package ch.epfl.sdp.drone3d

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test for the login activity
 */
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    var testRule = ActivityScenarioRule(LoginActivity::class.java)

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
        Espresso.onView(ViewMatchers.withId(R.id.loginButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.progressBar))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }


}