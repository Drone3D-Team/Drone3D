package ch.epfl.sdp.drone3d

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.drone.DroneInstanceProvider

class ConnectedDroneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (DroneInstanceProvider.isSimulation()) {
            setContentView(R.layout.activity_connected_drone_simulation)

            findViewById<TextView>(R.id.simulation_ip).apply {
                text = getString(R.string.drone_simulated_ip, DroneInstanceProvider.getIP())
            }
            findViewById<TextView>(R.id.simulation_port).apply {
                text = getString(R.string.drone_simulated_port, DroneInstanceProvider.getPort())
            }
        }
    }

    /**
     * Disconnect a connected drone or simulation from the app
     */
    fun disconnectDrone(@Suppress("UNUSED_PARAMETER") view: View) {
        DroneInstanceProvider.disconnect()
        val intent = Intent(this, DroneConnectActivity::class.java).apply {}
        startActivity(intent)
    }
}