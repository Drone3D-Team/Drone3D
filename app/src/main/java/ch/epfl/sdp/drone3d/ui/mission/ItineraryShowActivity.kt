/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.drone.DroneService
import ch.epfl.sdp.drone3d.map.MapboxMissionDrawer
import ch.epfl.sdp.drone3d.map.MapboxUtility
import ch.epfl.sdp.drone3d.service.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.ui.map.MissionInProgressActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ItineraryShowActivity : AppCompatActivity() {

    @Inject
    lateinit var mappingMissionDao: MappingMissionDao


    private lateinit var goToMissionInProgressButton: FloatingActionButton
    private lateinit var mapView: MapView
    private var currentMissionPath: ArrayList<LatLng>? = null
    private lateinit var missionDrawer: MapboxMissionDrawer

    private lateinit var ownerUid: String
    private var privateId: String? = null
    private var sharedId: String? = null

    @Inject
    lateinit var droneService: DroneService

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_itinerary_show)

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

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now we can add data or make other map adjustments
                if (!::missionDrawer.isInitialized) {
                    missionDrawer = MapboxMissionDrawer(mapView, mapboxMap, mapboxMap.style!!)
                }

                if (currentMissionPath != null) {
                    missionDrawer.showMission(currentMissionPath!!)
                    MapboxUtility.zoomOnMission(currentMissionPath!!, mapboxMap)
                }
            }
        }

        goToMissionInProgressButton = findViewById(R.id.buttonToMissionInProgressActivity)
        goToMissionInProgressButton.isEnabled = droneService.isConnected()
    }

    /**
     * Start an intent to go to the mission in progress activity
     */
    fun goToMissionInProgressActivity(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(this, MissionInProgressActivity::class.java)
        intent.putExtra(MissionViewAdapter.MISSION_PATH, currentMissionPath)
        startActivity(intent)
    }

    /**
     * Delete this mapping mission and go back to the mission selection activity
     */
    fun deleteMission(@Suppress("UNUSED_PARAMETER") view: View) {
        mappingMissionDao.removeMappingMission(ownerUid, privateId, sharedId)
        val intent = Intent(this, MappingMissionSelectionActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}