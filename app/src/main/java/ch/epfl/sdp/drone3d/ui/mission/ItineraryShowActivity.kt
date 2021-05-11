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
import androidx.lifecycle.Observer
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.map.MapboxMissionDrawer
import ch.epfl.sdp.drone3d.map.MapboxUtility
import ch.epfl.sdp.drone3d.model.weather.WeatherReport
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.service.api.weather.WeatherService
import ch.epfl.sdp.drone3d.ui.map.BaseMapActivity
import ch.epfl.sdp.drone3d.ui.map.MissionInProgressActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

        // limit values for the weather
        // min 100 meters of visibility
        private const val MIN_VISIBILITY_DISTANCE = 100
        // temperature min 0 degree Celsius
        private const val MIN_TEMPERATURE = 0
        // wind speed max = 8.8 m/s
        private const val MAX_WIND_SPEED = 8.8
        // set containing the keyword where it is dangerous for the drone to be launched
        private val SAFE_CONDITIONS = setOf("Clear", "Clouds")
    }

    @Inject
    lateinit var mappingMissionDao: MappingMissionDao

    @Inject
    lateinit var droneService: DroneService

    @Inject
    lateinit var  weatherService: WeatherService

    private lateinit var goToMissionInProgressButton: FloatingActionButton
    private var currentMissionPath: ArrayList<LatLng>? = null
    private lateinit var missionDrawer: MapboxMissionDrawer

    private lateinit var ownerUid: String
    private var privateId: String? = null
    private var sharedId: String? = null

    private lateinit var deleteButton: MaterialButton

    // true if the weather is good enough to launch the mission
    private var isWeatherGoodEnough: Boolean = false
    private lateinit var weatherReport: LiveData<WeatherReport>
    private var weatherReportObserver = Observer<WeatherReport> { report ->
        isWeatherGoodEnough = report.visibility >= MIN_VISIBILITY_DISTANCE
                && report.temperature >= MIN_TEMPERATURE
                && report.windSpeed <= MAX_WIND_SPEED
                && SAFE_CONDITIONS.contains(report.keywordDescription)
    }

    @Inject
    lateinit var authService: AuthenticationService

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        super.initMapView(savedInstanceState, R.layout.activity_itinerary_show, R.id.mapView)

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get the Intent that started this activity and extract the missionPath
        @Suppress("UNCHECKED_CAST")
        currentMissionPath =
            intent.getSerializableExtra(MissionViewAdapter.MISSION_PATH) as ArrayList<LatLng>?
        // Get the Intent that started this activity and extract user and ids
        ownerUid = intent.getStringExtra(MissionViewAdapter.OWNER).toString()
        privateId = intent.getStringExtra(MissionViewAdapter.PRIVATE)
        sharedId = intent.getStringExtra(MissionViewAdapter.SHARED)

        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now we can add data or make other map adjustments
                if (!::missionDrawer.isInitialized) {
                    missionDrawer = MapboxMissionDrawer(mapView, mapboxMap, mapboxMap.style!!)
                }

                if (currentMissionPath != null) {
                    missionDrawer.showMission(currentMissionPath!!, false)
                    MapboxUtility.zoomOnMission(currentMissionPath!!, mapboxMap)
                }
            }
        }

        if (currentMissionPath != null && currentMissionPath!!.isEmpty()) {
            weatherReport = weatherService.getWeatherReport(LatLng(currentMissionPath!![0].latitude, currentMissionPath!![0].longitude))
            weatherReport.observe(this, weatherReportObserver)
        }

        deleteButton = findViewById(R.id.mission_delete)
        deleteButton.visibility =
            if (authService.getCurrentSession()?.user?.uid == ownerUid) View.VISIBLE else View.GONE

        goToMissionInProgressButton = findViewById(R.id.buttonToMissionInProgressActivity)
        goToMissionInProgressButton.isEnabled = canMissionBeLaunched()
    }

    /**
     * Check if there is a connected drone, and if the user or the simulation is close enough to launch a mission
     */
    private fun canMissionBeLaunched(): Boolean {
        return if (currentMissionPath == null || currentMissionPath!!.isEmpty()) {
            false
        } else {
            val beginningPoint = currentMissionPath!![0]
            val distanceToMission =
                beginningPoint.distanceTo(droneService.getData().getPosition().value!!)
            isWeatherGoodEnough && distanceToMission < MAX_BEGINNING_DISTANCE
        }
    }

    /**
     * Start an intent to go to the mission in progress activity
     */
    fun goToMissionInProgressActivity(@Suppress("UNUSED_PARAMETER") view: View) {
        if (this::weatherReport.isInitialized) { weatherReport.removeObserver(weatherReportObserver) }
        val intent = Intent(this, MissionInProgressActivity::class.java)
        intent.putExtra(MissionViewAdapter.MISSION_PATH, currentMissionPath)
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
     * Delete this mapping mission and go back to the mission selection activity
     */
    private fun confirmDelete() {
        if (this::weatherReport.isInitialized) { weatherReport.removeObserver(weatherReportObserver) }
        mappingMissionDao.removeMappingMission(ownerUid, privateId, sharedId)
        val intent = Intent(this, MappingMissionSelectionActivity::class.java)
        startActivity(intent)
    }
}