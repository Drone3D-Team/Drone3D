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
import ch.epfl.sdp.drone3d.drone.DroneData
import ch.epfl.sdp.drone3d.map.MapUtils
import ch.epfl.sdp.drone3d.map.MapboxDronePainter
import ch.epfl.sdp.drone3d.map.MapboxHomePainter
import ch.epfl.sdp.drone3d.map.MapboxMissionPainter
import ch.epfl.sdp.drone3d.service.storage.data.LatLong
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
import java.lang.Math.abs
import javax.inject.Inject

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
    lateinit var droneData: DroneData

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

    private var droneConnectionStatusObserver = Observer<Boolean> {
        if (!it) {
            Toast.makeText(this, getString(R.string.lost_connection_message), Toast.LENGTH_SHORT).show()
        }
        stopMissionButton.isEnabled = it
    }

    private var videoStreamUriObserver = Observer<String> {
        cameraView.setVideoURI(Uri.parse(it))
        cameraView.requestFocus()
        cameraView.start()
    }

    private lateinit var stopMissionButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        initMapView(savedInstanceState, R.layout.activity_mission_in_progress, R.id.mapView)
        mapView.getMapAsync(this)

        cameraView = findViewById(R.id.camera_mission_view)
        cameraView.setMediaController(object : MediaController(this) {})
    }

    override fun onMapReady(@NotNull mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            homePainter = MapboxHomePainter(mapView, mapboxMap, style)
            dronePainter = MapboxDronePainter(mapView, mapboxMap, style)
            missionPainter = MapboxMissionPainter(mapView, mapboxMap, style)

            centerCameraOnDrone(mapView)

            Transformations.map(droneData.mission) { mission ->
                return@map mission?.map { item ->
                    LatLng(item.latitude, item.longitude)
                }
            }.observe(this, Observer {
                missionPainter.paint(it)
            })
        }
    }

    /**
     * Centers the camera on the drone
     */
    fun centerCameraOnDrone(v: View) {
        val currentZoom = mapboxMap.cameraPosition.zoom
        if (droneData.position.value != null) {
            mapboxMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    MapUtils.toLatLng(droneData.position.value)!!,
                if (abs(currentZoom - DEFAULT_ZOOM) < ZOOM_TOLERANCE) currentZoom else DEFAULT_ZOOM
                ))
        }
    }

    override fun onResume() {
        super.onResume()

        droneData.position.observe(this, dronePositionObserver)
        droneData.homeLocation.observe(this, homePositionObserver)
        droneData.isFlying.observe(this, droneFlyingStatusObserver)
        droneData.isConnected.observe(this, droneConnectionStatusObserver)
        droneData.videoStreamUri.observe(this, videoStreamUriObserver)
    }

    override fun onPause() {
        super.onPause()

        droneData.position.removeObserver(dronePositionObserver)
        droneData.homeLocation.removeObserver(homePositionObserver)
        droneData.isFlying.removeObserver(droneFlyingStatusObserver)
        droneData.isConnected.removeObserver(droneConnectionStatusObserver)
        droneData.videoStreamUri.removeObserver(videoStreamUriObserver)
    }

    override fun onDestroy() {
        super.onDestroy()

        dronePainter.onDestroy()
        missionPainter.onDestroy()
        homePainter.onDestroy()
    }

    companion object {
        private const val DEFAULT_ZOOM: Double = 17.0
        private const val ZOOM_TOLERANCE: Double = 2.0
    }
}