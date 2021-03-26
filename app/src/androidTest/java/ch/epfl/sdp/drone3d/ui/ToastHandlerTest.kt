package ch.epfl.sdp.drone3d.ui

import android.widget.Toast
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.TempTestActivity
import ch.epfl.sdp.drone3d.matcher.ToastMatcher.Companion.onToast
import org.junit.*

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
        intentsTestRule.scenario.onActivity { activity ->
            ToastHandler.showToast(activity, text, Toast.LENGTH_LONG)
        }
        onToast(text).check(matches(isDisplayed()))
    }

    @Test
    fun simpleToastWithResWorks() {
        intentsTestRule.scenario.onActivity { activity ->
            ToastHandler.showToast(activity, R.string.connect_a_drone, Toast.LENGTH_LONG)
        }
        onToast(R.string.connect_a_drone).check(matches(isDisplayed()))
    }

    @Test
    fun asyncToastWorks() {
        val text = "Toast text"
        intentsTestRule.scenario.onActivity { activity ->
            Thread {
                ToastHandler.showToastAsync(activity, text, Toast.LENGTH_LONG)
            }.start()
        }
        onToast(text).check(matches(isDisplayed()))
    }

    @Test
    fun asyncToastWithResWorks() {
        intentsTestRule.scenario.onActivity { activity ->
            Thread {
                ToastHandler.showToastAsync(activity, R.string.connect_a_drone, Toast.LENGTH_LONG)
            }.start()
        }
        onToast(R.string.connect_a_drone).check(matches(isDisplayed()))
    }

    @Test
    fun toastFormatWorks() {
        val format = "Text with %d %s"
        val args = arrayOf(2, "formatting")
        intentsTestRule.scenario.onActivity { activity ->
            ToastHandler.showToastF(activity, format, Toast.LENGTH_LONG, *args)
        }
        onToast(format.format(*args)).check(matches(isDisplayed()))
    }

    @Test
    fun asyncToastFormatWorks() {
        val format = "Text with %d %s"
        val args = arrayOf(2, "formatting")
        intentsTestRule.scenario.onActivity { activity ->
            Thread {
                ToastHandler.showToastAsyncF(activity, format, Toast.LENGTH_LONG, *args)
            }.start()
        }
        onToast(format.format(*args)).check(matches(isDisplayed()))
    }
}