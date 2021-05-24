package ch.epfl.sdp.drone3d.ui.mission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.api.drone.DroneData.DroneStatus.*
import ch.epfl.sdp.drone3d.service.api.drone.DroneExecutor
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.service.module.DroneModule
import ch.epfl.sdp.drone3d.ui.map.MissionInProgressActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mavsdk.mission.Mission
import io.reactivex.Completable
import io.reactivex.subjects.CompletableSubject
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.mockito.Mockito.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * Test for the mapping mission selection activity
 */
@HiltAndroidTest
@UninstallModules(DroneModule::class)
class MissionStartActivityTest {

    companion object {
        private val FLIGHT_PATH = listOf(
            LatLng(37.41253570576311, -121.99694775011824),
            LatLng(37.412496825414046, -121.99683107403213),
            LatLng(37.41243024942702, -121.99686795440418)
        )
        private const val FLIGHT_HEIGHT = 50.0
        private const val CAMERA_PITCH = 90f
    }

    private val activityRule = ActivityScenarioRule<MissionStartActivity>(
        Intent(
            ApplicationProvider.getApplicationContext(),
            MissionStartActivity::class.java
        ).apply {
            putExtras(Bundle().apply {
                putSerializable(
                    ItineraryShowActivity.FLIGHTPATH_INTENT_PATH,
                    ArrayList(FLIGHT_PATH)
                )
            })
            putExtra(ItineraryShowActivity.CAMERA_PITCH_INTENT_PATH, CAMERA_PITCH)
            putExtra(ItineraryShowActivity.FLIGHTHEIGHT_INTENT_PATH, FLIGHT_HEIGHT)
        })

    @get:Rule
    var testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val droneService: DroneService = DroneInstanceMock.mockService()

    private val droneStatus = MutableLiveData(IDLE)
    private var missionExecutionEnd = CompletableSubject.create()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
        missionExecutionEnd = CompletableSubject.create()
        droneStatus.postValue(IDLE)
    }

    init {
        val executor = mock(DroneExecutor::class.java)
        `when`(droneService.getExecutor()).thenReturn(executor)
        `when`(
            executor.setupMission(
                anyObj(Context::class.java),
                anyObj(Mission.MissionPlan::class.java)
            )
        )
            .thenAnswer { missionExecutionEnd }
        `when`(executor.executeMission(anyObj(Context::class.java))).thenReturn(Completable.never())
        `when`(droneService.getData().getDroneStatus()).thenReturn(droneStatus)
        `when`(droneService.getData().getPosition()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getHomeLocation()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getSpeed()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getRelativeAltitude()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getBatteryLevel()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getVideoStreamUri()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getMission()).thenReturn(MutableLiveData())
        `when`(droneService.getData().isConnected()).thenReturn(MutableLiveData(true))
    }

    @Test
    fun normalPipelineShowsCorrectProgress() {
        blockingSetValue(droneStatus, IDLE)
        onView(withId(R.id.mission_start_text)).check(matches(withText(R.string.mission_state_idle)))

        blockingSetValue(droneStatus, SENDING_ORDER)
        onView(withId(R.id.mission_start_text)).check(matches(withText(R.string.mission_state_sending)))

        blockingSetValue(droneStatus, ARMING)
        onView(withId(R.id.mission_start_text)).check(matches(withText(R.string.mission_state_arming)))

        blockingSetValue(droneStatus, STARTING_MISSION)
        onView(withId(R.id.mission_start_text)).check(matches(withText(R.string.mission_state_starting)))

        missionExecutionEnd.onComplete()
        intended(
            hasComponent(ComponentNameMatchers.hasClassName(MissionInProgressActivity::class.java.name))
        )
    }

    @Test
    fun failureDestroyActivity() {
        missionExecutionEnd.onError(Error("Test"))

        assertThat(activityRule.scenario.state, `is`(Lifecycle.State.DESTROYED))
    }

    @Test
    fun unknownStateHasCorrectText() {
        blockingSetValue(droneStatus, PAUSED)

        onView(withId(R.id.mission_start_text)).check(matches(withText(R.string.mission_state_unknown)))
    }

    private fun <T> blockingSetValue(data: MutableLiveData<T>, value: T) {
        val lock = Semaphore(0)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            data.value = value
            lock.release()
        }

        lock.tryAcquire(100, TimeUnit.MILLISECONDS)
    }

    private fun <T> anyObj(type: Class<T>): T = any(type)
}