package ch.epfl.sdp.drone3d.ui.mission

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.auth.AuthenticationService
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import ch.epfl.sdp.drone3d.storage.data.State
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

    private lateinit var selectedStorageTypeToggleButton: ToggleButton
    private lateinit var mappingMissionListView: LinearLayout
    private lateinit var createNewMappingMissionButton: Button
    private val buttonList = mutableListOf<Button>()
    private var mappingMissionPrivateList = mutableListOf<MappingMission>()
    private var mappingMissionSharedList = mutableListOf<MappingMission>()

    enum class StorageType(val checked: Boolean) {
        PRIVATE(true), SHARED(false)
    }

    private var currentType = StorageType.PRIVATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapping_mission_selection)


        selectedStorageTypeToggleButton = findViewById(R.id.mappingMissionToggleButton)
        mappingMissionListView = findViewById(R.id.mappingMissionList)
        createNewMappingMissionButton = findViewById(R.id.createMappingMissionButton)

        selectedStorageTypeToggleButton.isChecked = currentType.checked
        selectedStorageTypeToggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
            currentType = if (isChecked) StorageType.PRIVATE else StorageType.SHARED
            updateList()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setListenerMappingMissions()

        createNewMappingMissionButton.setOnClickListener {
            val intent = Intent(this, ItineraryCreateActivity::class.java).apply {}
            startActivity(intent)
        }

    }


    private fun setListenerMappingMissions() {
        val ownerId = authService.getCurrentSession()!!.user.uid

        val liveSharedMappingMissions = mappingMissionDao.getSharedMappingMissions()
        liveSharedMappingMissions.observe(this, Observer {
            mappingMissionSharedList = it.toMutableList()
            if (currentType == StorageType.SHARED) {
                updateList()
            }
        })

        val livePrivateMappingMissions = mappingMissionDao.getPrivateMappingMissions(ownerId)
        livePrivateMappingMissions.observe(this, Observer {
            mappingMissionPrivateList = it.toMutableList()
            if (currentType == StorageType.PRIVATE) {
                updateList()
            }
        })
    }


    private fun populateMappingMissionsList() {

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
    }


    private fun updateList() {
        mappingMissionListView.removeAllViews()
        buttonList.clear()

        val list: MutableList<MappingMission>
        when (currentType) {
            StorageType.PRIVATE -> list = mappingMissionPrivateList
            StorageType.SHARED -> list = mappingMissionSharedList
        }

        for (mission in list) {
            val button = Button(this)
            button.height = 50
            button.width = 100
            button.text = mission.name
            if (currentType == StorageType.PRIVATE && mission.state == State.PRIVATE_AND_SHARED) {
                button.text = mission.name + "- S"
            }
            button.setOnClickListener {
                //TODO: go to mission details
            }
            buttonList.add(button)
            mappingMissionListView.addView(button)
        }
    }
}