package ch.epfl.sdp.drone3d

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import ch.epfl.sdp.drone3d.auth.AuthenticationModule
import ch.epfl.sdp.drone3d.auth.AuthenticationService
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito

/**
 * Test for the map activity
 */
@HiltAndroidTest
@UninstallModules(AuthenticationModule::class)
class MapActivityTest {

    private lateinit var mUiDevice: UiDevice
    private val context = Mockito.mock(Context::class.java)

    @get:Rule
    var testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(ActivityScenarioRule(ItineraryCreateActivity::class.java))

    @get:Rule
    var activityRule = ActivityScenarioRule(ItineraryCreateActivity::class.java)

    /**
     * Make sure the context of the app is the right one
     */
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("ch.epfl.sdp.drone3d", appContext.packageName)
    }

    @BindValue
    val authService: AuthenticationService = Mockito.mock(AuthenticationService::class.java)

    @Before
    fun setUp() {
        Intents.init()
        Mockito.`when`(context.checkSelfPermission(ACCESS_FINE_LOCATION))
            .thenReturn(
                PackageManager.PERMISSION_DENIED
            )
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun supportActionBarGoesBackToMainActivity() {
        Mockito.`when`(authService.hasActiveSession()).thenReturn(true)

        val imageButton = onView(
            Matchers.allOf(
                withContentDescription("Navigate up"),
                isDisplayed()
            )
        )

        imageButton.perform(click())
        intended(hasComponent(MainActivity::class.java.name))
    }

    @Test
    fun denyLocatePositionWorks() {
        Mockito.`when`(context.checkSelfPermission(ACCESS_FINE_LOCATION))
            .thenReturn(
                PackageManager.PERMISSION_DENIED
            )

        activityRule.scenario.onActivity {
            it.onRequestPermissionsResult(
                0,
                arrayOf("android.permission.ACCESS_FINE_LOCATION"),
                intArrayOf(-1)
            )
        }

    }

    @Test
    fun allowLocatePositionWorks() {
        Mockito.`when`(context.checkSelfPermission(ACCESS_FINE_LOCATION))
            .thenReturn(
                PackageManager.PERMISSION_DENIED
            )

        activityRule.scenario.onActivity {
            it.onRequestPermissionsResult(
                0,
                arrayOf("android.permission.ACCESS_FINE_LOCATION"),
                intArrayOf(0)
            )
        }
    }
}