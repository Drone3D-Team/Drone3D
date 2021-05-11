package ch.epfl.sdp.drone3d.ui.map.offline

import android.os.SystemClock
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import com.mapbox.mapboxsdk.offline.OfflineManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.*
import org.junit.rules.RuleChain

@HiltAndroidTest
class ManageOfflineMapActivityTest {

    /*
    companion object {
        @BeforeClass
        fun clearDatabase() {
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm clear ch.epfl.sdp.drone3d")
                .close()
        }
    }
     */


    private val activityRule = ActivityScenarioRule(ManageOfflineMapActivity::class.java)

    @get:Rule
    var testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @Before
    fun setUp() {
        Intents.init()
    }

    //Clear the database for offline tiles
    @Before
    fun clearOfflineMapDatabase(){

        val fileSource = OfflineManager.getInstance(InstrumentationRegistry.getInstrumentation().targetContext)

        fileSource.resetDatabase(object : OfflineManager.FileSourceCallback {
            override fun onSuccess() {

            }

            override fun onError(message: String) {


            }
        })

        SystemClock.sleep(1000) //Make sure the database had enough time to be re-initialized
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
        onView(withId(R.id.tiles_used))
            .check(matches(withText("0/6000")))
    }
}
