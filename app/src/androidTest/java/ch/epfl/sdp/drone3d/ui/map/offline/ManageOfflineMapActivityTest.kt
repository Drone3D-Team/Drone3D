package ch.epfl.sdp.drone3d.ui.map.offline

import android.os.SystemClock
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.mapboxsdk.geometry.LatLng

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.map.MapboxUtility
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.OfflineManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.not
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

    /*
    @BindValue
    val offlineMapSaver: OfflineMapSaver = Mockito.mock(OfflineMapSaver::class.java)

    init {
        // Mock OfflineMapSaver
        Mockito.`when`(offlineMapSaver.getMaxTileCount()).thenReturn(6000L)
        Mockito.`when`(offlineMapSaver.getTotalTileCount()).thenReturn(MutableLiveData<Long>(3000L))
        Mockito.`when`(offlineMapSaver.getTotalTileCount()).
    }
     */

    @Before
    fun setUp() {
        Intents.init()
    }


    //Clear the database for offline tiles before starting the tests
    @Before
    fun clearOfflineMapDatabase(){

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

    @Test
    fun tileCountIsInitializedTo0() {
        SystemClock.sleep(2000) //Because the view is a observer and we need some time to make it get the value
        onView(withId(R.id.tiles_used))
            .check(matches(withText("0/6000")))
    }

//    @Test
//    fun DownloadTilesMakesTileCountGrowAndRecyclerViewNotEmpty() {
//
//        SystemClock.sleep(2000) //Need to wait for the map to be downloaded. Doesn't work with later callback only
//
//        //Zoom to make the download happens faster so that we don't have to wait to sleep for too long
//        var counter = CountDownLatch(1)
//
//        activityRule.scenario.onActivity { activity ->
//                activity.mapView.getMapAsync { mapboxMap ->
//                    mapboxMap.setStyle(Style.MAPBOX_STREETS) { _ ->
//                        MapboxUtility.zoomOnCoordinate(ZOOM_LOCATION, mapboxMap, ZOOM_VALUE)
//                        counter.countDown()
//
//                    }
//                }
//            }
//
//        assert(counter.await(TIMEOUT, TimeUnit.SECONDS))
//
//        onView(withId(R.id.buttonToSaveOfflineMap))
//            .perform(click())
//
//        SystemClock.sleep(10000) //Need to wait for the map to be downloaded. Cannot use counter
//                                    //since the callback is inside the activity
//
//        onView(withId(R.id.tiles_used))
//            .check(matches(not(withText("0/6000"))))
//
//
//        //Check that the recyclerView has an element
//
//        counter = CountDownLatch(1)
//        var size = 0
//        activityRule.scenario.onActivity { activity ->
//            val recyclerAdapter = activity.findViewById<RecyclerView>(R.id.saved_regions).adapter
//            size = recyclerAdapter?.itemCount ?: 0
//            counter.countDown()
//        }
//
//        assert(counter.await(TIMEOUT, TimeUnit.SECONDS))
//
//        assert(size==1)
//    }


}
