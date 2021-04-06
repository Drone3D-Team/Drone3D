/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.drone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.drone.DroneProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConnectedDroneActivity : AppCompatActivity() {

    @Inject
    lateinit var droneProvider: DroneProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (droneProvider.isSimulation()) {
            setContentView(R.layout.activity_connected_simulation)

            findViewById<TextView>(R.id.simulation_ip).apply {
                text = getString(R.string.drone_simulated_ip, droneProvider.getIP())
            }
            findViewById<TextView>(R.id.simulation_port).apply {
                text = getString(R.string.drone_simulated_port, droneProvider.getPort())
            }
        } else {
            setContentView(R.layout.activity_connected_drone)
        }
    }

    /**
     * Disconnect a connected drone or simulation from the app
     */
    fun disconnectDrone(@Suppress("UNUSED_PARAMETER") view: View) {
        droneProvider.disconnect()
        val intent = Intent(this, DroneConnectActivity::class.java)
        startActivity(intent)
    }
}