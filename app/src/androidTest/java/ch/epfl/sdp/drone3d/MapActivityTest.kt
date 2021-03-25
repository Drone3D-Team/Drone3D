package ch.epfl.sdp.drone3d

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.epfl.sdp.drone3d.auth.AuthenticationModule
import ch.epfl.sdp.drone3d.auth.AuthenticationService
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito.*

/**
 * Test for the map activity
 */
@HiltAndroidTest
@UninstallModules(AuthenticationModule::class)
class MapActivityTest {

    @get:Rule
    var testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
            .around(ActivityScenarioRule(ItineraryCreateActivity::class.java))

    /**
     * Make sure the context of the app is the right one
     */
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("ch.epfl.sdp.drone3d", appContext.packageName)
    }

    @BindValue val authService: AuthenticationService = mock(AuthenticationService::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun supportActionBarGoesBackToMainActivity() {
        `when`(authService.hasActiveSession()).thenReturn(true)

        val imageButton = onView(
            Matchers.allOf(
                withContentDescription("Navigate up"),
                isDisplayed()
            )
        )

        imageButton.perform(click())
        intended(hasComponent(MainActivity::class.java.name))
    }
}