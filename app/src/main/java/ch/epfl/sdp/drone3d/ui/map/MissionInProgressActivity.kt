/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.map

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.drone.DroneService
import ch.epfl.sdp.drone3d.drone.DroneServiceImpl
import ch.epfl.sdp.drone3d.map.MapUtils
import ch.epfl.sdp.drone3d.map.MapboxDronePainter
import ch.epfl.sdp.drone3d.map.MapboxHomePainter
import ch.epfl.sdp.drone3d.map.MapboxMissionPainter
import ch.epfl.sdp.drone3d.service.storage.data.LatLong
import ch.epfl.sdp.drone3d.ui.ToastHandler
import com.google.android.material.button.MaterialButton
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import dagger.hilt.android.AndroidEntryPoint
import io.mavsdk.telemetry.Telemetry
import org.jetbrains.annotations.NotNull
import javax.inject.Inject
import kotlin.math.abs

/**
 * This class is heavily inspired by the class MapActivity.kt from the Fly2Find project. It has a few
 * adaptations :
 *
 * - deletion of some values we don't use in our implementation
 * - removal of the creation of a mission, it is done by another activity in our project
 * - addition of the use of the camera of the drone, and a video feed on the layout of this activity
 * - a few minor adaptations to make the class compatible with our project
 */
@AndroidEntryPoint
class MissionInProgressActivity : BaseMapActivity(), OnMapReadyCallback {
    @Inject
    lateinit var droneService: DroneService

    private lateinit var mapboxMap: MapboxMap

    private lateinit var cameraView: VideoView

    private lateinit var missionPainter: MapboxMissionPainter
    private lateinit var dronePainter: MapboxDronePainter
    private lateinit var homePainter: MapboxHomePainter

    private var dronePositionObserver = Observer<LatLong> { newLatLong ->
        newLatLong?.let { if (::dronePainter.isInitialized) dronePainter.paint(it) }
    }

    private var homePositionObserver = Observer<Telemetry.Position> { newPosition: Telemetry.Position? ->
        newPosition?.let {
            if (::homePainter.isInitialized) homePainter.paint(LatLng(it.latitudeDeg, it.longitudeDeg))
        }
    }

    private var droneFlyingStatusObserver = Observer<Boolean> {
        stopMissionButton.visibility = if (it) View.VISIBLE else View.GONE
    }

    private var droneConnectionStatusObserver = Observer<Boolean> { connectionStatus ->
        if (!connectionStatus) {
            ToastHandler.showToastAsync(this, R.string.lost_connection_message, Toast.LENGTH_SHORT)
        }
        stopMissionButton.isEnabled = connectionStatus
    }

    private var videoStreamUriObserver = Observer<String> {
        cameraView.setVideoURI(Uri.parse(it))
        cameraView.requestFocus()
        cameraView.start()
    }

    private lateinit var stopMissionButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        droneService = DroneServiceImpl

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        initMapView(savedInstanceState, R.layout.activity_mission_in_progress, R.id.map_in_mission_view)
        mapView.getMapAsync(this)

        cameraView = findViewById(R.id.camera_mission_view)
        cameraView.setMediaController(object : MediaController(this) {})

        stopMissionButton = findViewById(R.id.stopMissionButton)
    }

    override fun onMapReady(@NotNull mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            homePainter = MapboxHomePainter(mapView, mapboxMap, style)
            dronePainter = MapboxDronePainter(mapView, mapboxMap, style)
            missionPainter = MapboxMissionPainter(mapView, mapboxMap, style)

            centerCameraOnDrone(mapView)

            Transformations.map(droneService.getData().getMissionPlan()) { mission ->
                return@map mission.missionItems.map { item ->
                    LatLng(item.latitudeDeg, item.longitudeDeg)
                }
            }.observe(this, Observer {
                missionPainter.paint(it)
            })
        }
    }

    /**
     * Centers the camera on the drone
     */
    private fun centerCameraOnDrone(@Suppress("UNUSED_PARAMETER") view: View) {
        val currentZoom = mapboxMap.cameraPosition.zoom
        if (droneService.getData().getPosition().value != null) {
            mapboxMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    MapUtils.toLatLng(droneService.getData().getPosition().value)!!,
                if (abs(currentZoom - DEFAULT_ZOOM) < ZOOM_TOLERANCE) currentZoom else DEFAULT_ZOOM
                ))
        }
    }

    override fun onResume() {
        super.onResume()

        droneService.getData().getPosition().observe(this, dronePositionObserver)
        droneService.getData().getHomeLocation().observe(this, homePositionObserver)
        droneService.getData().isFlying().observe(this, droneFlyingStatusObserver)
        droneService.getData().isConnected().observe(this, droneConnectionStatusObserver)
        droneService.getData().getVideoStreamUri().observe(this, videoStreamUriObserver)
    }

    override fun onPause() {
        super.onPause()

        droneService.getData().getPosition().removeObserver(dronePositionObserver)
        droneService.getData().getHomeLocation().removeObserver(homePositionObserver)
        droneService.getData().isFlying().removeObserver(droneFlyingStatusObserver)
        droneService.getData().isConnected().removeObserver(droneConnectionStatusObserver)
        droneService.getData().getVideoStreamUri().removeObserver(videoStreamUriObserver)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this::dronePainter.isInitialized) dronePainter.onDestroy()
        if (this::missionPainter.isInitialized) missionPainter.onDestroy()
        if (this::homePainter.isInitialized) homePainter.onDestroy()
    }

    companion object {
        private const val DEFAULT_ZOOM: Double = 17.0
        private const val ZOOM_TOLERANCE: Double = 2.0
    }
}