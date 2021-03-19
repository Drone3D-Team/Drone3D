package ch.epfl.sdp.drone3d

import android.util.Log
import android.widget.ToggleButton
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDaoModule
import ch.epfl.sdp.drone3d.storage.data.LatLong
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.*
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.mockito.Mockito

/**
 * Test for the mapping mission selection activity
 */
@HiltAndroidTest
@UninstallModules(MappingMissionDaoModule::class)
class MappingMissionSelectionActivityTest {

    @get:Rule
    var testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(ActivityScenarioRule(MappingMissionSelectionActivity::class.java))

    private fun createMock(): MappingMissionDao {
        val mock = Mockito.mock(MappingMissionDao::class.java)
        val liveData = MutableLiveData<List<MappingMission>>(listOf(MappingMission("name", listOf<LatLong>())))
        Mockito.`when`(mock.getPrivateMappingMissions(Mockito.anyString())).thenReturn(liveData)
        Mockito.`when`(mock.getSharedMappingMissions()).thenReturn(liveData)

        return mock
    }

    @BindValue
    val mappingMissionDao : MappingMissionDao = createMock()

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
    fun clickOnSwitchChangesPrivateOrSharedMode() {
        var initialState = false

        Log.i("TAG","--1" +mappingMissionDao)

        //Sets initial state to the current value of the toggle button
        onView(withId(R.id.mappingMissionToggleButton)).check { view, _ ->
            initialState =
                view.findViewById<ToggleButton>(R.id.mappingMissionToggleButton).isChecked
        }

        onView(withId(R.id.mappingMissionToggleButton))
            .perform(ViewActions.click())
        onView(withId(R.id.mappingMissionToggleButton))
            .check(matches(if (initialState) isNotChecked() else isChecked()))
        onView(withId(R.id.mappingMissionToggleButton))
            .perform(ViewActions.click())
        onView(withId(R.id.mappingMissionToggleButton))
            .check(matches(if (initialState) isChecked() else isNotChecked()))
    }

    @Test
    fun goToItineraryCreateWorks() {
        onView(withId(R.id.createMappingMissionButton))
            .perform(ViewActions.click())
        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(ItineraryCreateActivity::class.java.name))
        )
    }

}