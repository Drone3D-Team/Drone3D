package ch.epfl.sdp.drone3d.ui.mission

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.drone3d.R

class MissionPicturesActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission_pictures)


        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.mission_pictures_view)
        val adapter = PictureViewAdapter()
        recyclerView.adapter = adapter
    }
}