/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.model.auth.UserSession
import ch.epfl.sdp.drone3d.model.mission.MappingMission
import ch.epfl.sdp.drone3d.model.weather.WeatherReport
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.api.mission.MappingMissionService
import ch.epfl.sdp.drone3d.service.api.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.service.api.weather.WeatherService
import ch.epfl.sdp.drone3d.service.module.AuthenticationModule
import ch.epfl.sdp.drone3d.service.module.LocationModule
import ch.epfl.sdp.drone3d.service.module.MappingMissionDaoModule
import ch.epfl.sdp.drone3d.service.module.WeatherModule
import com.google.firebase.auth.FirebaseUser
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.*
import org.junit.rules.RuleChain
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.lang.Thread.sleep
import java.util.*

@HiltAndroidTest
@UninstallModules(
    AuthenticationModule::class,
    MappingMissionDaoModule::class,
    LocationModule::class,
    WeatherModule::class,
)
class SaveMappingMissionActivityTest {

    companion object {
        private const val USER_UID = "user_id"
        private val GOOD_WEATHER_REPORT = WeatherReport(
            "Clear", "description",
            "01d",20.0, 20, 5.0, 500, Date(12903)
        )
        private val AREA = arrayListOf(
            LatLng(46.52109254025395, 6.643008669745938),
            LatLng(46.52109254025395, 6.64300866974594),
            LatLng(46.52109254025397, 6.643008669745938),
        )
        private val UNNAMED_MISSION = MappingMission(
            "Unnamed mission",
            50.0,
            MappingMissionService.Strategy.SINGLE_PASS,
            AREA
        )
        private val NAMED_MISSION =
            MappingMission("My mission", 50.0, MappingMissionService.Strategy.SINGLE_PASS, AREA)

    }

    @BindValue
    val locationService: LocationService = mock(LocationService::class.java)

