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
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.map.MapboxAreaBuilderDrawer
import ch.epfl.sdp.drone3d.map.MapboxMissionDrawer
import ch.epfl.sdp.drone3d.map.area.AreaBuilder
import ch.epfl.sdp.drone3d.map.area.ParallelogramBuilder
import ch.epfl.sdp.drone3d.map.gps.LocationComponentManager
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.impl.mission.ParallelogramMappingMissionService
import ch.epfl.sdp.drone3d.ui.map.BaseMapActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lukelorusso.verticalseekbar.VerticalSeekBar
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

    // Location
    @Inject
    lateinit var locationService: LocationService
    lateinit var locationComponentManager: LocationComponentManager

    @Inject
    lateinit var droneService: DroneService

    // Map
    private var isMapReady = false
    private lateinit var mapboxMap: MapboxMap


    // Mission
    private var isMissionDisplayed = false
    private var flightPath = arrayListOf<LatLng>()
    private var flightHeight = 50.0
    private lateinit var missionBuilder: ParallelogramMappingMissionService
    private lateinit var missionDrawer: MapboxMissionDrawer

    // Area
    private lateinit var areaBuilder: AreaBuilder
    private lateinit var areaBuilderDrawer: MapboxAreaBuilderDrawer

    // Button
    private lateinit var goToSaveButton: FloatingActionButton
    private lateinit var showMissionButton: FloatingActionButton
    private lateinit var deleteButton: FloatingActionButton
    private lateinit var altitudeButton: VerticalSeekBar

    // Text
    private lateinit var altitudeText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.initMapView(savedInstanceState, R.layout.activity_itinerary_create, R.id.mapView)

        mapView.getMapAsync(this)
        mapView.contentDescription = getString(R.string.map_not_ready)


        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Button
        goToSaveButton = findViewById(R.id.buttonToSaveActivity)
        goToSaveButton.isEnabled = false
        deleteButton = findViewById(R.id.delete_button)
        showMissionButton = findViewById(R.id.showMission)
        altitudeButton = findViewById(R.id.verticalBar)

        // TextView
        altitudeText = findViewById(R.id.altitude)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        locationComponentManager = LocationComponentManager(this, mapboxMap, locationService)

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->

            locationComponentManager.enableLocationComponent(style)

            // Mission
            missionBuilder = ParallelogramMappingMissionService(droneService)
            missionDrawer = MapboxMissionDrawer(mapView, mapboxMap, style)

            // Area - Need to be the last Drawer instanciated to allow draggable vertex
            areaBuilder = ParallelogramBuilder()
            areaBuilder.onVerticesChanged.add { areaBuilderDrawer.draw(areaBuilder) }
            areaBuilder.onAreaChanged.add { updateFlightPath() }
            areaBuilderDrawer = MapboxAreaBuilderDrawer(mapView, mapboxMap, style)
            areaBuilderDrawer.onVertexMoved.add { old, new -> areaBuilder.moveVertex(old, new) }

            mapboxMap.addOnMapClickListener(this)

            // Buttons
            showMissionButton.setOnClickListener {
                isMissionDisplayed = !isMissionDisplayed
                if (isMissionDisplayed) {
                    updateFlightPath()
                    showMissionButton.setImageDrawable(
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_eye_closed, null)
                    )
                } else {
                    showMissionButton.setImageDrawable(
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_eye_open, null)
                    )
                }
            }

            altitudeButton.setOnProgressChangeListener { progressValue ->
                flightHeight = progressValue.toDouble()
                altitudeText.text = progressValue.toString()
                updateFlightPath()
            }

            deleteButton.setOnClickListener{
                areaBuilder.reset()
                flightPath = ArrayList<LatLng>()
            }
        }

        // Used to detect when the map is ready in tests
        mapView.contentDescription = getString(R.string.map_ready)

        this.mapboxMap = mapboxMap
        isMapReady = true
    }

    /**
     * Update the mapping mission flightPath and draw it on the map if the option is activated
     */
    private fun updateFlightPath() {
        if(areaBuilder.isComplete()){
            goToSaveButton.isEnabled = authService.hasActiveSession()

            if (isMissionDisplayed) {
                val path =
                    missionBuilder.buildSinglePassMappingMission(areaBuilder.vertices, flightHeight)

                if (path != null) {
                    flightPath = ArrayList(path)
                    missionDrawer.showMission(flightPath)
                }
            }
        }

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

    fun goToSaveActivity(@Suppress("UNUSED_PARAMETER") view: View) {

        updateFlightPath()
        // TODO Replace by the actual MappingMission flight path once we are able to generate it from an area
        if (flightPath.size == 0 && areaBuilder.getShapeVertices() != null) {
            flightPath = ArrayList(areaBuilder.getShapeVertices())
        }

        val intent = Intent(this, SaveMappingMissionActivity::class.java)
        intent.putExtra("flightPath", flightPath)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isMapReady) {
            areaBuilderDrawer.onDestroy()
            areaBuilder.onDestroy()
        }
        mapView.onDestroy()
        missionDrawer.onDestroy()
    }
}