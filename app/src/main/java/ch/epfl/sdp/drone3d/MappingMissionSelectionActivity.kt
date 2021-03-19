package ch.epfl.sdp.drone3d

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.storage.data.MappingMission

/**
 * The activity that allows the user to browse his mapping missions.
 */
class MappingMissionSelectionActivity : AppCompatActivity() {

    private lateinit var selectedStorageTypeToggleButton: ToggleButton
    private lateinit var mappingMissionListView: LinearLayout
    private lateinit var createNewMappingMissionButton: Button
    private val buttonList = mutableListOf<Button>()
    private val mappingMissionPrivateList = mutableListOf<MappingMission>()
    private val mappingMissionSharedList = mutableListOf<MappingMission>()

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

        populateMappingMissionsList()

        updateList()

        createNewMappingMissionButton.setOnClickListener {
            val intent = Intent(this, ItineraryCreateActivity::class.java).apply {}
            startActivity(intent)
        }

    }

    private fun populateMappingMissionsList() {
        for (i in 0..10) {
            mappingMissionPrivateList.add(MappingMission("Mission $i",emptyList()))
        }
        for (i in 10..20) {
            mappingMissionSharedList.add(MappingMission("Mission $i",emptyList()))
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
            button.setOnClickListener {
                //TODO: go to mission details
            }
            buttonList.add(button)
            mappingMissionListView.addView(button)
        }
    }
}