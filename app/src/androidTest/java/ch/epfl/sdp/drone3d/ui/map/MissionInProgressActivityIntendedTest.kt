package ch.epfl.sdp.drone3d.ui.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.matcher.ToastMatcher
import ch.epfl.sdp.drone3d.service.api.drone.DroneData
import ch.epfl.sdp.drone3d.service.api.drone.DroneExecutor
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.drone.DroneInstanceMock
import ch.epfl.sdp.drone3d.service.module.DroneModule
import ch.epfl.sdp.drone3d.ui.mission.ItineraryShowActivity
import ch.epfl.sdp.drone3d.ui.mission.MissionViewAdapter
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mavsdk.mission.Mission
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.mockito.Mockito.*
import java.util.concurrent.CompletableFuture

/**
 * Test MissionInProgressActivity with a mission as input
 */
@HiltAndroidTest
@UninstallModules(DroneModule::class)
class MissionInProgressActivityIntendedTest {

    private var missionEndFuture: CompletableFuture<Any> = CompletableFuture()
    private val someLocationsList = arrayListOf(
        LatLng(47.398979, 8.543434),
        LatLng(47.398279, 8.543934),
        LatLng(47.397426, 8.544867),
        LatLng(47.397026, 8.543067)
    )

    private val activityRule = ActivityScenarioRule<MissionInProgressActivity>(
        Intent(ApplicationProvider.getApplicationContext(), MissionInProgressActivity::class.java).apply {
            putExtras(
                    Bundle().apply {
                        putSerializable(MissionViewAdapter.MISSION_PATH, someLocationsList)
                    }
            )
        }
    )

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val droneService: DroneService = DroneInstanceMock.mockService()

    init {
        val executor = mock(DroneExecutor::class.java)

        `when`(droneService.getExecutor()).thenReturn(executor)
        `when`(executor.startMission(anyObj(Context::class.java), anyObj(Mission.MissionPlan::class.java)))
            .thenAnswer{ Completable.fromFuture(missionEndFuture).subscribeOn(Schedulers.io()) }
        `when`(executor.returnToHomeLocationAndLand(anyObj(Context::class.java)))
                .thenReturn(Completable.complete())
        `when`(executor.returnToUserLocationAndLand(anyObj(Context::class.java)))
            .thenReturn(Completable.complete())

        `when`(droneService.getData().isConnected()).thenReturn(MutableLiveData(true))
        `when`(droneService.getData().getPosition()).thenReturn(MutableLiveData(LatLng(0.3, 0.0)))
        `when`(droneService.getData().getHomeLocation()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getDroneStatus()).thenReturn(MutableLiveData())
        `when`(droneService.getData().isConnected()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getVideoStreamUri()).thenReturn(MutableLiveData())
        `when`(droneService.getData().getMission()).thenReturn(MutableLiveData())
    }

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun loosingDroneConnectionShowsToast() {
        val isConnected = MutableLiveData(true)

        `when`(droneService.getData().isConnected()).thenReturn(isConnected)

        // Test that the toast is displayed
        val activity = CompletableFuture<MissionInProgressActivity>()
        activityRule.scenario.onActivity {
            isConnected.value = false
            activity.complete(it)
        }

        ToastMatcher.onToast(activity.get(), R.string.lost_connection_message)
    }

    @Test
    fun goBackToItineraryShowActivityWhenBackToHomePressed() {

        val dronePosition = MutableLiveData(LatLng(10.1, 10.1))
        val homePosition = MutableLiveData(LatLng(0.0, 0.0))

        `when`(droneService.getData().getPosition()).thenReturn(dronePosition)
        `when`(droneService.getData().getHomeLocation()).thenReturn(MutableLiveData(
            Telemetry.Position(
            homePosition.value?.latitude,
            homePosition.value?.longitude,
            10f,
            10f)))
        `when`(droneService.getData().getDroneStatus()).thenReturn(MutableLiveData(DroneData.DroneStatus.EXECUTING_MISSION))
        `when`(droneService.getData().isConnected()).thenReturn(MutableLiveData(true))

        Espresso.onView(ViewMatchers.withId(R.id.backToHomeButton))
            .perform(ViewActions.click())

        missionEndFuture.complete(Any())

        Intents.intended(
            IntentMatchers.hasComponent(
                ComponentNameMatchers.hasClassName(ItineraryShowActivity::class.java.name))
        )
    }

    @Test
    fun goBackToItineraryShowActivityWhenBackToUserPressed() {

        val dronePosition = MutableLiveData(LatLng(10.1, 10.1))
        val userPosition = MutableLiveData(LatLng(0.0, 0.0))

        `when`(droneService.getData().getPosition()).thenReturn(dronePosition)
        `when`(droneService.getData().getHomeLocation()).thenReturn(MutableLiveData(
            Telemetry.Position(
                userPosition.value?.latitude,
                userPosition.value?.longitude,
                10f,
                10f)))
        `when`(droneService.getData().getDroneStatus()).thenReturn(MutableLiveData(DroneData.DroneStatus.EXECUTING_MISSION))
        `when`(droneService.getData().isConnected()).thenReturn(MutableLiveData(true))

        Espresso.onView(ViewMatchers.withId(R.id.backToUserButton))
            .perform(ViewActions.click())

        missionEndFuture.complete(Any())

        Intents.intended(
            IntentMatchers.hasComponent(
                ComponentNameMatchers.hasClassName(ItineraryShowActivity::class.java.name))
        )

        missionEndFuture = CompletableFuture() //reset
    }

    private fun <T> anyObj(type: Class<T>): T = any<T>(type)
}