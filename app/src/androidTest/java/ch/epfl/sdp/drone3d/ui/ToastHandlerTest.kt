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
import ch.epfl.sdp.drone3d.matcher.ToastMatcher
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

    @Ignore("Apparently, having multiple toast tests breaks the continuous integration. This test works locally")
    @Test
    fun simpleToastWorks() {
        val text = "Toast text"
        testToast(
            { activity -> ToastHandler.showToast(activity, text, Toast.LENGTH_LONG) },
            { activity -> ToastMatcher.onToast(activity, text) }
        )
    }

    @Ignore("Apparently, having multiple toast tests breaks the continuous integration. This test works locally")
    @Test
    fun simpleToastWithResWorks() {
        testToast(
            { activity -> ToastHandler.showToast(activity, R.string.connect_a_drone, Toast.LENGTH_LONG) },
            { activity -> ToastMatcher.onToast(activity, R.string.connect_a_drone) }
        )
    }

    @Ignore("Apparently, having multiple toast tests breaks the continuous integration. This test works locally")
    @Test
    fun asyncToastWorks() {
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
    fun asyncToastWithResWorks() {
        testToast(
            { activity ->
                Thread {
                    ToastHandler.showToastAsync(activity, R.string.connect_a_drone, Toast.LENGTH_LONG)
                }.start()
            },
            { activity -> ToastMatcher.onToast(activity, R.string.connect_a_drone) }
        )
    }

    @Ignore("Apparently, having multiple toast tests breaks the continuous integration. This test works locally")
    @Test
    fun toastFormatWorks() {
        val format = "Text with %d %s"
        val args = arrayOf(2, "formatting")

        testToast(
            { activity -> ToastHandler.showToastF(activity, format, Toast.LENGTH_LONG, *args) },
            { activity -> ToastMatcher.onToast(activity, format.format(*args)) }
        )
    }

    @Ignore("Apparently, having multiple toast tests breaks the continuous integration. This test works locally")
    @Test
    fun asyncToastFormatWorks() {
        val format = "Text with %d %s"
        val args = arrayOf(2, "formatting")

        testToast(
            { activity ->
                Thread {
                    ToastHandler.showToastAsyncF(activity, format, Toast.LENGTH_LONG, *args)
                }.start()
            },
            { activity -> ToastMatcher.onToast(activity, format.format(*args)) }
        )
    }

    @Test
    fun testAllToasts() {

        val text = "Toast text"
        val id = R.string.email_text
        val format = "Text with %d %s"
        val args = arrayOf(2, "formatting")

        testToast(
                { activity -> ToastHandler.showToast(activity, text, Toast.LENGTH_LONG) },
                { activity -> ToastMatcher.onToast(activity, text) }
        )

        testToast(
                { activity -> ToastHandler.showToast(activity, id, Toast.LENGTH_LONG) },
                { activity -> ToastMatcher.onToast(activity, id) }
        )

        testToast(
                { activity ->
                    Thread {
                        ToastHandler.showToastAsync(activity, text, Toast.LENGTH_LONG)
                    }.start()
                },
                { activity -> ToastMatcher.onToast(activity, text) }
        )

        testToast(
                { activity ->
                    Thread {
                        ToastHandler.showToastAsync(activity, id, Toast.LENGTH_LONG)
                    }.start()
                },
                { activity -> ToastMatcher.onToast(activity, id) }
        )

        testToast(
                { activity -> ToastHandler.showToastF(activity, format, Toast.LENGTH_LONG, *args) },
                { activity -> ToastMatcher.onToast(activity, format.format(*args)) }
        )

        testToast(
                { activity ->
                    Thread {
                        ToastHandler.showToastAsyncF(activity, format, Toast.LENGTH_LONG, *args)
                    }.start()
                },
                { activity -> ToastMatcher.onToast(activity, format.format(*args)) }
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