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
import ch.epfl.sdp.drone3d.drone.DroneService
import ch.epfl.sdp.drone3d.ui.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class DroneConnectActivity : AppCompatActivity() {

    /**
     * Values used to check if an ip has a valid format or not
     */
    private val regex = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    private val pattern: Pattern = Pattern.compile(regex)

    @Inject
    lateinit var droneService: DroneService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drone_connect)
    }

    /**
     * Connect a simulation to the application using an ip address and a port
     */
    fun connectSimulatedDrone(@Suppress("UNUSED_PARAMETER") view: View) {
        val ip = findViewById<EditText>(R.id.text_IP_address).text.toString()
        if (verifyIp(ip)) {
            val port = findViewById<EditText>(R.id.text_port).text.toString()
            droneService.setSimulation(ip, port)
            val intent = Intent(this, ConnectedDroneActivity::class.java)
            startActivity(intent)
        } else {
            ToastHandler.showToastAsync(this, R.string.ip_format_invalid)
        }
    }

    /**
     * Connect a drone to the application
     */
    fun connectDrone(@Suppress("UNUSED_PARAMETER") view: View) {
        droneService.setDrone()
        val intent = Intent(this, ConnectedDroneActivity::class.java)
        startActivity(intent)
    }

    /**
     * Checks if an ip has a valid format or not
     */
    private fun verifyIp(ip: String): Boolean {
        val matcher: Matcher = pattern.matcher(ip)
        return matcher.matches()
    }
}