/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * Some elements are inspired by Fly2Find project
 */

package ch.epfl.sdp.drone3d.ui.mission


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.map.MapboxAreaBuilderDrawer
import ch.epfl.sdp.drone3d.map.MapboxMissionDrawer
import ch.epfl.sdp.drone3d.map.area.AreaBuilder
import ch.epfl.sdp.drone3d.map.area.PolygonBuilder
import ch.epfl.sdp.drone3d.map.gps.LocationComponentManager
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.ui.map.BaseMapActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The activity that allows the user to create itinerary using a map.
 */
@AndroidEntryPoint
class ItineraryCreateActivity : BaseMapActivity(), OnMapReadyCallback,
    MapboxMap.OnMapClickListener {
    @Inject
    lateinit var authService: AuthenticationService

    @Inject
    lateinit var locationService: LocationService

    // Map
    private var isMapReady = false
    private lateinit var mapboxMap: MapboxMap

    // Mission
    private var flightPath = arrayListOf<LatLng>()

    // Button
    private lateinit var goToSaveButton: FloatingActionButton

    // Drawer
    private lateinit var missionDrawer: MapboxMissionDrawer
    private lateinit var areaBuilderDrawer: MapboxAreaBuilderDrawer

    // Area
    private lateinit var areaBuilder: AreaBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        super.initMapView(savedInstanceState, R.layout.activity_itinerary_create, R.id.mapView)

        mapView.getMapAsync(this)
        mapView.contentDescription = getString(R.string.map_not_ready)


        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        goToSaveButton = findViewById(R.id.buttonToSaveActivity)
        goToSaveButton.isEnabled = authService.hasActiveSession()
    }

    fun goToSaveActivity(@Suppress("UNUSED_PARAMETER") view: View) {

        // TODO Replace by the actual MappingMission flight path once we are able to generate it from an area
        if (areaBuilder.getShapeVertices() != null) {
            flightPath = ArrayList(areaBuilder.getShapeVertices())
        }

        val intent = Intent(this, SaveMappingMissionActivity::class.java)
        intent.putExtra("flightPath", flightPath)
        startActivity(intent)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            LocationComponentManager.enableLocationComponent(this, mapboxMap, locationService)
            //configureLocationOptions(style)

            areaBuilder = PolygonBuilder()
            //areaBuilder = ParallelogramBuilder()
            areaBuilder.onVerticesChanged.add { areaBuilderDrawer.draw(areaBuilder) }
            //areaBuilder.onAreaChanged.add { missionBuilder.withSearchArea(it) }


            missionDrawer = MapboxMissionDrawer(mapView, mapboxMap, style)
            // Need to be the last Drawer instanciated to allow draggable vertex
            areaBuilderDrawer =
                MapboxAreaBuilderDrawer(mapView, mapboxMap, style)
            areaBuilderDrawer.onVertexMoved.add { old, new -> areaBuilder.moveVertex(old, new) }

            mapboxMap.addOnMapClickListener(this)
        }

        // Used to detect when the map is ready in tests
        mapView.contentDescription = getString(R.string.map_ready)

        this.mapboxMap = mapboxMap
        isMapReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isMapReady) {
            areaBuilderDrawer.onDestroy()
            areaBuilder.onDestroy()
        }
        mapView.onDestroy()
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
}