/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.auth.AuthenticationService
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.storage.data.LatLong
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SaveMappingMissionActivity : AppCompatActivity()  {

    @Inject
    lateinit var mappingMissionDao: MappingMissionDao
    @Inject
    lateinit var authService: AuthenticationService

    // TODO(Replace by a recieved list created by ItineraryCreateActivity)
    private val flightPath = listOf<LatLong>(LatLong(10.0, 12.0), LatLong(1.1,2.2))

    private lateinit var nameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.activity_save_mapping_mission)

        nameEditText = findViewById(R.id.missionName)
    }

    fun save(@Suppress("UNUSED_PARAMETER") view: View) {
        val privateCheckBox: CheckBox = findViewById(R.id.privateCheckBox)
        val sharedCheckBox: CheckBox = findViewById(R.id.sharedCheckBox)
        if(!privateCheckBox.isChecked and !sharedCheckBox.isChecked){
            // TODO(DIsplay: "You should pick at least one location to save your mission")
            return
        }
        val name = if (nameEditText.text.equals("")) "Unnamed mission" else nameEditText.text
        val newMappingMission = MappingMission(name.toString(), flightPath)

        // The user should be logged to access this page
        val ownerId = authService.getCurrentSession()!!.user.uid

        if(privateCheckBox.isChecked){
            mappingMissionDao.storeMappingMission(ownerId, newMappingMission)
        }

        if(sharedCheckBox.isChecked){
            mappingMissionDao.shareMappingMission(ownerId, newMappingMission)
        }
    }

}