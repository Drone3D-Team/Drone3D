/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */


package ch.epfl.sdp.drone3d.ui.mission

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.service.storage.data.MappingMission
import ch.epfl.sdp.drone3d.service.storage.data.State
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * The activity that allows the user to browse his mapping missions.
 */
@AndroidEntryPoint
class MappingMissionSelectionActivity : AppCompatActivity() {
    companion object {
        val MISSION_PATH = "ch.epfl.sdp.drone3d.ui.mission.MAPPING_MISSION"
    }

    @Inject
    lateinit var mappingMissionDao: MappingMissionDao

    @Inject
    lateinit var authService: AuthenticationService

    private lateinit var selectedStorageTypeToggleButton: ToggleButton
    private lateinit var searchBar: SearchView
    private lateinit var mappingMissionListView: LinearLayout
    private lateinit var createNewMappingMissionButton: Button
    private val buttonList = mutableListOf<Button>()
    private var mappingMissionPrivateList = mutableListOf<MappingMission>()
    private var mappingMissionSharedList = mutableListOf<MappingMission>()
    private var mappingMissionPrivateFilteredList = mutableListOf<MappingMission>()
    private var mappingMissionSharedFilteredList = mutableListOf<MappingMission>()
    private var filter: String? = null

    enum class StorageType(val checked: Boolean) {
        PRIVATE(true), SHARED(false)
    }

    private var currentType = StorageType.PRIVATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapping_mission_selection)


        selectedStorageTypeToggleButton = findViewById(R.id.mappingMissionToggleButton)
        searchBar = findViewById(R.id.searchView)
        mappingMissionListView = findViewById(R.id.mappingMissionList)
        createNewMappingMissionButton = findViewById(R.id.createMappingMissionButton)

        selectedStorageTypeToggleButton.isChecked = currentType.checked
        selectedStorageTypeToggleButton.setOnCheckedChangeListener { _, isChecked ->
            currentType = if (isChecked) StorageType.PRIVATE else StorageType.SHARED
            updateFilter()
            updateList()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupSearchBar()

        setListenerMappingMissions()

        createNewMappingMissionButton.setOnClickListener {
            val intent = Intent(this, ItineraryCreateActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSearchBar() {
        // Searches only when submit button is pressed
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filter = searchBar.query.toString()
                if (filter!!.replace("\\s".toRegex(), "") == "") {
                    filter = null
                }
                updateFilter()
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
            filter = null
            updateList()
        }
    }

    private fun updateFilter() {
        if (filter != null) {
            val ownerId = authService.getCurrentSession()!!.user.uid
            if (currentType == StorageType.PRIVATE) {
                mappingMissionDao.updatePrivateFilteredMappingMissions(ownerId, filter!!)
            } else if (currentType == StorageType.SHARED) {
                mappingMissionDao.updateSharedFilteredMappingMissions((filter!!))
            }
        }
    }

    private fun setListenerMappingMissions() {
        val ownerId = authService.getCurrentSession()!!.user.uid

        val liveSharedMappingMissions = mappingMissionDao.getSharedMappingMissions()
        liveSharedMappingMissions.observe(this, {
            mappingMissionSharedList = it.toMutableList()
            if (currentType == StorageType.SHARED) {
                updateList()
            }
        })

        val livePrivateMappingMissions = mappingMissionDao.getPrivateMappingMissions(ownerId)
        livePrivateMappingMissions.observe(this, {
            mappingMissionPrivateList = it.toMutableList()
            if (currentType == StorageType.PRIVATE) {
                updateList()
            }
        })

        val livePrivateFilteredMappingMissions =
            mappingMissionDao.getPrivateFilteredMappingMissions()
        livePrivateFilteredMappingMissions.observe(this, {
            mappingMissionPrivateFilteredList = it.toMutableList()
            if (currentType == StorageType.PRIVATE) {
                updateList()
            }
        })

        val liveSharedFilteredMappingMissions = mappingMissionDao.getSharedFilteredMappingMissions()
        liveSharedFilteredMappingMissions.observe(this, {
            mappingMissionSharedFilteredList = it.toMutableList()
            if (currentType == StorageType.SHARED) {
                updateList()
            }
        })
    }

    /*private fun populateMappingMissionsList() {

        for (i in 0..10) {
            val mm = MappingMission("Mission $i", emptyList())
            mappingMissionDao.storeMappingMission(authService.getCurrentSession()!!.user.uid, mm)
            if (i > 4) {
                mappingMissionDao.shareMappingMission(
                    authService.getCurrentSession()!!.user.uid,
                    mm
                )
            }

        }
        for (i in 10..20) {
            mappingMissionDao.shareMappingMission(
                authService.getCurrentSession()!!.user.uid,
                MappingMission("Mission $i", emptyList())
            )

        }
    }*/

    @SuppressLint("SetTextI18n")
    private fun updateList() {
        mappingMissionListView.removeAllViews()
        buttonList.clear()

        val list: MutableList<MappingMission> = when (currentType) {
            StorageType.PRIVATE -> if (filter == null) mappingMissionPrivateList else mappingMissionPrivateFilteredList
            StorageType.SHARED -> if (filter == null) mappingMissionSharedList else mappingMissionSharedFilteredList
        }

        for (mission in list) {
            val button = Button(this)
            button.height = 50
            button.width = 100
            button.text = mission.name
            button.tag = "button " + mission.name //used for testing
            if (currentType == StorageType.PRIVATE && mission.state == State.PRIVATE_AND_SHARED)
                button.text = mission.name + "- S"


            button.setOnClickListener {

                val intent = Intent(this, ItineraryShowActivity::class.java)
                val flightPathArrayList: ArrayList<LatLng> =
                    ArrayList(mission.flightPath) //ArrayList implements Serializable, not List

                intent.putExtra(MISSION_PATH, flightPathArrayList)
                startActivity(intent)

            }

            buttonList.add(button)
            mappingMissionListView.addView(button)
        }
    }
}