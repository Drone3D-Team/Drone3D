/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.drone

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.drone.DroneService
import ch.epfl.sdp.drone3d.ui.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    private lateinit var droneConnectButton: Button
    private lateinit var simulationConnectButton: Button
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var ipText: EditText
    private lateinit var portText: EditText
    private lateinit var waitingText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drone_connect)

        droneConnectButton = findViewById(R.id.connect_drone_button)
        droneConnectButton.visibility = View.VISIBLE
        simulationConnectButton = findViewById(R.id.connect_simulation_button)
        simulationConnectButton.visibility = View.VISIBLE
        ipText = findViewById(R.id.text_IP_address)
        ipText.visibility = View.VISIBLE
        portText = findViewById(R.id.text_port)
        portText.visibility = View.VISIBLE

        loadingProgressBar = findViewById(R.id.progress_bar_drone)
        loadingProgressBar.visibility = View.GONE
        waitingText = findViewById(R.id.waiting_connection)
        waitingText.visibility = View.GONE
    }

    /**
     * Connect a simulation to the application using an ip address and a port
     */
    fun connectSimulatedDrone(@Suppress("UNUSED_PARAMETER") view: View) {
        val ip = findViewById<EditText>(R.id.text_IP_address).text.toString()
        if (verifyIp(ip)) {
            val port = findViewById<EditText>(R.id.text_port).text.toString()
            showWaiting()
            droneService.setSimulation(ip, port)

            GlobalScope.launch {
                async {
                    checkIfDroneConnected(30)

                    val mainHandler = Handler(applicationContext.mainLooper)

                    val myRunnable = Runnable {
                        if (droneService.isConnected()) {
                            val intent = Intent(applicationContext, ConnectedDroneActivity::class.java)
                            startActivity(intent)
                        } else {
                            droneService.disconnect()
                            showConnectionOptions()
                            ToastHandler.showToastAsync(applicationContext, R.string.ip_connection_timeout)
                        }
                    }

                    mainHandler.post(myRunnable)
                }
            }
        } else {
            ToastHandler.showToastAsync(this, R.string.ip_format_invalid)
        }
    }

    /**
     * Connect a drone to the application
     */
    fun connectDrone(@Suppress("UNUSED_PARAMETER") view: View) {
        showWaiting()
        droneService.setDrone()

        GlobalScope.launch {
            async {
                checkIfDroneConnected(50)

                val mainHandler = Handler(applicationContext.mainLooper)

                val myRunnable = Runnable {
                    if (droneService.isConnected()) {
                        val intent = Intent(applicationContext, ConnectedDroneActivity::class.java)
                        startActivity(intent)
                    } else {
                        droneService.disconnect()
                        showConnectionOptions()
                        ToastHandler.showToastAsync(applicationContext, R.string.no_drone_detected)
                    }
                }

                mainHandler.post(myRunnable)
            }
        }
    }

    /**
     * Change the view to hide connection options and show waiting connection interface
     */
    private fun showWaiting() {
        droneConnectButton.visibility = View.GONE
        simulationConnectButton.visibility = View.GONE
        ipText.visibility = View.GONE
        portText.visibility = View.GONE

        loadingProgressBar.visibility = View.VISIBLE
        waitingText.visibility = View.VISIBLE
    }

    /**
     * Change the view to hide waiting connection interface and show connection options
     */
    private fun showConnectionOptions() {
        droneConnectButton.visibility = View.VISIBLE
        simulationConnectButton.visibility = View.VISIBLE
        ipText.visibility = View.VISIBLE
        portText.visibility = View.VISIBLE

        loadingProgressBar.visibility = View.GONE
        waitingText.visibility = View.GONE
    }

    /**
     * Check if a drone was connected on the application after [counterMax]/10 seconds
     */
    private suspend fun checkIfDroneConnected(counterMax: Int) {
        var counter = 0
        while (!droneService.isConnected() && counter < counterMax) {
            delay(100L)
            counter++
        }
    }

    /**
     * Checks if an ip has a valid format or not
     */
    private fun verifyIp(ip: String): Boolean {
        val matcher: Matcher = pattern.matcher(ip)
        return matcher.matches()
    }
}