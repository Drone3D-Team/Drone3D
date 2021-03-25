package ch.epfl.sdp.drone3d

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.drone.DroneInstanceProvider

class ConnectedDroneActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (DroneInstanceProvider.isSimulation()) {
            setContentView(R.layout.activity_connected_drone_simulation)

            val textViewIp = findViewById<TextView>(R.id.simulation_ip).apply {
                text = "IP : ${DroneInstanceProvider.getIP()}"
            }
            val textViewPort = findViewById<TextView>(R.id.simulation_port).apply {
                text = "Port : ${DroneInstanceProvider.getPort()}"
            }
        }
    }

    fun disconnectDrone(@Suppress("UNUSED_PARAMETER") view: View) {
        DroneInstanceProvider.disconnect()
        val intent = Intent(this, DroneConnectActivity::class.java).apply {}
        startActivity(intent)
    }
}