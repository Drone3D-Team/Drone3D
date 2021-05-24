/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.api.drone.DroneData.DroneStatus
import ch.epfl.sdp.drone3d.service.api.drone.DroneData.DroneStatus.*
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.impl.drone.DroneUtils
import ch.epfl.sdp.drone3d.ui.ToastHandler
import ch.epfl.sdp.drone3d.ui.map.MissionInProgressActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import timber.log.Timber
import javax.inject.Inject

/**
 * Activity showing a loading screen when setting up a drone for a mission
 */
@AndroidEntryPoint
class MissionStartActivity : AppCompatActivity() {


    companion object {
        const val MISSION_ALTITUDE: Float = 20f

        val MISSION_START_STATUS: List<DroneStatus> = listOf(
            IDLE,
            SENDING_ORDER,
            ARMING,
            STARTING_MISSION
        )
    }

    @Inject lateinit var droneService : DroneService

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission_start)

        val progressBar = findViewById<ProgressBar>(R.id.mission_start_progress).apply {
            max = totalProgress()
        }

        val progressText = findViewById<TextView>(R.id.mission_start_text)

        droneService.getData().getDroneStatus().observe(this) { status ->
            getProgress(status)?.let {
                progressBar.progress = it
            }

            progressText.text = getText(status)
        }

        val missionPath = intent.getParcelableArrayListExtra<LatLng>(ItineraryShowActivity.FLIGHTPATH_INTENT_PATH)
        setupMission(missionPath)
    }

    override fun onDestroy() {
        super.onDestroy()

        disposable?.dispose()
    }

    private fun getProgress(status: DroneStatus?) : Int? =
        MISSION_START_STATUS.indexOf(status).takeIf { it != -1 }

    private fun totalProgress(): Int = MISSION_START_STATUS.size

    private fun getText(status: DroneStatus?): CharSequence =
        getText(
            when(status) {
                IDLE -> R.string.mission_state_idle
                ARMING -> R.string.mission_state_arming
                SENDING_ORDER -> R.string.mission_state_sending
                STARTING_MISSION -> R.string.mission_state_starting
                else -> R.string.mission_state_unknown
            }
        )

    private fun setupMission(missionPath: List<LatLng>?) {
        if(missionPath == null) {
            ToastHandler.showToastAsync(this, R.string.mission_null)
            finish()
        } else {
            val droneMission = DroneUtils.makeDroneMission(missionPath, MISSION_ALTITUDE)
            try {
                disposable = droneService.getExecutor().setupMission(this, droneMission)
                    .subscribe(
                        {
                            val intent = Intent(this, MissionInProgressActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        {
                            showError(it)
                            finish()
                        })
            } catch (e: Exception) {
                showError(e)
                finish()
            }
        }
    }

    private fun showError(ex: Throwable) {
        ToastHandler.showToastAsync(
            this,
            R.string.drone_setup_mission_error,
            Toast.LENGTH_LONG,
            ex.message)
        Timber.e(ex)
    }
}
