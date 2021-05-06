/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.map

import android.content.Intent
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.map.*
import ch.epfl.sdp.drone3d.service.api.drone.DroneData.DroneStatus
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.impl.drone.DroneUtils
import ch.epfl.sdp.drone3d.ui.ToastHandler
import ch.epfl.sdp.drone3d.ui.mission.ItineraryShowActivity
import ch.epfl.sdp.drone3d.ui.mission.MissionViewAdapter
import com.google.android.material.button.MaterialButton
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import dagger.hilt.android.AndroidEntryPoint
import io.mavsdk.telemetry.Telemetry
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs

/**
 * This class show the drone on a map while it is doing a mission, as well as what the drone is currently
 * seeing.
 *
 * This class is heavily inspired by the class MapActivity.kt from the Fly2Find project. It has a few
 * adaptations :
 *
 * - deletion of some values we don't use in our implementation
 * - removal of the creation of a mission, it is done by another activity in our project
 * - addition of the use of the camera of the drone, and a video feed on the layout of this activity
 * - a few minor adaptations to make the class compatible with our project
 */
@AndroidEntryPoint
class MissionInProgressActivity : BaseMapActivity() {

    @Inject lateinit var droneService: DroneService
    @Inject lateinit var locationService: LocationService

    private val disposables = CompositeDisposable()
    private lateinit var mapboxMap: MapboxMap
    private lateinit var cameraView: SurfaceView

    private lateinit var missionDrawer: MapboxMissionDrawer
    private lateinit var droneDrawer: MapboxDroneDrawer
    private lateinit var homeDrawer: MapboxHomeDrawer

    private var missionPath: ArrayList<LatLng>? = null

    private var dronePositionObserver = Observer<LatLng> { newLatLng ->
        newLatLng?.let { if (::droneDrawer.isInitialized) droneDrawer.showDrone(newLatLng) }
    }
    private var homePositionObserver = Observer<Telemetry.Position> { newPosition: Telemetry.Position? ->
        newPosition?.let {
            if (::homeDrawer.isInitialized) homeDrawer.showHome(LatLng(newPosition.latitudeDeg, newPosition.longitudeDeg))
        }
    }

    private var droneStatusObserver = Observer<DroneStatus> { status ->
        val visibility =
                if (status == DroneStatus.EXECUTING_MISSION) View.VISIBLE else View.GONE

        backToHomeButton.visibility = visibility
        backToUserButton.visibility = visibility
    }

    private var droneConnectionStatusObserver = Observer<Boolean> { connectionStatus ->
        if (!connectionStatus) {
            ToastHandler.showToastAsync(this, R.string.lost_connection_message, Toast.LENGTH_SHORT)
        }
        backToHomeButton.isEnabled = connectionStatus
        backToUserButton.isEnabled = connectionStatus
    }

    private var videoStreamUriObserver = Observer<String> { streamUri ->
        // TODO View stream
    }

    private var speedObserver = Observer<Float> { speed ->
        speedLiveText.apply {
            text = getString(R.string.live_speed, speed.toString())
        }
    }

    private var altitudeObserver = Observer<Float> { altitude ->
        altitudeLiveText.apply {
            text = getString(R.string.live_altitude, altitude.toString())
        }
    }

    private var batteryObserver = Observer<Float> { batteryLevel ->
        batteryLiveText.apply {
            text = getString(R.string.live_battery, batteryLevel.toString())
        }
    }

    private var distanceUserObserver = Observer<LatLng> { position ->
        if (locationService.isLocationEnabled()) {
            val distanceUser = position.distanceTo(locationService.getCurrentLocation()!!)
            distanceUserLiveText.apply {
                text = getString(R.string.live_distance_user, distanceUser.toString())
            }
        } else {
            distanceUserLiveText.apply {
                text = getString(R.string.user_location_deactivated)
            }
        }
    }

    private var statusObserver = Observer<DroneStatus> { status ->
        statusLiveText.apply {
            text = getString(R.string.live_status, status.name)
        }
    }

    private lateinit var backToHomeButton: MaterialButton
    private lateinit var backToUserButton: MaterialButton

    private lateinit var speedLiveText: TextView
    private lateinit var altitudeLiveText: TextView
    private lateinit var batteryLiveText: TextView
    private lateinit var distanceUserLiveText: TextView
    private lateinit var statusLiveText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("UNCHECKED_CAST")
        missionPath = intent.getSerializableExtra(MissionViewAdapter.MISSION_PATH) as ArrayList<LatLng>?

        initMapView(savedInstanceState,
            R.layout.activity_mission_in_progress,
            R.id.map_in_mission_view)

        mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                setupMapboxMap(mapView, style)
            }
        }

        cameraView = findViewById(R.id.camera_mission_view)

        backToHomeButton = findViewById(R.id.backToHomeButton)
        backToUserButton = findViewById(R.id.backToUserButton)

        speedLiveText = findViewById(R.id.speedLive)
        altitudeLiveText = findViewById(R.id.altitudeLive)
        batteryLiveText = findViewById(R.id.batteryLive)
        distanceUserLiveText = findViewById(R.id.distanceUserLive)
        statusLiveText = findViewById(R.id.statusLive)

        startMission()
    }

    private fun setupMapboxMap(mapView: MapView, style: Style) {
        homeDrawer = MapboxHomeDrawer(mapView, mapboxMap, style)
        droneDrawer = MapboxDroneDrawer(mapView, mapboxMap, style)
        missionDrawer = MapboxMissionDrawer(mapView, mapboxMap, style)

        centerCameraOnDrone()

        Transformations.map(droneService.getData().getMission()) { mission ->
            return@map mission?.let {
                it.map { item ->
                    LatLng(item.latitudeDeg, item.longitudeDeg)
                }
            }
        }.observe(this, { path ->
            if(path != null) missionDrawer.showMission(path, false)
        })
    }

    /**
     * Centers the camera on the drone
     */
    private fun centerCameraOnDrone() {
        val currentZoom = mapboxMap.cameraPosition.zoom
        droneService.getData().getPosition().value?.let {
            mapboxMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    it,
                    if (abs(currentZoom - DEFAULT_ZOOM) < ZOOM_TOLERANCE) currentZoom else DEFAULT_ZOOM
                ))
        }
    }

    /**
     * Launch the mission
     */
    private fun startMission() {
        if(missionPath == null) {
            ToastHandler.showToastAsync(this, R.string.mission_null)
        } else {
            val droneMission = DroneUtils.makeDroneMission(missionPath!!, 20f)
            try {
                val completable = droneService.getExecutor().startMission(this, droneMission)

                disposables.add(
                    completable.subscribe({
                        val intent = Intent(this, ItineraryShowActivity::class.java)
                        intent.putExtra(MissionViewAdapter.MISSION_PATH, missionPath)
                        startActivity(intent)
                    }, {
                        throw it
                    })
                )
            } catch (e: Exception) {
                ToastHandler.showToastAsync(
                        this,
                        R.string.drone_mission_error,
                        Toast.LENGTH_LONG,
                        e.message)
                Timber.e(e)
            }
        }
    }

    /**
     * Stop the mission and bring back the drone to its home
     */
    fun backToHome(@Suppress("UNUSED_PARAMETER") view: View) {
        val completable = droneService.getExecutor().returnToHomeLocationAndLand(this)

        disposables.add(
            completable.subscribe({
                ToastHandler.showToastAsync(this, "The drone is coming back to its launch location")
            }, { e ->
                ToastHandler.showToastAsync(
                    this,
                    R.string.drone_mission_error,
                    Toast.LENGTH_LONG,
                    e.message)
                Timber.e(e)
            })
        )
    }

    /**
     * Stop the mission and bring back the drone to the user
     */
    fun backToUser(@Suppress("UNUSED_PARAMETER") view: View) {
        val completable = droneService.getExecutor().returnToUserLocationAndLand(this)

        disposables.add(
            completable.subscribe({
                ToastHandler.showToastAsync(this, "The drone is coming back to you")
            }, { e ->
                ToastHandler.showToastAsync(
                    this,
                    R.string.drone_mission_error,
                    Toast.LENGTH_LONG,
                    e.message)
                Timber.e(e)
            })
        )
    }

    override fun onResume() {
        super.onResume()

        droneService.getData().getPosition().observe(this, dronePositionObserver)
        droneService.getData().getHomeLocation().observe(this, homePositionObserver)
        droneService.getData().getDroneStatus().observe(this, droneStatusObserver)
        droneService.getData().isConnected().observe(this, droneConnectionStatusObserver)
        droneService.getData().getVideoStreamUri().observe(this, videoStreamUriObserver)

        // setup observers for live info given to user
        droneService.getData().getSpeed().observe(this, speedObserver)
        droneService.getData().getRelativeAltitude().observe(this, altitudeObserver)
        droneService.getData().getBatteryLevel().observe(this, batteryObserver)
        droneService.getData().getPosition().observe(this, distanceUserObserver)
        droneService.getData().getDroneStatus().observe(this, statusObserver)
    }

    override fun onPause() {
        super.onPause()

        droneService.getData().getPosition().removeObserver(dronePositionObserver)
        droneService.getData().getHomeLocation().removeObserver(homePositionObserver)
        droneService.getData().getDroneStatus().removeObserver(droneStatusObserver)
        droneService.getData().isConnected().removeObserver(droneConnectionStatusObserver)
        droneService.getData().getVideoStreamUri().removeObserver(videoStreamUriObserver)

        // remove observers for live info given to user
        droneService.getData().getSpeed().removeObserver(speedObserver)
        droneService.getData().getRelativeAltitude().removeObserver(altitudeObserver)
        droneService.getData().getBatteryLevel().removeObserver(batteryObserver)
        droneService.getData().getPosition().removeObserver(distanceUserObserver)
        droneService.getData().getDroneStatus().removeObserver(statusObserver)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this::droneDrawer.isInitialized) droneDrawer.onDestroy()
        if (this::missionDrawer.isInitialized) missionDrawer.onDestroy()
        if (this::homeDrawer.isInitialized) homeDrawer.onDestroy()

        disposables.dispose()
    }

    companion object {
        private const val DEFAULT_ZOOM: Double = 17.0
        private const val ZOOM_TOLERANCE: Double = 2.0
    }
}