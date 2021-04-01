/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.drone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.drone.DroneProviderImpl


class DroneConnectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drone_connect)
    }

    /**
     * Connect a simulation to the application using an ip address and a port
     */
    fun connectSimulatedDrone(@Suppress("UNUSED_PARAMETER") view: View) {
        val ip = findViewById<EditText>(R.id.text_IP_address).text.toString()
        val port = findViewById<EditText>(R.id.text_port).text.toString()
        DroneProviderImpl.setSimulation(ip, port)
        val intent = Intent(this, ConnectedDroneActivity::class.java)
        startActivity(intent)
    }

    /**
     * Connect a drone to the application
     */
    fun connectDrone(@Suppress("UNUSED_PARAMETER") view: View) {
        DroneInstanceProvider.setDrone()
        val intent = Intent(this, ConnectedDroneActivity::class.java).apply{}
        startActivity(intent)
    }
}