    private val activityRule = ActivityScenarioRule<SaveMappingMissionActivity>(
        Intent(
            ApplicationProvider.getApplicationContext(),
            SaveMappingMissionActivity::class.java
        ).apply {
            putExtras(Bundle().apply {
                putSerializable(
                    ItineraryCreateActivity.FLIGHTHEIGHT_INTENT_PATH,
                    UNNAMED_MISSION.flightHeight
                )
                putSerializable(
                    ItineraryCreateActivity.STRATEGY_INTENT_PATH,
                    UNNAMED_MISSION.strategy
                )
                putSerializable(
                    ItineraryCreateActivity.AREA_INTENT_PATH, AREA
                )
            })
        }
    )


    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this))
        .around(activityRule)

    @BindValue
    val authService: AuthenticationService = mockAuthenticationService()

    @BindValue
    val weatherService: WeatherService = mock(WeatherService::class.java)


    @BindValue
    val mappingMissionDao: MappingMissionDao = mockMappingMissionDao()

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    private fun mockMappingMissionDao(): MappingMissionDao {
        val mappingMissionDao = mock(MappingMissionDao::class.java)

        `when`(
            mappingMissionDao.shareMappingMission(
                anyString(),
                any(MappingMission::class.java)
            )
        ).thenReturn(MutableLiveData(true))
        `when`(
            mappingMissionDao.storeMappingMission(
                anyString(),
                any(MappingMission::class.java)
            )
        ).thenReturn(MutableLiveData(true))

        return mappingMissionDao
    }

    private fun mockAuthenticationService(): AuthenticationService {
        val authService = mock(AuthenticationService::class.java)

        val user = mock(FirebaseUser::class.java)
        `when`(user.uid).thenReturn(USER_UID)
        val userSession = UserSession(user)
        `when`(authService.getCurrentSession()).thenReturn(userSession)

        return authService
    }

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

    init {
        `when`(locationService.isLocationEnabled()).thenReturn(false)
        `when`(weatherService.getWeatherReport(AREA[0])).thenReturn(
            MutableLiveData(
                GOOD_WEATHER_REPORT
            )
        )
    }


    @Test
    fun saveMappingMissionToPrivateCallStore() {
        activityRule.scenario.recreate()

        onView(withId(R.id.privateCheckBox)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        sleep(1000)

        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(ItineraryShowActivity::class.java.name))
        )

        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra(MissionViewAdapter.FLIGHTHEIGHT_INTENT_PATH) })
        assert(intents.any { it.hasExtra(MissionViewAdapter.STRATEGY_INTENT_PATH) })
        assert(intents.any { it.hasExtra(MissionViewAdapter.AREA_INTENT_PATH) })

        verify(mappingMissionDao, times(1)).storeMappingMission(USER_UID, UNNAMED_MISSION)
    }

    @Test
    fun saveMappingMissionToShareCallShare() {
        activityRule.scenario.recreate()

        onView(withId(R.id.sharedCheckBox)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        sleep(1000)

        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(ItineraryShowActivity::class.java.name))
        )

        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra(MissionViewAdapter.FLIGHTHEIGHT_INTENT_PATH) })
        assert(intents.any { it.hasExtra(MissionViewAdapter.STRATEGY_INTENT_PATH) })
        assert(intents.any { it.hasExtra(MissionViewAdapter.AREA_INTENT_PATH) })

        verify(mappingMissionDao, times(1)).shareMappingMission(USER_UID, UNNAMED_MISSION)
    }

    @Test
    fun saveMappingMissionToShareAndPrivateCallShareAndStore() {
        activityRule.scenario.recreate()

        onView(withId(R.id.sharedCheckBox)).perform(click())
        onView(withId(R.id.privateCheckBox)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        sleep(1000)

        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(ItineraryShowActivity::class.java.name))
        )

        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra(MissionViewAdapter.FLIGHTHEIGHT_INTENT_PATH) })
        assert(intents.any { it.hasExtra(MissionViewAdapter.STRATEGY_INTENT_PATH) })
        assert(intents.any { it.hasExtra(MissionViewAdapter.AREA_INTENT_PATH) })

        verify(mappingMissionDao, times(1)).shareMappingMission(USER_UID, UNNAMED_MISSION)
        verify(mappingMissionDao, times(1)).storeMappingMission(USER_UID, UNNAMED_MISSION)
    }

    @Test
    fun nameIsCorrect() {
        activityRule.scenario.recreate()

        onView(withId(R.id.missionName)).perform(click()).perform(
            typeText(
                NAMED_MISSION.name
            )
        ).perform(closeSoftKeyboard())

        onView(withId(R.id.privateCheckBox)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        sleep(1000)

        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(ItineraryShowActivity::class.java.name))
        )

        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra(MissionViewAdapter.FLIGHTHEIGHT_INTENT_PATH) })
        assert(intents.any { it.hasExtra(MissionViewAdapter.STRATEGY_INTENT_PATH) })
        assert(intents.any { it.hasExtra(MissionViewAdapter.AREA_INTENT_PATH) })

        verify(mappingMissionDao, times(1)).storeMappingMission(USER_UID, NAMED_MISSION)
    }

    @Test
    fun saveWithoutCheckingShowDialog() {
        activityRule.scenario.recreate()

        onView(withId(R.id.saveButton)).perform(click())

        onView(withText(R.string.continue_without_saving)).check(matches(isDisplayed()))
        onView(withText(R.string.continue_without_saving)).perform(click())

        sleep(1000)

        Intents.intended(
            IntentMatchers.hasComponent(ComponentNameMatchers.hasClassName(ItineraryShowActivity::class.java.name))
        )

        val intents = Intents.getIntents()
        assert(intents.any { it.hasExtra(MissionViewAdapter.FLIGHTHEIGHT_INTENT_PATH) })
        assert(intents.any { it.hasExtra(MissionViewAdapter.STRATEGY_INTENT_PATH) })
        assert(intents.any { it.hasExtra(MissionViewAdapter.AREA_INTENT_PATH) })
    }
}
