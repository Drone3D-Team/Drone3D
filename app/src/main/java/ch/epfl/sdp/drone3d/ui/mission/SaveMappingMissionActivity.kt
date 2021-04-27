/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MediatorLiveData
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.model.mission.MappingMission
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.api.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.ui.MainActivity
import ch.epfl.sdp.drone3d.ui.ToastHandler
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This activity let the user name then store and/or share a mapping mission.
 */
@AndroidEntryPoint
class SaveMappingMissionActivity : AppCompatActivity() {

    @Inject
    lateinit var mappingMissionDao: MappingMissionDao

    @Inject
    lateinit var authService: AuthenticationService

    private lateinit var privateCheckBox: CheckBox
    private lateinit var sharedCheckBox: CheckBox
    private lateinit var saveButton: Button

    private lateinit var flightPath: List<LatLng>

    private lateinit var nameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = intent.extras;
        if (bundle != null) {
            flightPath = bundle.getSerializable("flightPath") as List<LatLng>
        }

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.activity_save_mapping_mission)

        nameEditText = findViewById(R.id.missionName)
        privateCheckBox = findViewById(R.id.privateCheckBox)
        sharedCheckBox = findViewById(R.id.sharedCheckBox)
        saveButton = findViewById(R.id.saveButton)
        saveButton.isEnabled = false


        privateCheckBox.setOnCheckedChangeListener { _, _ -> updateSaveButton() }
        sharedCheckBox.setOnCheckedChangeListener { _, _ -> updateSaveButton() }

    }

    private fun updateSaveButton() {
        saveButton.isEnabled = privateCheckBox.isChecked || sharedCheckBox.isChecked
    }

    /**
     * Store the mapping mission in the checked repo(s)
     */
    fun save(@Suppress("UNUSED_PARAMETER") view: View) {
        saveButton.isEnabled = false;

        val name = if (nameEditText.text.isEmpty()) "Unnamed mission" else nameEditText.text
        val newMappingMission = MappingMission(name.toString(), flightPath)

        // The user should be logged to access this page
        val ownerId = authService.getCurrentSession()!!.user.uid

        fun onComplete(isSuccess: Boolean) {
            if (isSuccess) {
                ToastHandler.showToast(this, R.string.mission_saved)
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                ToastHandler.showToast(this, R.string.error_mission_saved)
                saveButton.isEnabled = true;
            }
        }

        if (privateCheckBox.isChecked && sharedCheckBox.isChecked) {


            object : MediatorLiveData<Pair<Boolean, Boolean>>() {
                var private: Boolean? = null
                var shared: Boolean? = null

                init {
                    addSource(
                        mappingMissionDao.storeMappingMission(
                            ownerId,
                            newMappingMission
                        )
                    ) { result ->
                        this.private = result
                        private?.let { value = result to it }
                    }
                    addSource(
                        mappingMissionDao.shareMappingMission(
                            ownerId,
                            newMappingMission
                        )
                    ) { result ->
                        this.shared = result
                        shared?.let { value = it to result }

                    }
                }
            }.observe(this) { (privateResult, sharedResult) ->
                onComplete(
                    privateResult && sharedResult
                )
            }
            return
        }


        if (privateCheckBox.isChecked) {
            mappingMissionDao.storeMappingMission(ownerId, newMappingMission).observe(this) {
                onComplete(it)
            }
            return
        }

        if (sharedCheckBox.isChecked) {
            mappingMissionDao.shareMappingMission(ownerId, newMappingMission).observe(this) {
                if (it) {
                    onComplete(it)
                }
            }
            return
        }
    }

}