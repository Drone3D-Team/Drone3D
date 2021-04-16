/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui

import android.app.Activity
import android.widget.Toast
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.matcher.ToastMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CompletableFuture

class ToastHandlerTest {

    @get:Rule
    val intentsTestRule = ActivityScenarioRule(TempTestActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun simpleToastWorks() {
        val text = "Toast text"
        testToast(
            { activity -> ToastHandler.showToast(activity, text) },
            { activity -> ToastMatcher.onToast(activity, text) }
        )
    }

    @Test
    fun simpleToastWithResWorks() {
        testToast(
            { activity -> ToastHandler.showToast(activity, R.string.app_name) },
            { activity -> ToastMatcher.onToast(activity, R.string.app_name) }
        )
    }

    @Test
    fun asyncToastWorks() {
        val text = "Toast text"

        testToast(
            { activity ->
                Thread {
                    ToastHandler.showToastAsync(activity, text)
                }.start()
            },
            { activity -> ToastMatcher.onToast(activity, text) }
        )
    }

    @Test
    fun asyncToastWithResWorks() {
        testToast(
            { activity ->
                Thread {
                    ToastHandler.showToastAsync(activity,R.string.app_name)
                }.start()
            },
            { activity -> ToastMatcher.onToast(activity, R.string.app_name) }
        )
    }

    @Test
    fun toastFormatWorks() {
        val format = "Text with %d %s"
        val args = arrayOf(2, "formatting")

        testToast(
            { activity -> ToastHandler.showToast(activity, format, Toast.LENGTH_LONG, *args) },
            { activity -> ToastMatcher.onToast(activity, format.format(*args)) }
        )
    }

    @Test
    fun asyncToastFormatWorks() {
        val format = "Text with %d %s"
        val args = arrayOf(2, "formatting")

        testToast(
            { activity ->
                Thread {
                    ToastHandler.showToastAsync(activity, format, Toast.LENGTH_LONG, *args)
                }.start()
            },
            { activity -> ToastMatcher.onToast(activity, format.format(*args)) }
        )
    }

    @Test
    fun toastResFormatWorks() {
        val args = arrayOf("formatting")

        testToast(
                { activity -> ToastHandler.showToast(activity, R.string.drone_simulated_ip, Toast.LENGTH_LONG, *args) },
                { activity -> ToastMatcher.onToast(activity, activity.getString(R.string.drone_simulated_ip, *args)) }
        )
    }

    @Test
    fun asyncToastResFormatWorks() {
        val args = arrayOf("formatting")

        testToast(
                { activity ->
                    Thread {
                        ToastHandler.showToastAsync(activity, R.string.drone_simulated_ip, Toast.LENGTH_LONG, *args)
                    }.start()
                },
                { activity -> ToastMatcher.onToast(activity, activity.getString(R.string.drone_simulated_ip, *args)) }
        )
    }

    @Test
    fun simpleToastCustomDurationWorks() {
        val text = "Toast text"
        testToast(
                { activity -> ToastHandler.showToast(activity, text, 5) },
                { activity -> ToastMatcher.onToast(activity, text) }
        )
    }

    @Test
    fun simpleToastWithCustomDurationResWorks() {
        testToast(
                { activity -> ToastHandler.showToast(activity, R.string.app_name, Toast.LENGTH_LONG) },
                { activity -> ToastMatcher.onToast(activity, R.string.app_name) }
        )
    }

    @Test
    fun asyncToastCustomDurationWorks() {
        val text = "Toast text"

        testToast(
                { activity ->
                    Thread {
                        ToastHandler.showToastAsync(activity, text, Toast.LENGTH_LONG)
                    }.start()
                },
                { activity -> ToastMatcher.onToast(activity, text) }
        )
    }

    @Test
    fun asyncToastWithResCustomDurationCustomDurationWorks() {
        testToast(
                { activity ->
                    Thread {
                        ToastHandler.showToastAsync(activity,R.string.app_name, Toast.LENGTH_LONG)
                    }.start()
                },
                { activity -> ToastMatcher.onToast(activity, R.string.app_name) }
        )
    }
    private fun testToast(generator: (Activity) -> Unit, matcher: (Activity) -> ViewInteraction) {
        val activity = CompletableFuture<Activity>()
        intentsTestRule.scenario.onActivity {
            generator.invoke(it)
            activity.complete(it)
        }

        matcher.invoke(activity.get()).check(matches(isDisplayed()))
    }
}