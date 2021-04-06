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
import ch.epfl.sdp.drone3d.service.storage.data.MappingMission
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style

/**
 * The activity that allows the user to create itinerary using a map.
 */
class ItineraryCreateActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_itinerary_create)

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now we can add data or make other map adjustments
                var mapmission = MappingMission("EPFL", listOf(LatLng(46.52061, 6.56794), LatLng(46.52304, 6.56358), LatLng(46.53304, 6.56958), LatLng(46.52061, 6.56794)))
                val missionDrawer = MapboxMissionDrawer(mapView,mapboxMap, mapboxMap.style!!)
                missionDrawer.showMission(mapmission)
                MapboxUtility.zoomOnMission(mapmission, mapboxMap)
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