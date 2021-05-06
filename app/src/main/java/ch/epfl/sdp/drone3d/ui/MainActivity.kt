/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.ui.auth.LoginActivity
import ch.epfl.sdp.drone3d.ui.drone.ConnectedDroneActivity
import ch.epfl.sdp.drone3d.ui.drone.DroneConnectActivity
import ch.epfl.sdp.drone3d.ui.map.offline.ManageOfflineMapActivity
import ch.epfl.sdp.drone3d.ui.mission.ItineraryCreateActivity
import ch.epfl.sdp.drone3d.ui.mission.MappingMissionSelectionActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * The activity that creates the main menu
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var authService: AuthenticationService

    @Inject
    lateinit var droneService: DroneService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
    }

    private fun refresh() {
        val loginButton: Button = findViewById(R.id.log_in_button)
        val logoutButton: Button = findViewById(R.id.log_out_button)

        val connectDroneButton: Button = findViewById(R.id.go_connect_drone_button)
        val disconnectDroneButton: Button = findViewById(R.id.go_disconnect_drone_button)

        if (authService.hasActiveSession()) {
            logoutButton.visibility = View.VISIBLE
            loginButton.visibility = View.GONE
        } else {
            logoutButton.visibility = View.GONE
            loginButton.visibility = View.VISIBLE
        }

        if (droneService.isConnected()) {
            disconnectDroneButton.visibility = View.VISIBLE
            connectDroneButton.visibility = View.GONE
        } else {
            disconnectDroneButton.visibility = View.GONE
            connectDroneButton.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        refresh()
    }

    /**
     * Go to LoginActivity when log_in_button is clicked
     */
    fun goToLogin(@Suppress("UNUSED_PARAMETER") view: View) {
        open(LoginActivity::class)
    }

    /**
     * Logs the user out and refresh the activity
     */
    fun logout(@Suppress("UNUSED_PARAMETER") view: View) {
        authService.signOut()
        refresh()
    }

    /**
     * Go to ItineraryCreateActivity when create_itinerary_button is clicked
     */
    fun goToItineraryCreate(@Suppress("UNUSED_PARAMETER") view: View) {
        open(ItineraryCreateActivity::class)
    }

    /**
     * Go to MappingMissionSelectionActivity when browse_itinerary_button is clicked
     */
    fun goToMappingMissionSelection(@Suppress("UNUSED_PARAMETER") view: View) {
        open(MappingMissionSelectionActivity::class)
    }

    /**
     * Go to DroneConnectActivity or ConnectedDroneActivity when connect_drone_button is clicked
     * depending of if a drone is connected or not
     */
    fun goToDroneConnect(@Suppress("UNUSED_PARAMETER") view: View) {
        if(droneService.isConnected()) {
            open(ConnectedDroneActivity::class)
        } else {
            open(DroneConnectActivity::class)
        }
    }

    /**
     * Go to ManageOfflineMapActivity when manage_offline_map_button is clicked
     */
    fun goToDroneManageOfflineMap(@Suppress("UNUSED_PARAMETER") view: View) {
        open(ManageOfflineMapActivity::class)
    }

    private fun <T> open(activity: KClass<T>) where T : Activity {
        val intent = Intent(this, activity.java)
        startActivity(intent)
    }
}
