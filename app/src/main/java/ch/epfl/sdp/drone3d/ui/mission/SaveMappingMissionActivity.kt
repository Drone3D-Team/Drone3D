/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MediatorLiveData
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.model.mission.MappingMission
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.api.mission.MappingMissionService
import ch.epfl.sdp.drone3d.service.api.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.ui.ToastHandler
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates

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

    private var flightHeight by Delegates.notNull<Double>()
    private lateinit var strategy: MappingMissionService.Strategy
    private lateinit var area: List<LatLng>


    private lateinit var nameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = intent.extras
        if (bundle != null) {
            flightHeight = bundle.getDouble(ItineraryCreateActivity.FLIGHTHEIGHT_INTENT_PATH)
            strategy =
                (bundle.get(ItineraryCreateActivity.STRATEGY_INTENT_PATH) as MappingMissionService.Strategy)
            area = bundle.getParcelableArrayList(ItineraryCreateActivity.AREA_INTENT_PATH)!!
        }

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.activity_save_mapping_mission)

        nameEditText = findViewById(R.id.missionName)
        privateCheckBox = findViewById(R.id.privateCheckBox)
        sharedCheckBox = findViewById(R.id.sharedCheckBox)
        saveButton = findViewById(R.id.saveButton)
        saveButton.isEnabled = true
    }


    private fun onCompleteSaving(isSuccess: Boolean) {
        if (isSuccess) {
            goToItineraryShow()
            ToastHandler.showToast(this, R.string.mission_saved)
        } else {
            ToastHandler.showToast(this, R.string.error_mission_saved)
            saveButton.isEnabled = true;
        }
    }

    private fun shareAndStoreMission(ownerId: String, newMappingMission: MappingMission) {
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
                    if (shared != null) {
                        private?.let { value = result to it }

                    }
                }
                addSource(
                    mappingMissionDao.shareMappingMission(
                        ownerId,
                        newMappingMission
                    )
                ) { result ->
                    this.shared = result
                    if (private != null) {
                        shared?.let { value = it to result }
                    }
                }
            }
        }.observe(this) { (privateResult, sharedResult) ->
            onCompleteSaving(
                privateResult && sharedResult
            )
        }
    }

    private fun goToItineraryShow(){
        val intent = Intent(this, ItineraryShowActivity::class.java)
        intent.putExtra(MissionViewAdapter.FLIGHTHEIGHT_INTENT_PATH, flightHeight)
        intent.putExtra(MissionViewAdapter.AREA_INTENT_PATH, ArrayList(area))
        intent.putExtra(MissionViewAdapter.STRATEGY_INTENT_PATH, strategy)
        startActivity(intent)
        finish()
    }

    /**
     * Store the mapping mission in the checked repo(s)
     */
    fun save(@Suppress("UNUSED_PARAMETER") view: View) {

        if (!privateCheckBox.isChecked && !sharedCheckBox.isChecked) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.warning_no_box_selected))
            builder.setCancelable(true)

            builder.setPositiveButton(getString(R.string.continue_without_saving)) { dialog, _ ->
                dialog.cancel()
                goToItineraryShow()
            }

            builder.setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            builder.create()?.show()
        } else {
            saveButton.isEnabled = false;

            val name = if (nameEditText.text.isEmpty()) "Unnamed mission" else nameEditText.text
            val newMappingMission = MappingMission(name.toString(), flightHeight, strategy, area)

            // The user should be logged to access this page
            val ownerId = authService.getCurrentSession()!!.user.uid

            if (privateCheckBox.isChecked && sharedCheckBox.isChecked) {
                shareAndStoreMission(ownerId, newMappingMission)
            } else {
                if (privateCheckBox.isChecked) {
                    mappingMissionDao.storeMappingMission(ownerId, newMappingMission)
                        .observe(this) {
                            onCompleteSaving(it)
                        }
                }

                if (sharedCheckBox.isChecked) {
                    mappingMissionDao.shareMappingMission(ownerId, newMappingMission)
                        .observe(this) {
                            onCompleteSaving(it)
                        }
                }
            }
        }


    }

}