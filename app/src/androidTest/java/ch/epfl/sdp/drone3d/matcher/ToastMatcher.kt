package ch.epfl.sdp.drone3d.matcher

import android.app.Activity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

/**
 * This class allows to match Toast messages in tests with Espresso.
 *
 * Written by yasd at https://stackoverflow.com/a/33387980
 *
 * Usage in test class:
 *
 * import somepkg.ToastMatcher.Companion.onToast
 *
 * // To assert a toast does *not* pop up:
 * onToast("text").check(doesNotExist())
 * onToast(textId).check(doesNotExist())
 *
 * // To assert a toast does pop up:
 * onToast("text").check(matches(isDisplayed()))
 * onToast(textId).check(matches(isDisplayed()))
 */
object ToastMatcher {

   fun onToast(activity: Activity, text: String): ViewInteraction = onView(withText(text)).inRoot(isToast(activity))!!

    fun onToast(activity: Activity, textId: Int): ViewInteraction = onView(withText(textId)).inRoot(isToast(activity))!!

    private fun isToast(activity: Activity): Matcher<Root> {
        return RootMatchers.withDecorView(CoreMatchers.not(activity.window.decorView))
    }
}