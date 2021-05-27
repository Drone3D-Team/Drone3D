/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * Some elements are inspired by Fly2Find project
 */

package ch.epfl.sdp.drone3d.ui.mission

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
import ch.epfl.sdp.drone3d.map.MapboxUtility
import ch.epfl.sdp.drone3d.map.area.AreaBuilder
import ch.epfl.sdp.drone3d.map.area.ParallelogramBuilder
import ch.epfl.sdp.drone3d.map.gps.LocationComponentManager
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.api.mission.MappingMissionService.Strategy
import ch.epfl.sdp.drone3d.service.impl.mission.ParallelogramMappingMissionService
import ch.epfl.sdp.drone3d.ui.ToastHandler
import ch.epfl.sdp.drone3d.ui.auth.LoginActivity
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

    @Inject
    lateinit var droneService: DroneService

    // Map
    private var isMapReady = false

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var mapboxMap: MapboxMap

    // Mission
    private var flightPath = arrayListOf<LatLng>()
    private var flightHeight = DEFAULT_FLIGHTHEIGHT
    private var strategy = DEFAULT_STRATEGY
    private lateinit var missionBuilder: ParallelogramMappingMissionService
    private lateinit var missionDrawer: MapboxMissionDrawer
    private var isPreviewUpToDate = true
    private var isMissionDisplayed = true

    // Area
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var areaBuilder: AreaBuilder
    private lateinit var areaBuilderDrawer: MapboxAreaBuilderDrawer
    private var initialArea = listOf<LatLng>()


    // Button
    private lateinit var altitudeButton: VerticalSeekBar
    private lateinit var changeStrategyButton: FloatingActionButton
    private lateinit var buildMissionButton: FloatingActionButton
    private lateinit var showMissionButton: FloatingActionButton
    private lateinit var deleteButton: FloatingActionButton
    private lateinit var goToSaveButton: FloatingActionButton

    // Text
    private lateinit var altitudeText: TextView

    companion object {
        const val STRATEGY_INTENT_PATH = "ICA_strategy"
        const val AREA_INTENT_PATH = "ICA_area"
        const val FLIGHTHEIGHT_INTENT_PATH = "ICA_flightHeight"
        const val DEFAULT_FLIGHTHEIGHT = 50.0
        const val MINIMUM_FLIGHTHEIGHT = 3.0
        val DEFAULT_STRATEGY = Strategy.SINGLE_PASS

        // Maximum area size in m2
        const val MAXIMUM_AREA_SIZE = 10000.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.initMapView(savedInstanceState, R.layout.activity_itinerary_create, R.id.mapView)

        mapView.getMapAsync(this)
        mapView.contentDescription = getString(R.string.map_not_ready)


        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundle = intent.extras
        if (bundle != null && !bundle.isEmpty) {
            flightHeight = bundle.getDouble(ItineraryShowActivity.FLIGHTHEIGHT_INTENT_PATH)
            strategy = (bundle.get(ItineraryShowActivity.STRATEGY_INTENT_PATH) as Strategy)
            initialArea = bundle.getParcelableArrayList(ItineraryShowActivity.AREA_INTENT_PATH)!!
        }

        // Button
        altitudeButton = findViewById(R.id.verticalBar)
        altitudeButton.progress = flightHeight.toInt()
        changeStrategyButton = findViewById(R.id.changeStrategy)
        setStrategyButtonIcon()
        buildMissionButton = findViewById(R.id.buildFlightPath)
        buildMissionButton.isEnabled = false
        showMissionButton = findViewById(R.id.showMission)
        deleteButton = findViewById(R.id.delete_button)
        deleteButton.isEnabled = false
        goToSaveButton = findViewById(R.id.buttonToSaveActivity)
        goToSaveButton.isEnabled = false
        setButtonIconToSaveOrLaunchMission()

        if (!authService.hasActiveSession()) {
            setDialogNotLogin()
        }


        // TextView
        altitudeText = findViewById(R.id.altitude)
    }

    /**
     * Warn the user that he is not login and he won't be able to save his mission
     */
    private fun setDialogNotLogin() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.warning_not_login))
        builder.setCancelable(false)

        builder.setPositiveButton(getString(R.string.go_to_login)) { dialog, _ ->
            dialog.cancel()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        builder.setNegativeButton(R.string.no_saving_possible) { dialog, _ ->
            dialog.cancel()
        }
        builder.create()?.show()
    }

    /**
     * If the user as an active session display save icon, otherwise send icon is displayed on the bottom button
     */
    private fun setButtonIconToSaveOrLaunchMission() {
        goToSaveButton.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                if(authService.hasActiveSession()) android.R.drawable.ic_menu_save else android.R.drawable.ic_menu_send,
                null
            )
        )
    }

    /**
     * Update the strategy button icon based on the current strategy
     */
    private fun setStrategyButtonIcon() {
        changeStrategyButton.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                when (strategy) {
                    Strategy.SINGLE_PASS -> R.drawable.ic_single_path_strategy
                    Strategy.DOUBLE_PASS -> R.drawable.ic_double_path_strategy
                },
                null
            )
        )
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            //configureLocationOptions
            LocationComponentManager.enableLocationComponent(this, mapboxMap, locationService)

            // Mission
            missionBuilder = ParallelogramMappingMissionService(droneService)
            missionDrawer = MapboxMissionDrawer(mapView, mapboxMap, style)

            areaBuilderDrawer = MapboxAreaBuilderDrawer(mapView, mapboxMap, style)

            // Area - Need to be the last Drawer instanciated to allow draggable vertex
            areaBuilder = ParallelogramBuilder()
            areaBuilder.onAreaChanged.add { onMissionSettingModified() }
            areaBuilder.onVerticesChanged.add { areaBuilderDrawer.draw(areaBuilder) }
            areaBuilder.onVerticesChanged.add { deleteButton.isEnabled = true }
            initialArea.forEach { areaBuilder.addVertex(it) }
            if (initialArea.isNotEmpty()) {
                MapboxUtility.zoomOnCoordinate(initialArea[0], mapboxMap)
            }

            areaBuilderDrawer.onVertexMoved.add { old, new -> areaBuilder.moveVertex(old, new) }

            mapboxMap.addOnMapClickListener(this)

            // Buttons
            altitudeText.text = getString(R.string.altitude_text, flightHeight)
            altitudeButton.setOnProgressChangeListener { progressValue ->
                flightHeight = progressValue.toDouble() + MINIMUM_FLIGHTHEIGHT
                altitudeText.text = getString(R.string.altitude_text, flightHeight)
                onMissionSettingModified()
            }
            // Used to detect when the map is ready in tests
            mapView.contentDescription = getString(R.string.map_ready)

            this.mapboxMap = mapboxMap
            isMapReady = true
        }
    }

    private fun onMissionSettingModified() {
        if (areaBuilder.isComplete()) {
            isPreviewUpToDate = false
            buildMissionButton.isEnabled = true
            goToSaveButton.isEnabled = true
        }
        if (areaBuilder.vertices.isNotEmpty() || flightPath.isNotEmpty()) {
            deleteButton.isEnabled = true
        }
    }

    /**
     * Erase all drawing on the map and delete previously built flightPath
     */
    fun eraseAll(@Suppress("UNUSED_PARAMETER") view: View) {
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
            Strategy.SINGLE_PASS ->
                Strategy.DOUBLE_PASS
            Strategy.DOUBLE_PASS ->
                Strategy.SINGLE_PASS
        }
        setStrategyButtonIcon()
        onMissionSettingModified()
    }

    /**
     * Generated a flightpath based on the mission settings
     * Should be connected to a drone or a simulation
     */
    fun buildFlightPath(@Suppress("UNUSED_PARAMETER") view: View) {
        buildMissionButton.isEnabled = false

        if (areaBuilder.isComplete()) {
            if (!isSizeWithinLimit()) {
                ToastHandler.showToast(this, R.string.area_size_to_big)
                return
            }
            if (!isPreviewUpToDate) {
                isPreviewUpToDate = true

                val path = when (strategy) {
                    Strategy.SINGLE_PASS -> missionBuilder.buildSinglePassMappingMission(
                        areaBuilder.vertices,
                        flightHeight
                    )
                    Strategy.DOUBLE_PASS -> missionBuilder.buildDoublePassMappingMission(
                        areaBuilder.vertices,
                        flightHeight
                    )
                }

                flightPath = ArrayList(path)
                if (isMissionDisplayed) {
                    missionDrawer.showMission(flightPath, false)
                }

            }
        }
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

    /**
     * Go to SaveMappingMissionActivity but first check if the flight path is up to date and if not warn the user
     */
    fun onSaved(@Suppress("UNUSED_PARAMETER") view: View) {
        if (!isSizeWithinLimit()) {
            ToastHandler.showToast(this, R.string.area_size_to_big)
        } else if (authService.hasActiveSession()) {
            if (!isPreviewUpToDate) {
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
            } else {
                goToSaveActivity()
            }
        } else {
            val intent = Intent(this, ItineraryShowActivity::class.java)
            intent.putExtra(MissionViewAdapter.FLIGHTHEIGHT_INTENT_PATH, flightHeight)
            intent.putExtra(MissionViewAdapter.AREA_INTENT_PATH, ArrayList(areaBuilder.vertices))
            intent.putExtra(MissionViewAdapter.STRATEGY_INTENT_PATH, strategy)

            startActivity(intent)
            finish()
        }
    }

    /**
     * Test if the size of the area is with the accepted size
     */
    private fun isSizeWithinLimit(): Boolean {
        return areaBuilder.isComplete() && areaBuilder.getAreaSize() < MAXIMUM_AREA_SIZE
    }


    /**
     * Launch SaveMappingMissionActivity and transfer flightpath
     */
    private fun goToSaveActivity() {
        val intent = Intent(this, SaveMappingMissionActivity::class.java)
        intent.putExtra(FLIGHTHEIGHT_INTENT_PATH, flightHeight)
        intent.putExtra(AREA_INTENT_PATH, ArrayList(areaBuilder.vertices))
        intent.putExtra(STRATEGY_INTENT_PATH, strategy)

        startActivity(intent)
        finish()
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
