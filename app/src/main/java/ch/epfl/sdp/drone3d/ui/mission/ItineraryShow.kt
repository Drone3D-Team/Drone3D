/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.map.MapboxMissionDrawer
import ch.epfl.sdp.drone3d.map.MapboxUtility
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style

class ItineraryShow : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var currentMissionPath: ArrayList<LatLng>? = null
    private lateinit var missionDrawer: MapboxMissionDrawer

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_itinerary_show)

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true);


        // Get the Intent that started this activity and extract the missionPath
        val intent = intent
        currentMissionPath = intent.getSerializableExtra(MappingMissionSelectionActivity.MISSION_PATH) as ArrayList<LatLng>?

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now we can add data or make other map adjustments
                if(!::missionDrawer.isInitialized) {
                    missionDrawer = MapboxMissionDrawer(mapView, mapboxMap, mapboxMap.style!!)
                }

                if(currentMissionPath!=null){
                    missionDrawer.showMission(currentMissionPath!!)
                    MapboxUtility.zoomOnMission(currentMissionPath!!, mapboxMap)
                }
            }
        }
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