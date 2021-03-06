/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.auth


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.matcher.ToastMatcher
import ch.epfl.sdp.drone3d.service.module.AuthenticationModule
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.ui.MainActivity
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.AuthResult
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

/**
 * Test for the register activity
 */
@HiltAndroidTest
@UninstallModules(AuthenticationModule::class)
class RegisterActivityTest {

    private val activityRule = ActivityTestRule(RegisterActivity::class.java)

    @get:Rule
    var testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val authService: AuthenticationService = mock(AuthenticationService::class.java)

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
    fun registerButtonWorksWithSuccess() {
        val taskSource = TaskCompletionSource<AuthResult>()
        `when`(authService.register(anyString(), anyString())).thenReturn(taskSource.task)

        // write something in emailEditText and passwordEditText because particular behaviour when empty
        onView(withId(R.id.emailEditText))
            .perform(ViewActions.typeText("Email"))
        onView(withId(R.id.passwordEditText))
            .perform(ViewActions.typeText("Password"))
        onView(isRoot())
            .perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.registerButton)).perform(click())
        // Progress bar visible until success
        onView(withId(R.id.progressBar))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        val result = mock(AuthResult::class.java)
        taskSource.setResult(result)
        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(MainActivity::class.java.name))
        )
    }


    @Test
    fun registerButtonWorksWithFailure() {
        val taskSource = TaskCompletionSource<AuthResult>()
        `when`(authService.register(anyString(), anyString())).thenReturn(taskSource.task)

        // write something in emailEditText and passwordEditText because particular behaviour when empty
        onView(withId(R.id.emailEditText))
            .perform(ViewActions.typeText("Email"))
        onView(withId(R.id.passwordEditText))
            .perform(ViewActions.typeText("Password"))
        onView(isRoot())
            .perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.registerButton)).perform(click())
        // Progress bar visible until success
        onView(withId(R.id.progressBar))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        val message = "Error message"
        taskSource.setException(Exception(message))

        onView(withId(R.id.infoText))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            .check(matches(withText(message)))
        onView(withId(R.id.progressBar))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun loginButtonWorksWithEmptyValues() {
        // Test that the toast is displayed
        onView(withId(R.id.emailEditText))
            .perform(ViewActions.typeText(""))
        onView(withId(R.id.passwordEditText))
            .perform(ViewActions.typeText(""))
        onView(isRoot())
            .perform(ViewActions.closeSoftKeyboard())
        onView(withId(R.id.loginButton))
            .perform(click())

        ToastMatcher.onToast(activityRule.activity, R.string.login_fail)
    }

    @Test
    fun startsLoginActivity() {
        onView(withId(R.id.loginButton)).perform(click())
        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(LoginActivity::class.java.name))
        )
    }
}