package ch.epfl.sdp.drone3d.ui.map.offline

import android.os.SystemClock
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
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
        private const val ZOOM_VALUE = 19.9
        private val ZOOM_LOCATION = LatLng(10.0, 10.0)
    }

    private val activityRule = ActivityScenarioRule(ManageOfflineMapActivity::class.java)

    @get:Rule
    var testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)


    /**
     * This object is used to click on a specific element with a given id inside a recyclerView.
     * It is taken from https://stackoverflow.com/questions/28476507/using-espresso-to-click-view-inside-recyclerview-item
     */
    object MyViewAction {
        fun clickChildViewWithId(id: Int): ViewAction {
            return object : ViewAction {
                override fun getConstraints(): Matcher<View>? {
                    return null
                }

                override fun getDescription(): String {
                    return "Click on a child view with specified id."
                }

                override fun perform(uiController: UiController?, view: View) {
                    val v: View = view.findViewById(id)
                    v.performClick()
                }
            }
        }
    }

    @Before
    fun setUp() {
        Intents.init()
    }

    /**
     * This method clears the database that stores the OfflineRegions in Mapbox before every test.
     */
    @Before
    fun clearOfflineMapDatabaseBefore(){
        val counter = CountDownLatch(1)
        val offlineManager = OfflineManager.getInstance(InstrumentationRegistry.getInstrumentation().targetContext)
        offlineManager.resetDatabase(object : OfflineManager.FileSourceCallback {
            override fun onSuccess() {
                counter.countDown()
            }

            override fun onError(message: String) {


            }
        })

        assert(counter.await(TIMEOUT, TimeUnit.SECONDS))
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

    /**
     * Make sure the tile count is initialized to 0
     */
    @Test
    fun tileCountIsInitializedTo0() {
        SystemClock.sleep(2000) //Because the view is a observer and we need some time to make it get the value
        onView(withId(R.id.tiles_used))
            .check(matches(withText("0/6000")))
    }

    /**
     * This test tests most of the UI functionality at once. The reason is that the downloading takes
     * quite a long time and that it is done asynchronously, thus it makes the testing faster if we don't
     * have to wait for most of the tests to download some tiles. Furthermore, mocking the OfflineMapSaverImpl
     * is not possible either, because Mapbox's OfflineRegion cannot be instantiated since its constructor
     * is private.
     */
    @Test
    fun userBehaviorTest() {

        SystemClock.sleep(2000) //Need to wait for the map to be downloaded. Doesn't work with later callback only

        //Zoom to make the download happens faster so that we don't have to wait to sleep for too long
        var counter = CountDownLatch(1)

        activityRule.scenario.onActivity { activity ->
                activity.mapView.getMapAsync { mapboxMap ->
                    mapboxMap.setStyle(Style.MAPBOX_STREETS) { _ ->
                        MapboxUtility.zoomOnCoordinate(ZOOM_LOCATION, mapboxMap, ZOOM_VALUE)
                        counter.countDown()

                    }
                }
            }

        assert(counter.await(TIMEOUT, TimeUnit.SECONDS))

        onView(withId(R.id.buttonToSaveOfflineMap))
            .perform(click())

        SystemClock.sleep(10000) //Need to wait for the map to be downloaded. Cannot use counter
                                    //since the callback is inside the activity

        onView(withId(R.id.tiles_used))
            .check(matches(not(withText("0/6000"))))


        //Check that the recyclerView has an element
        counter = CountDownLatch(1)
        var size = 0
        var recyclerView: RecyclerView? = null
        activityRule.scenario.onActivity { activity ->
            recyclerView = activity.findViewById<RecyclerView>(R.id.saved_regions)
            size = recyclerView?.adapter?.itemCount ?: 0
            counter.countDown()
        }

        assert(counter.await(TIMEOUT, TimeUnit.SECONDS))

        assert(size==1)

        //Click to remove the element and check that the recyclerView has no more elements
        onView(withId(R.id.saved_regions)).perform(
           RecyclerViewActions.actionOnItemAtPosition<OfflineRegionViewAdapter.OfflineRegionViewHolder>(
               0, MyViewAction.clickChildViewWithId(R.id.deleteRegionButton)))

        SystemClock.sleep(2000) // Make sure the region had enough time to be deleted

        size = recyclerView?.adapter?.itemCount ?: 1
        assert(size==0)

        onView(withId(R.id.tiles_used))
            .check(matches(withText("0/6000")))

    }
}
