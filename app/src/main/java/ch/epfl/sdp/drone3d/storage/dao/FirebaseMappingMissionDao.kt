package ch.epfl.sdp.drone3d.storage.dao

import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseMappingMissionDao:MappingMissionDao {

    private val database =
            Firebase.database("https://drone3d-6819a-default-rtdb.europe-west1.firebasedatabase.app/")

    companion object {
        private const val TAG = "FirebaseMappingMissionDao"
    }

    private fun privateMappingMissionRef(UID: String): DatabaseReference {
        return database.getReference("users/$UID/mappingMissions")
    }

    private fun sharedMappingMissionRef(): DatabaseReference {
        return database.getReference("mappingMissions")
    }

    override fun getPrivateMappingMission(
        ownerUid: String,
        mappingId: String
    ): LiveData<MappingMission> {
        TODO("Not yet implemented")
    }

    override fun getSharedMappingMission(
        ownerUid: String,
        mappingId: String
    ): LiveData<MappingMission> {
        TODO("Not yet implemented")
    }

    override fun getPrivateMappingMissions(ownerUid: String): LiveData<List<MappingMission>> {
        TODO("Not yet implemented")
    }

    override fun getSharedMappingMissions(): LiveData<List<MappingMission>> {
        TODO("Not yet implemented")
    }

    override fun addPrivateMappingMission(ownerUid: String, mappingMission: MappingMission) {
        val id = privateMappingMissionRef(ownerUid).push().key
        mappingMission.id = id;
        privateMappingMissionRef(ownerUid).child(id!!).setValue(mappingMission)
    }


    override fun addSharedMappingMission(mappingMission: MappingMission) {
        TODO("Not yet implemented")
    }

    override fun removePrivateMappingMission(ownerUid: String, mappingId: String) {
        TODO("Not yet implemented")
    }

    override fun removeSharedMappingMission(ownerUid: String, mappingId: String) {
        TODO("Not yet implemented")
    }
}