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
import android.widget.ImageView
import android.widget.SearchView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.service.storage.data.MappingMission
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

    enum class StorageType(
        val checked: Boolean,
        val sharedVisible: Boolean,
        val privateVisible: Boolean
    ) {
        PRIVATE(true, false, true),
        SHARED(false, true, false)
    }

    private val currentType = MutableLiveData(Pair<StorageType, String?>(StorageType.PRIVATE, null))

    private fun setupAdapter(
        data: LiveData<List<MappingMission>>,
        adapter: ListAdapter<MappingMission, out RecyclerView.ViewHolder>
    ) =
        data.observe(this) {
            it.let { adapter.submitList(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapping_mission_selection)

        setupListViews()

        // Setup toggle button
        val selectedStorageTypeToggleButton =
            findViewById<ToggleButton>(R.id.mapping_mission_state_toggle)
        selectedStorageTypeToggleButton.isChecked = currentType.value!!.first.checked
        selectedStorageTypeToggleButton.setOnCheckedChangeListener { _, isChecked ->
            currentType.value =
                Pair(
                    if (isChecked) StorageType.PRIVATE else StorageType.SHARED,
                    currentType.value!!.second
                )
        }

        val searchBar = findViewById<SearchView>(R.id.searchView)
        setupSearchBar(searchBar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupListViews() {
        val ownerId = authService.getCurrentSession()!!.user.uid

        val sharedList = findViewById<RecyclerView>(R.id.shared_mission_list_view)
        val privateList = findViewById<RecyclerView>(R.id.private_mission_list_view)
        val sharedFilteredList = findViewById<RecyclerView>(R.id.shared_filtered_mission_list_view)
        val privateFilteredList =
            findViewById<RecyclerView>(R.id.private_filtered_mission_list_view)

        setupListAdapter(sharedList, false, false)
        setupListAdapter(privateList, true, false)
        setupListAdapter(sharedFilteredList, false, true)
        setupListAdapter(privateFilteredList, true, true)

        // Link state with view visibility
        currentType.observe(this) {
            it.let {
                sharedList.visibility =
                    if (it.first.sharedVisible && it.second == null) VISIBLE else GONE
                privateList.visibility =
                    if (it.first.privateVisible && it.second == null) VISIBLE else GONE
                sharedFilteredList.visibility =
                    if (it.first.sharedVisible && it.second != null) VISIBLE else GONE
                privateFilteredList.visibility =
                    if (it.first.privateVisible && it.second != null) VISIBLE else GONE

                mappingMissionDao.updateSharedFilteredMappingMissions(it.second)
                mappingMissionDao.updatePrivateFilteredMappingMissions(ownerId, it.second)
            }
        }
    }

    private fun setupListAdapter(list: RecyclerView, private: Boolean, filter: Boolean) {
        val adapter = MissionViewAdapter(private)
        list.adapter = adapter
        setupAdapter(getMappingMissionsLiveData(private, filter), adapter)
    }

    private fun getMappingMissionsLiveData(
        private: Boolean,
        filter: Boolean
    ): LiveData<List<MappingMission>> {
        val ownerId = authService.getCurrentSession()!!.user.uid
        return when {
            private and !filter -> mappingMissionDao.getPrivateMappingMissions(ownerId)
            !private and !filter -> mappingMissionDao.getSharedMappingMissions()
            private and filter -> mappingMissionDao.getPrivateFilteredMappingMissions()
            else -> mappingMissionDao.getSharedFilteredMappingMissions()
        }
    }

    private fun setupSearchBar(searchBar: SearchView) {
        // Searches only when submit button is pressed
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                currentType.value = Pair(currentType.value!!.first, query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        // When the clear text button is pressed, show all private or shared mapping missions
        val searchCloseButtonId =
            searchBar.context.resources.getIdentifier("android:id/search_close_btn", null, null)
        val closeButton: ImageView = searchBar.findViewById(searchCloseButtonId)
        closeButton.setOnClickListener {
            searchBar.setQuery("", false)
            currentType.value = Pair(currentType.value!!.first, null)
        }
    }

    fun createNewMission(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(this, ItineraryCreateActivity::class.java)
        startActivity(intent)
    }
}