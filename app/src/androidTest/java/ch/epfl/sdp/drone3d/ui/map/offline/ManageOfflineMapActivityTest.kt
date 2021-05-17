package ch.epfl.sdp.drone3d.ui.map.offline

import android.os.SystemClock
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.map.MapboxUtility
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.OfflineManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.junit.*
import org.junit.rules.RuleChain
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@HiltAndroidTest
class ManageOfflineMapActivityTest {

    companion object {
        private const val TIMEOUT = 5L
    }

    private val activityRule = ActivityScenarioRule(ManageOfflineMapActivity::class.java)

    @get:Rule
    var testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

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
    fun cannotEnterEmptyStringForRegionName() {

        //Used to wait for the map to be downloaded before clickling the button
        var counter = CountDownLatch(1)

        activityRule.scenario.onActivity { activity ->
            activity.mapView.getMapAsync { mapboxMap ->
                mapboxMap.setStyle(Style.MAPBOX_STREETS) { _ ->
                    counter.countDown()

                }
            }
        }

        assert(counter.await(TIMEOUT, TimeUnit.SECONDS))

        onView(withId(R.id.buttonToSaveOfflineMap))
            .perform(click())

        onView(
            allOf(
                withId(R.id.input_text),
                isAssignableFrom(EditText::class.java)
            )
        )
            .inRoot(isDialog())
            .perform(typeText(""), closeSoftKeyboard())

        onView(withContentDescription("Positive button"))
            .inRoot(isDialog())
            .perform(click())

        //Check that the dialog is still displayed
        onView(withId((R.id.input_text)))
            .check(matches(isDisplayed()))
    }
}
