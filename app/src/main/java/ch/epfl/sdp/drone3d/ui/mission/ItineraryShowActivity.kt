/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.app.AlertDialog.Builder
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.map.MapboxMissionDrawer
import ch.epfl.sdp.drone3d.map.MapboxUtility
import ch.epfl.sdp.drone3d.model.weather.WeatherReport
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.mission.MappingMissionService
import ch.epfl.sdp.drone3d.service.api.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.service.api.weather.WeatherService
import ch.epfl.sdp.drone3d.service.impl.mission.ParallelogramMappingMissionService
import ch.epfl.sdp.drone3d.service.impl.weather.WeatherUtils
import ch.epfl.sdp.drone3d.ui.ToastHandler
import ch.epfl.sdp.drone3d.ui.map.BaseMapActivity
import ch.epfl.sdp.drone3d.ui.map.MissionInProgressActivity
import ch.epfl.sdp.drone3d.ui.weather.WeatherInfoActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.Style
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ItineraryShowActivity : BaseMapActivity() {

    companion object {
        // max 1000 meters between the user/simulation and the start of the mission
        private const val MAX_BEGINNING_DISTANCE = 1000
        const val FLIGHTPATH_INTENT_PATH = "ISA_flightPath"
    }

    @Inject
    lateinit var mappingMissionDao: MappingMissionDao

    @Inject
    lateinit var droneService: DroneService

    @Inject
    lateinit var weatherService: WeatherService

    @Inject
    lateinit var authService: AuthenticationService

    private var flightPath = listOf<LatLng>()
    private lateinit var missionDrawer: MapboxMissionDrawer

    private lateinit var ownerUid: String
    private var privateId: String? = null
    private var sharedId: String? = null
    private lateinit var area: List<LatLng>
    private lateinit var strategy: MappingMissionService.Strategy
    private var flightHeight: Double = 50.0

    // true if the weather is good enough to launch the mission
    private var isWeatherGoodEnough: Boolean = false
    private lateinit var weatherReport: LiveData<WeatherReport>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        super.initMapView(savedInstanceState, R.layout.activity_itinerary_show, R.id.mapView)

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundle = intent.extras
        if (bundle != null) {
            // Get the Intent that started this activity and extract user and ids
            ownerUid = intent.getStringExtra(MissionViewAdapter.OWNER_ID_INTENT_PATH).toString()
            privateId = intent.getStringExtra(MissionViewAdapter.PRIVATE_ID_INTENT_PATH)
            sharedId = intent.getStringExtra(MissionViewAdapter.SHARED_ID_INTENT_PATH)
            flightHeight = bundle.getDouble(MissionViewAdapter.FLIGHTHEIGHT_INTENT_PATH)
            strategy =
                (bundle.get(MissionViewAdapter.STRATEGY_INTENT_PATH) as MappingMissionService.Strategy?)!!
            area = bundle.getParcelableArrayList(MissionViewAdapter.AREA_INTENT_PATH)!!
        }

        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now we can add data or make other map adjustments
                if (!::missionDrawer.isInitialized) {
                    missionDrawer = MapboxMissionDrawer(mapView, mapboxMap, mapboxMap.style!!)
                }

                val missionBuilder = ParallelogramMappingMissionService(droneService)
                flightPath = when (strategy) {
                    MappingMissionService.Strategy.SINGLE_PASS -> missionBuilder.buildSinglePassMappingMission(
                        area,
                        flightHeight
                    )
                    MappingMissionService.Strategy.DOUBLE_PASS -> missionBuilder.buildDoublePassMappingMission(
                        area,
                        flightHeight
                    )
                }!!
                missionDrawer.showMission(flightPath, false)
                MapboxUtility.zoomOnMission(flightPath, mapboxMap)
            }
        }

        findViewById<View>(R.id.mission_delete).visibility =
            if (authService.getCurrentSession()?.user?.uid == ownerUid) View.VISIBLE else View.GONE

        weatherReport = weatherService.getWeatherReport(area[0])
        weatherReport.observe(this) {
            isWeatherGoodEnough = WeatherUtils.isWeatherGoodEnough(it)

        }
    }

    /**
     * Start an intent to go to the mission in progress activity
     */
    fun launchMission(@Suppress("UNUSED_PARAMETER") view: View) {
        val dronePos = droneService.getData().getPosition().value
        val beginningPoint = flightPath[0]
        if (!droneService.isConnected()) {
            ToastHandler.showToast(this, R.string.launch_no_drone)
        } else if (dronePos == null) {
            ToastHandler.showToast(this, R.string.launch_no_drone_pos)
        } else if (dronePos.distanceTo(beginningPoint) > MAX_BEGINNING_DISTANCE) {
            ToastHandler.showToast(this, R.string.drone_too_far_from_start)
        } else if (!isWeatherGoodEnough) {
            val builder = Builder(this)
            builder.setMessage(getString(R.string.launch_mission_confirmation))
            builder.setCancelable(true)

            builder.setPositiveButton(getString(R.string.confirm_launch)) { dialog, _ ->
                dialog.cancel()
                goToMissionInProgressActivity()
            }

            builder.setNegativeButton(R.string.cancel_launch) { dialog, _ ->
                dialog.cancel()
            }
            builder.create()?.show()
        } else {
            goToMissionInProgressActivity()
        }
    }

    private fun goToMissionInProgressActivity() {
        val intent = Intent(this, MissionInProgressActivity::class.java)
        intent.putExtra(FLIGHTPATH_INTENT_PATH, ArrayList(flightPath))
        startActivity(intent)
    }

    /**
     * Show an alert asking for confirmation to delete the mission
     */
    fun deleteMission(@Suppress("UNUSED_PARAMETER") view: View) {
        val builder = Builder(this)
        builder.setMessage(getString(R.string.delete_confirmation))
        builder.setCancelable(true)

        builder.setPositiveButton(getString(R.string.confirm_delete)) { dialog, _ ->
            dialog.cancel()
            confirmDelete()
        }

        builder.setNegativeButton(R.string.cancel_delete) { dialog, _ ->
            dialog.cancel()
        }
        builder.create()?.show()
    }

    /**
     * Go to WeatherInfoActivity
     */
    fun goToWeatherInfo(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(this, WeatherInfoActivity::class.java)
        intent.putExtra(FLIGHTPATH_INTENT_PATH, ArrayList(flightPath))
        startActivity(intent)
    }

    /**
     * Delete this mapping mission and go back to the mission selection activity
     */
    private fun confirmDelete() {
        mappingMissionDao.removeMappingMission(ownerUid, privateId, sharedId)
        val intent = Intent(this, MappingMissionSelectionActivity::class.java)
        startActivity(intent)
    }
}