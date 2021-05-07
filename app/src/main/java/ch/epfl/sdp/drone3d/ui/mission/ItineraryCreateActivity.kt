/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * Some elements are inspired by Fly2Find project
 */

package ch.epfl.sdp.drone3d.ui.mission


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.VisibleForTesting
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
import ch.epfl.sdp.drone3d.service.api.mission.MappingMissionService.Strategy
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
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public lateinit var mapboxMap: MapboxMap


    // Mission
    private var isMissionDisplayed = true
    private var flightPath = arrayListOf<LatLng>()
    private var flightHeight = 50.0
    private lateinit var missionBuilder: ParallelogramMappingMissionService
    private lateinit var missionDrawer: MapboxMissionDrawer
    private var flightPathShouldBeReGenerated = false;
    private var strategy = Strategy.SINGLE_PASS

    // Area
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public lateinit var areaBuilder: AreaBuilder
    private lateinit var areaBuilderDrawer: MapboxAreaBuilderDrawer

    // Button
    private lateinit var altitudeButton: VerticalSeekBar
    private lateinit var changeStrategyButton: FloatingActionButton
    private lateinit var buildMissionButton: FloatingActionButton
    private lateinit var showMissionButton: FloatingActionButton
    private lateinit var deleteButton: FloatingActionButton
    private lateinit var goToSaveButton: FloatingActionButton

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
        altitudeButton = findViewById(R.id.verticalBar)
        changeStrategyButton = findViewById(R.id.changeStrategy)
        buildMissionButton = findViewById(R.id.buildFlightPath)
        buildMissionButton.isEnabled = false
        showMissionButton = findViewById(R.id.showMission)
        deleteButton = findViewById(R.id.delete_button)
        deleteButton.isEnabled = false
        goToSaveButton = findViewById(R.id.buttonToSaveActivity)
        goToSaveButton.isEnabled = false


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
            areaBuilder.onVerticesChanged.add { deleteButton.isEnabled = true }

            areaBuilder.onAreaChanged.add { onMissionSettingModified() }
            areaBuilderDrawer = MapboxAreaBuilderDrawer(mapView, mapboxMap, style)
            areaBuilderDrawer.onVertexMoved.add { old, new -> areaBuilder.moveVertex(old, new) }

            mapboxMap.addOnMapClickListener(this)

            // Buttons
            altitudeButton.setOnProgressChangeListener { progressValue ->
                flightHeight = progressValue.toDouble()
                altitudeText.text = progressValue.toString()
                onMissionSettingModified()
            }
        }

        // Used to detect when the map is ready in tests
        mapView.contentDescription = getString(R.string.map_ready)

        this.mapboxMap = mapboxMap
        isMapReady = true
    }

    private fun onMissionSettingModified() {
        if (areaBuilder.isComplete()) {
            buildMissionButton.isEnabled = true
            flightPathShouldBeReGenerated = true
        }
        if(areaBuilder.vertices.isNotEmpty() || flightPath.isNotEmpty()){
            deleteButton.isEnabled = true
        }
    }

    /**
     * Erase all drawing on the map and delete previously built flightPath
     */
    fun eraseAll(@Suppress("UNUSED_PARAMETER") view: View){
        areaBuilder.reset()
        flightPath = ArrayList<LatLng>()
        missionDrawer.showMission(listOf(), false)
        deleteButton.isEnabled = false
        goToSaveButton.isEnabled = false
    }

    /**
     * Hide or show the flightPath
     * Does not rebuild the flightPath
     */
    fun switchFlightPathVisibility(@Suppress("UNUSED_PARAMETER") view: View) {
        isMissionDisplayed = !isMissionDisplayed
        if (isMissionDisplayed) {
            missionDrawer.showMission(flightPath, false)
            showMissionButton.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.ic_eye_open, null)
            )
        } else {
            showMissionButton.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.ic_eye_desactivated, null)
            )
            missionDrawer.showMission(listOf(), false)
        }
    }

    /**
     * Switch between the strategies
     */
    fun switchStrategy(@Suppress("UNUSED_PARAMETER") view: View) {
        strategy = when (strategy) {
            Strategy.SINGLE_PASS ->{
                changeStrategyButton.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_double_path_strategy, null))
                Strategy.DOUBLE_PASS
            }
            Strategy.DOUBLE_PASS -> {
                changeStrategyButton.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_single_path_strategy, null))
                Strategy.SINGLE_PASS
            }
        }
        onMissionSettingModified()
    }

    /**
     * Generated a flightpath based on the mission settings
     * Should be connected to a drone or a simulation
     */
    fun buildFlightPath(@Suppress("UNUSED_PARAMETER") view: View) {
        buildMissionButton.isEnabled = false
        if (flightPathShouldBeReGenerated) {
            flightPathShouldBeReGenerated = false
            if (areaBuilder.isComplete()) {

                val path = when (strategy) {
                    Strategy.SINGLE_PASS -> missionBuilder.buildSinglePassMappingMission(areaBuilder.vertices, flightHeight)
                    Strategy.DOUBLE_PASS -> missionBuilder.buildDoublePassMappingMission(areaBuilder.vertices, flightHeight)
                }

                if (path != null) {
                    flightPath = ArrayList(path)
                    if (isMissionDisplayed) {
                        missionDrawer.showMission(flightPath, false)
                    }
                    goToSaveButton.isEnabled = authService.hasActiveSession()
                }else{
                    Toast.makeText(
                        baseContext, R.string.drone_should_be_connected,
                        Toast.LENGTH_SHORT
                    ).show()
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

    /**
     * Add a vertex on the map or display a toast if all vertices have been placed already
     */
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

    /**
     * Go to SaveMappingMissionActivity but first check if the flight path is up to date and if not warn the user
     */
    fun onSaved(@Suppress("UNUSED_PARAMETER") view: View) {
        if(flightPathShouldBeReGenerated){
            val builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.save_without_updating_confirmation))
            builder.setCancelable(true)

            builder.setPositiveButton(getString(R.string.confirm_save_without_updating)) { dialog, _ ->
                dialog.cancel()
                goToSaveActivity()
            }

            builder.setNegativeButton(R.string.cancel_save_without_updating) { dialog, _ ->
                dialog.cancel()
            }
            builder.create()?.show()
        }else{
            goToSaveActivity()
        }
    }

    /**
     * Launch SaveMappingMissionActivity and transfer flightpath
     */
    private fun goToSaveActivity(){
        val intent = Intent(this, SaveMappingMissionActivity::class.java)
        intent.putExtra("flightPath", flightPath)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isMapReady) {
            areaBuilderDrawer.onDestroy()
            areaBuilder.onDestroy()
            missionDrawer.onDestroy()
        }

        mapView.onDestroy()
    }
}