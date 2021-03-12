package ch.epfl.sdp.drone3d.storage.dao

import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.storage.data.MappingMission

class FirebaseMappingMissionDao:MappingMissionDao {
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

    override fun addPrivateMappingMission(mappingMission: MappingMission) {
        TODO("Not yet implemented")
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