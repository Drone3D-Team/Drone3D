package ch.epfl.sdp.drone3d

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.drone.DroneInstanceProvider


class DroneConnectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drone_connect)
    }

    fun connectDrone(@Suppress("UNUSED_PARAMETER") view: View) {
        val ip = findViewById<EditText>(R.id.text_IP_address).text.toString()
        val port = findViewById<EditText>(R.id.text_port).text.toString()
        DroneInstanceProvider.setSimIPAndPort(ip, port)
        val intent = Intent(this, ConnectedDroneActivity::class.java).apply{}
        startActivity(intent)
    }
}