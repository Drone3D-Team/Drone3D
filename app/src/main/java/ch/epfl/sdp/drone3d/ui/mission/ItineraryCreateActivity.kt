/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */


package ch.epfl.sdp.drone3d.ui.mission


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.service.auth.AuthenticationService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.gps.LocationComponentManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * The activity that allows the user to create itinerary using a map.
 */
@AndroidEntryPoint
class ItineraryCreateActivity : AppCompatActivity() {
    private lateinit var mapView: MapView

    private lateinit var goToSaveButton: FloatingActionButton

    private var flightPath = arrayListOf<LatLng>()

    @Inject
    lateinit var authService: AuthenticationService

    lateinit var locationComponentManager: LocationComponentManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_itinerary_create)

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->

            locationComponentManager = LocationComponentManager(this, mapboxMap)
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now we can add data or make other map adjustments

                locationComponentManager.enableLocationComponent(it)

            }
        }
        goToSaveButton = findViewById(R.id.buttonToSaveActivity)
        goToSaveButton.isEnabled = authService.hasActiveSession()
    }

    fun goToSaveActivity(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(this, SaveMappingMissionActivity::class.java)
        intent.putExtra("flightPath", flightPath)
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

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationComponentManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}