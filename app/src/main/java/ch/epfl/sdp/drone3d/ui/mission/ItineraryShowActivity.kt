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
import ch.epfl.sdp.drone3d.service.api.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.service.api.weather.WeatherService
import ch.epfl.sdp.drone3d.service.impl.weather.WeatherUtils
import ch.epfl.sdp.drone3d.ui.map.BaseMapActivity
import ch.epfl.sdp.drone3d.ui.map.MissionInProgressActivity
import ch.epfl.sdp.drone3d.ui.weather.WeatherInfoActivity
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
    }

    @Inject
    lateinit var mappingMissionDao: MappingMissionDao

    @Inject
    lateinit var droneService: DroneService

    @Inject
    lateinit var  weatherService: WeatherService

    private var currentMissionPath: ArrayList<LatLng>? = null
    private lateinit var missionDrawer: MapboxMissionDrawer

    private lateinit var ownerUid: String
    private var privateId: String? = null
    private var sharedId: String? = null

    // true if the weather is good enough to launch the mission
    private var isWeatherGoodEnough: Boolean = false
    private lateinit var weatherReport: LiveData<WeatherReport>

    @Inject
    lateinit var authService: AuthenticationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        super.initMapView(savedInstanceState, R.layout.activity_itinerary_show, R.id.mapView)

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get the Intent that started this activity and extract the missionPath
        currentMissionPath = intent.getParcelableArrayListExtra(MissionViewAdapter.MISSION_PATH)
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

        findViewById<View>(R.id.mission_delete).visibility =
            if (authService.getCurrentSession()?.user?.uid == ownerUid) View.VISIBLE else View.GONE

        if (currentMissionPath != null && currentMissionPath!!.isNotEmpty()) {
            weatherReport = weatherService.getWeatherReport(LatLng(currentMissionPath!![0]))
            weatherReport.observe(this) {
                isWeatherGoodEnough = WeatherUtils.isWeatherGoodEnough(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        droneService.getData().isConnected().observe(this) {
            findViewById<View>(R.id.buttonToMissionInProgressActivity).isEnabled = canMissionBeLaunched()
        }
    }

    override fun onPause() {
        super.onPause()

        droneService.getData().isConnected().removeObservers(this)
    }

    /**
     * Check if there is a connected drone, and if the user or the simulation is close enough to launch a mission
     */
    private fun canMissionBeLaunched(): Boolean {
        val dronePos = droneService.getData().getPosition().value
        return if (currentMissionPath?.isEmpty() == true || !droneService.isConnected() || dronePos == null)
            false
        else {
            val beginningPoint = currentMissionPath!![0]
            dronePos.distanceTo(beginningPoint) < MAX_BEGINNING_DISTANCE
        }
    }

    /**
     * Start an intent to go to the mission in progress activity
     */
    fun launchMission(@Suppress("UNUSED_PARAMETER") view: View) {
        if(!isWeatherGoodEnough){
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
        }else{
            goToMissionInProgressActivity()
        }
    }

    private fun goToMissionInProgressActivity(){
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
     * Go to WeatherInfoActivity
     */
    fun goToWeatherInfo(@Suppress("UNUSED_PARAMETER")view: View) {
        val intent = Intent(this, WeatherInfoActivity::class.java)
        intent.putExtra(MissionViewAdapter.MISSION_PATH, currentMissionPath)
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