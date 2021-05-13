/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.map.MapboxDroneDrawer
import ch.epfl.sdp.drone3d.map.MapboxHomeDrawer
import ch.epfl.sdp.drone3d.map.MapboxMissionDrawer
import ch.epfl.sdp.drone3d.model.weather.WeatherReport
import ch.epfl.sdp.drone3d.service.api.drone.DroneData
import ch.epfl.sdp.drone3d.service.api.drone.DroneData.DroneStatus
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.api.weather.WeatherService
import ch.epfl.sdp.drone3d.service.impl.drone.DroneUtils
import ch.epfl.sdp.drone3d.service.impl.weather.WeatherUtils
import ch.epfl.sdp.drone3d.ui.ToastHandler
import ch.epfl.sdp.drone3d.ui.mission.ItineraryShowActivity
import ch.epfl.sdp.drone3d.ui.mission.MissionViewAdapter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.rtsp.RtspDefaultClient
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.source.rtsp.core.Client
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.button.MaterialButton
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import dagger.hilt.android.AndroidEntryPoint
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

    companion object {
        private const val DEFAULT_ZOOM: Double = 17.0
        private const val ZOOM_TOLERANCE: Double = 2.0
    }

    @Inject lateinit var droneService: DroneService
    @Inject lateinit var locationService: LocationService
    @Inject lateinit var weatherService: WeatherService

    private val disposables = CompositeDisposable()
    private lateinit var mapboxMap: MapboxMap

    private lateinit var missionDrawer: MapboxMissionDrawer
    private lateinit var droneDrawer: MapboxDroneDrawer
    private lateinit var homeDrawer: MapboxHomeDrawer

    private lateinit var player: ExoPlayer
    private lateinit var rtspFactory: Client.Factory<*>

    private val observedData: MutableSet<LiveData<*>> = mutableSetOf()
    private var missionPath: ArrayList<LatLng>? = null

    private lateinit var backToHomeButton: MaterialButton
    private lateinit var backToUserButton: MaterialButton

    private lateinit var warningBadWeather: TextView
    private lateinit var weatherReport: LiveData<WeatherReport>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        missionPath = intent.getParcelableArrayListExtra(MissionViewAdapter.MISSION_PATH)

        initMapView(savedInstanceState,
            R.layout.activity_mission_in_progress,
            R.id.map_in_mission_view)

        mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                setupMapboxMap(mapView, style)
            }
        }

        backToHomeButton = findViewById(R.id.backToHomeButton)
        backToUserButton = findViewById(R.id.backToUserButton)

        warningBadWeather = findViewById(R.id.warningBadWeather)
        if (missionPath == null) {
            warningBadWeather.visibility = View.GONE
            weatherReport = MutableLiveData()
        } else {
            weatherReport =
                weatherService.getWeatherReport(droneService.getData().getPosition().value!!)
        }

        player = SimpleExoPlayer.Builder(this).build()
        rtspFactory = RtspDefaultClient.factory(player)
            .setFlags(Client.FLAG_ENABLE_RTCP_SUPPORT)
            .setNatMethod(Client.RTSP_NAT_DUMMY)
        findViewById<PlayerView>(R.id.camera_mission_view).player = player

        setupObservers()

        startMission()
    }

    private fun setupMapboxMap(mapView: MapView, style: Style) {
        homeDrawer = MapboxHomeDrawer(mapView, mapboxMap, style)
        droneDrawer = MapboxDroneDrawer(mapView, mapboxMap, style)
        missionDrawer = MapboxMissionDrawer(mapView, mapboxMap, style)

        centerCameraOnDrone()

        Transformations.map(droneService.getData().getMission()) { mission ->
            mission?.let {
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
                    completable.subscribe(
                            { openItineraryShow() },
                            {
                                showError(it)
                                openItineraryShow()
                            })
                )
            } catch (e: Exception) {
                showError(e)
                openItineraryShow()
            }
        }
    }

    private fun openItineraryShow() {
        val intent = Intent(this, ItineraryShowActivity::class.java)
        intent.putExtra(MissionViewAdapter.MISSION_PATH, missionPath)
        startActivity(intent)
        finish()
    }

    private fun setupObservers() {
        val droneData = droneService.getData()

        createObserver(droneData.getPosition()) {
            it?.let { newLatLng -> if (::droneDrawer.isInitialized) droneDrawer.showDrone(newLatLng) }
        }

        createObserver(droneData.getHomeLocation()) {
            it?.let { home -> if (::homeDrawer.isInitialized) homeDrawer.showHome(LatLng(home.latitudeDeg, home.longitudeDeg)) }
        }

        createObserver(weatherReport) { report ->
                warningBadWeather.visibility =
                    if (report == null || WeatherUtils.isWeatherGoodEnough(report)) View.GONE else View.VISIBLE
        }

        createDroneStatusObserver(droneData)
        createTextObserver(droneData.getSpeed(), R.id.speedLive, R.string.live_speed) { it }
        createTextObserver(droneData.getRelativeAltitude(), R.id.altitudeLive, R.string.live_altitude) { it }
        createTextObserver(droneData.getBatteryLevel(), R.id.batteryLive, R.string.live_battery) { it*100 }
        createPositionObserver(droneData)
        createConnectionObserver(droneData)
        createObserver(droneData.getVideoStreamUri()) {
            it?.let { streamUri ->
                val mediaSource = RtspMediaSource.Factory(rtspFactory)
                    .createMediaSource(Uri.parse(streamUri.replace("rtspt://", "rtsp://")))

                player.setMediaSource(mediaSource)
                player.prepare()
                player.play()
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
            }, {
                showError(it)
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
            }, {
                showError(it)
            })
        )
    }

    private fun showError(ex: Throwable) {
        ToastHandler.showToastAsync(
                this,
                R.string.drone_mission_error,
                Toast.LENGTH_LONG,
                ex.message)
        Timber.e(ex)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this::droneDrawer.isInitialized) droneDrawer.onDestroy()
        if (this::missionDrawer.isInitialized) missionDrawer.onDestroy()
        if (this::homeDrawer.isInitialized) homeDrawer.onDestroy()

        observedData.forEach { data -> data.removeObservers(this) }
        observedData.clear()

        disposables.dispose()

        player.release()
    }

    private fun createPositionObserver(droneData: DroneData) {
        createObserver(droneData.getPosition()) {
            it?.let {
                findViewById<TextView>(R.id.distanceUserLive).apply {
                    text = if (locationService.isLocationEnabled())
                        if (locationService.getCurrentLocation() == null)
                            getString(R.string.user_location_null)
                        else {
                            val distanceUser: Double = it.distanceTo(locationService.getCurrentLocation()!!)
                            getString(R.string.live_distance_user, distanceUser)
                        }
                    else
                        getString(R.string.user_location_deactivated)
                }
            }
        }
    }

    private fun createDroneStatusObserver(droneData: DroneData) {
        createObserver(droneData.getDroneStatus()) { status ->
            val visibility = if (status == DroneStatus.EXECUTING_MISSION) View.VISIBLE else View.GONE

            backToHomeButton.visibility = visibility
            backToUserButton.visibility = visibility

            findViewById<TextView>(R.id.statusLive).apply {
                text = getString(R.string.live_status, status.name)
            }
        }
    }

    private fun createConnectionObserver(droneData: DroneData) {
        createObserver(droneData.isConnected()) {
            if (it == null || !it) {
                ToastHandler.showToastAsync(this, R.string.lost_connection_message, Toast.LENGTH_SHORT)
                openItineraryShow()
            }
        }
    }

    private fun <T> createTextObserver(data: LiveData<T>, viewId: Int, txtId: Int, arg: (T) -> Any) {
        createObserver(data) {
            it?.let { findViewById<TextView>(viewId).text = getString(txtId, arg(it)) }
        }
    }

    private fun <T> createObserver(data: LiveData<T>, observer: Observer<T>) {
        data.observe(this, observer)
        observedData.add(data)
    }
}