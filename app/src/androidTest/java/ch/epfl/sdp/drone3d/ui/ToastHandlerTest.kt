package ch.epfl.sdp.drone3d.ui

import android.app.Activity
import android.widget.Toast
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.TempTestActivity
import ch.epfl.sdp.drone3d.matcher.ToastMatcher.onToast
import org.junit.*
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
            { activity -> onToast(activity, text) }
        )
    }

    @Test
    fun simpleToastWithResWorks() {
        testToast(
            { activity -> ToastHandler.showToast(activity, R.string.connect_a_drone) },
            { activity -> onToast(activity, R.string.connect_a_drone) }
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
            { activity -> onToast(activity, text) }
        )
    }

    @Test
    fun asyncToastWithResWorks() {
        testToast(
            { activity ->
                Thread {
                    ToastHandler.showToastAsync(activity, R.string.connect_a_drone)
                }.start()
            },
            { activity -> onToast(activity, R.string.connect_a_drone) }
        )
    }

    @Test
    fun toastFormatWorks() {
        val format = "Text with %d %s"
        val args = arrayOf(2, "formatting")

        testToast(
            { activity -> ToastHandler.showToastF(activity, format, Toast.LENGTH_SHORT, *args) },
            { activity -> onToast(activity, format.format(*args)) }
        )
    }

    @Test
    fun asyncToastFormatWorks() {
        val format = "Text with %d %s"
        val args = arrayOf(2, "formatting")

        testToast(
            { activity ->
                Thread {
                    ToastHandler.showToastAsyncF(activity, format, Toast.LENGTH_SHORT, *args)
                }.start()
            },
            { activity -> onToast(activity, format.format(*args)) }
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