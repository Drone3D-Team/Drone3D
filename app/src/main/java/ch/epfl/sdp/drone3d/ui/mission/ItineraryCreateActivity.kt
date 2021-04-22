/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * Some elements are inspired by Fly2Find project
 */

package ch.epfl.sdp.drone3d.ui.mission


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.gps.LocationComponentManager
import ch.epfl.sdp.drone3d.map.MapboxAreaBuilderDrawer
import ch.epfl.sdp.drone3d.map.MapboxMissionDrawer
import ch.epfl.sdp.drone3d.map.area.AreaBuilder
import ch.epfl.sdp.drone3d.map.area.PolygonBuilder
import ch.epfl.sdp.drone3d.service.auth.AuthenticationService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The activity that allows the user to create itinerary using a map.
 */
@AndroidEntryPoint
class ItineraryCreateActivity : AppCompatActivity(), OnMapReadyCallback,
    MapboxMap.OnMapClickListener, MapboxMap.OnMapLongClickListener {
    @Inject
    lateinit var authService: AuthenticationService

    // Map
    private lateinit var mapView: MapView
    private var isMapReady = false
    private lateinit var mapboxMap: MapboxMap

    // Mission
    private var flightPath = arrayListOf<LatLng>()

    // Button
    private lateinit var goToSaveButton: FloatingActionButton

    // Location
    lateinit var locationComponentManager: LocationComponentManager

    // Area
    private var longClickConsumed = false
    private lateinit var areaBuilder: AreaBuilder
    private lateinit var areaBuilderDrawer: MapboxAreaBuilderDrawer

    // Drawer
    private lateinit var missionDrawer: MapboxMissionDrawer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_itinerary_create)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(this)
        mapView.contentDescription = getString(R.string.map_not_ready)


        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        goToSaveButton = findViewById(R.id.buttonToSaveActivity)
        goToSaveButton.isEnabled = authService.hasActiveSession()
    }

    fun goToSaveActivity(@Suppress("UNUSED_PARAMETER") view: View) {

        // TODO Replace by the actual MappingMission flight path once we are able to generate it from an area
        if (areaBuilder.getShapeVertices() != null){
            flightPath = ArrayList(areaBuilder.getShapeVertices());
        }

        val intent = Intent(this, SaveMappingMissionActivity::class.java)
        intent.putExtra("flightPath", flightPath)
        startActivity(intent)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        locationComponentManager = LocationComponentManager(this, mapboxMap)

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            locationComponentManager.enableLocationComponent(style)

            fun onLongClickConsumed(): Boolean {
                longClickConsumed = true;
                return false
            }

            areaBuilderDrawer =
                MapboxAreaBuilderDrawer(mapView, mapboxMap, style) { onLongClickConsumed() }
            missionDrawer = MapboxMissionDrawer(mapView, mapboxMap, style)

            mapboxMap.addOnMapClickListener(this)
            mapboxMap.addOnMapLongClickListener(this)

            areaBuilder = PolygonBuilder()
            //areaBuilder = ParallelogramBuilder()

            //areaBuilder.onAreaChanged.add { missionBuilder.withSearchArea(it) }
            areaBuilder.onVerticesChanged.add { areaBuilderDrawer.draw(areaBuilder) }
            areaBuilderDrawer.onVertexMoved.add { old, new -> areaBuilder.moveVertex(old, new) }

        }
        // Used to detect when the map is ready in tests
        mapView.contentDescription = getString(R.string.map_ready)

        this.mapboxMap = mapboxMap
        isMapReady = true
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
        if (isMapReady) {
            areaBuilderDrawer.onDestroy()
            areaBuilder.onDestroy()
        }
        mapView.onDestroy()

        //missionDrawer.onDestroy()
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationComponentManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapClick(position: LatLng): Boolean {
        try {
            areaBuilder.addVertex(position)
        } catch (e: IllegalArgumentException) {
            Toast.makeText(
                baseContext, e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
        return true
    }

    override fun onMapLongClick(point: LatLng): Boolean {
        longClickConsumed = false
        return true
    }

}