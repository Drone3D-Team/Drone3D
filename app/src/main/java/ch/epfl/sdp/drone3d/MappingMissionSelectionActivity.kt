package ch.epfl.sdp.drone3d

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import ch.epfl.sdp.drone3d.data.MappingMission

/**
 * The activity that allows the user to browse his mapping missions.
 */
class MappingMissionSelectionActivity : AppCompatActivity() {

    private lateinit var mappingMissionListView: LinearLayout
    private lateinit var createNewMappingMissionButton: Button
    private val buttonList = mutableListOf<Button>()
    private val mappingMissionList = mutableListOf<MappingMission>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapping_mission_selection)

        mappingMissionListView = findViewById(R.id.mappingMissionList)
        createNewMappingMissionButton = findViewById(R.id.createMappingMissionButton)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        populateMappingMissionsList()

        for (mission in mappingMissionList) {
            val button = Button(this)
            button.height = 50
            button.width = 100
            button.text = mission.name
            button.setOnClickListener {
                //TODO: go to mission details with edit and mission launch options
            }
            buttonList.add(button)
            mappingMissionListView.addView(button)
        }

        createNewMappingMissionButton.setOnClickListener {
            //TODO: go to mapping mission creation
        }

    }

    private fun populateMappingMissionsList() {
        for (i in 0..10) {
            mappingMissionList.add(MappingMission(emptyList(), "Mission $i"))
        }
    }
}