/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.storage.dao.MappingMissionDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * The activity that allows the user to browse his mapping missions.
 */
@AndroidEntryPoint
class MappingMissionSelectionActivity : AppCompatActivity() {

    @Inject
    lateinit var mappingMissionDao: MappingMissionDao

    @Inject
    lateinit var authService: AuthenticationService

    enum class StorageType(val checked: Boolean, val sharedVisible: Boolean, val privateVisible: Boolean) {
        PRIVATE(true, false, true),
        SHARED(false, true, false)
    }

    private val currentType = MutableLiveData(StorageType.PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapping_mission_selection)

        val ownerId = authService.getCurrentSession()!!.user.uid

        val sharedAdapter = MissionViewAdapter( false)
        val privateAdapter = MissionViewAdapter(true)

        val sharedList = findViewById<RecyclerView>(R.id.shared_mission_list_view)
        val privateList = findViewById<RecyclerView>(R.id.private_mission_list_view)

        // Setup lists
        sharedList.adapter = sharedAdapter
        privateList.adapter = privateAdapter

        // Setup adapters
        mappingMissionDao.getSharedMappingMissions().observe(this) {
            it?.let { sharedAdapter.submitList(it) }
        }

        mappingMissionDao.getPrivateMappingMissions(ownerId).observe(this) {
            it?.let { privateAdapter.submitList(it) }
        }

        // Link state with view visibility
        currentType.observe(this) {
            it?.let {
                sharedList.visibility = if (it.sharedVisible) VISIBLE else GONE
                privateList.visibility = if (it.privateVisible) VISIBLE else GONE
            }
        }

        // Setup toggle button
        val selectedStorageTypeToggleButton = findViewById<ToggleButton>(R.id.mapping_mission_state_toggle)
        selectedStorageTypeToggleButton.isChecked = currentType.value!!.checked
        selectedStorageTypeToggleButton.setOnCheckedChangeListener { _, isChecked ->
            currentType.value = if (isChecked) StorageType.PRIVATE else StorageType.SHARED
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun createNewMission(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(this, ItineraryCreateActivity::class.java)
        startActivity(intent)
    }
}