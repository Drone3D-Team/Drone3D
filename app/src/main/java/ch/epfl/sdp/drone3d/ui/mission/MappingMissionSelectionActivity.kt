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
import ch.epfl.sdp.drone3d.model.mission.MappingMission
import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.api.storage.dao.MappingMissionDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.min


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

    private val currentListState =
        MutableLiveData(Pair<StorageType, String?>(StorageType.PRIVATE, null))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapping_mission_selection)


        val selectedStorageTypeToggleButton =
                findViewById<ToggleButton>(R.id.mapping_mission_state_toggle)


        setupListViews()

        if(!authService.hasActiveSession()){

            currentListState.value =
                    Pair(
                            StorageType.SHARED,
                            currentListState.value!!.second
                    )

            selectedStorageTypeToggleButton.isEnabled = false

        } else{

            selectedStorageTypeToggleButton.isChecked = currentListState.value!!.first.checked
            selectedStorageTypeToggleButton.setOnCheckedChangeListener { _, isChecked ->
                currentListState.value =
                        Pair(
                                if (isChecked) StorageType.PRIVATE else StorageType.SHARED,
                                currentListState.value!!.second
                        )
            }
        }

        val searchBar = findViewById<SearchView>(R.id.searchView)
        setupSearchBar(searchBar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupAdapter(
        data: LiveData<List<MappingMission>>,
        adapter: ListAdapter<MappingMission, out RecyclerView.ViewHolder>
    ) = data.observe(this, {
        it.let { adapter.submitList(sortedList(it)) }
    })


    private fun sortedList(mappingMissions: List<MappingMission>?): List<MappingMission> {
        return if (mappingMissions.isNullOrEmpty()) {
            emptyList()
        } else {
            mappingMissions.sortedWith { m1: MappingMission, m2: MappingMission ->
                compare(m1, m2)
            }
        }
    }

    private fun compare(m1: MappingMission, m2: MappingMission): Int {
        val name1 = m1.nameUpperCase
        val name2 = m2.nameUpperCase
        val smallerStringSize = min(name1.length, name2.length)
        val diffIndex = indexOfDifference(name1, name2)

        if (diffIndex < smallerStringSize) {
            return if (name1[diffIndex].isDigit() && name2[diffIndex].isDigit()) {
                extractInt(name1.substring(diffIndex)) - extractInt(name2.substring(diffIndex))
            } else if (name1[diffIndex].isDigit()) {
                1
            } else if (name2[diffIndex].isDigit()) {
                -1
            } else {
                name1.compareTo(name2)
            }
        }

        return name1.compareTo(name2)
    }

    private fun extractInt(s: String): Int {
        val num = s.replace("\\D".toRegex(), "")
        return if (num.isEmpty()) 0 else num.toInt()
    }

    private fun indexOfDifference(s1: String, s2: String): Int {
        if (s1.isNotEmpty() && s2.isNotEmpty()) {
            val smaller = min(s1.length,s2.length)
            for (i in 0 until smaller) {
                if (s1[i] != s2[i]) return i
            }
            return smaller
        }
        return 0
    }

    /**
     * Get the missions to show (shared if not connected, shared + private if user is connected)
     * and make the list observe the currentListState to have the right visibility when we click
     * on the toggle button
     */
    private fun setupListViews() {

        val sharedList = findViewById<RecyclerView>(R.id.shared_mission_list_view)
        val privateList = findViewById<RecyclerView>(R.id.private_mission_list_view)
        val sharedFilteredList = findViewById<RecyclerView>(R.id.shared_filtered_mission_list_view)
        val privateFilteredList =
                findViewById<RecyclerView>(R.id.private_filtered_mission_list_view)

        setupListAdapter(sharedList, private = false, filter = false)
        setupListAdapter(sharedFilteredList, private = false, filter = true)

        // Link state with view visibility
        currentListState.observe(this) {
            it.let {
                sharedList.visibility = getVisibility(private = false, filter = false, current = it)
                privateList.visibility = getVisibility(private = true, filter = false, current = it)
                sharedFilteredList.visibility = getVisibility(private = false, filter = true, current = it)
                privateFilteredList.visibility = getVisibility(private = true, filter = true, current = it)

                mappingMissionDao.updateSharedFilteredMappingMissions(it.second)
            }
        }


        if(authService.getCurrentSession() != null) {
            val ownerId = authService.getCurrentSession()!!.user.uid

            setupListAdapter(privateList, private = true, filter = false)
            setupListAdapter(privateFilteredList, private = true, filter = true)

            currentListState.observe(this, {
                it.let {
                    mappingMissionDao.updatePrivateFilteredMappingMissions(ownerId, it.second)
                }
            })
        }
    }

    private fun getVisibility(
        private: Boolean,
        filter: Boolean,
        current: Pair<StorageType, String?>
    ): Int {
        return when {
            private && !current.first.privateVisible -> GONE
            !private && !current.first.sharedVisible -> GONE
            filter && current.second == null -> GONE
            !filter && current.second != null -> GONE
            else -> VISIBLE
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
        return when {
            private and !filter -> mappingMissionDao.getPrivateMappingMissions(authService.getCurrentSession()!!.user.uid)
            !private and !filter -> mappingMissionDao.getSharedMappingMissions()
            private and filter -> mappingMissionDao.getPrivateFilteredMappingMissions()
            else -> mappingMissionDao.getSharedFilteredMappingMissions()
        }
    }

    private fun setupSearchBar(searchBar: SearchView) {
        // Searches only when submit button is pressed
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                currentListState.value =
                    if (query.isEmpty()) Pair(currentListState.value!!.first, null)
                    else Pair(currentListState.value!!.first, query)

                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                currentListState.value =
                    if (query.isEmpty()) Pair(currentListState.value!!.first, null)
                    else Pair(currentListState.value!!.first, query)
                return false
            }
        })

        // When the clear text button is pressed, show all private or shared mapping missions
        val searchCloseButtonId =
            searchBar.context.resources.getIdentifier("android:id/search_close_btn", null, null)
        val closeButton: ImageView = searchBar.findViewById(searchCloseButtonId)
        closeButton.setOnClickListener {
            searchBar.setQuery("", false)
            currentListState.value = Pair(currentListState.value!!.first, null)
        }
    }

    fun createNewMission(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(this, ItineraryCreateActivity::class.java)
        startActivity(intent)
    }
}