package ch.epfl.sdp.drone3d.ui.map

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ch.epfl.sdp.drone3d.R

class MissionInProgressActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission_in_progress)
    }
}