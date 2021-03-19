package ch.epfl.sdp.drone3d.storage.repositories

import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.storage.dao.FirebaseMappingMissionDao
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MappingMissionRepository {
    companion object {
        private var DEFAULT_DAO: MappingMissionDao

        init {
            val database = Firebase.database("https://drone3d-6819a-default-rtdb.europe-west1.firebasedatabase.app/")
            database.setPersistenceEnabled(true)
            DEFAULT_DAO = FirebaseMappingMissionDao(database)
        }

        var daoProvider: () -> MappingMissionDao = { DEFAULT_DAO }
    }

    private val dao: MappingMissionDao = daoProvider()

    /**
     * Returns the once updated private mapping mission with id [mappingId] and owned by [privateId]
     */
    fun getPrivateMappingMission(ownerUid: String, privateId: String): LiveData<MappingMission> {
        return dao.getPrivateMappingMission(ownerUid,privateId)
    }

    /**
     * Returns the once updated shared mapping mission with id [mappingId] and owned by [privateId]
     */
    fun getSharedMappingMission(ownerUid: String, privateId: String): LiveData<MappingMission> {
        return dao.getSharedMappingMission(ownerUid,privateId)
    }

    /**
     * Returns the list of private mapping missions owned by [ownerUid], this live data list might
     * get modified afterwards if a change occurs in the database
     */
    fun getPrivateMappingMissions(ownerUid: String): LiveData<List<MappingMission>> {
        return dao.getPrivateMappingMissions(ownerUid)
    }

    /**
     * Returns the list of all shared mapping missions, this live data is updated in continuous
     * time by other users modifications
     */
    fun getSharedMappingMissions(): LiveData<List<MappingMission>> {
        return dao.getSharedMappingMissions()
    }

    /**
     * Store the private [mappingMission] of owner [ownerUid]
     */
    fun storeMappingMission(ownerUid: String, mappingMission: MappingMission) {
        return dao.storeMappingMission(ownerUid,mappingMission)
    }

    /**
     * Share the [mappingMission] of owner [ownerUid]
     */
    fun shareMappingMission(ownerUid: String, mappingMission: MappingMission) {
        return dao.shareMappingMission(ownerUid,mappingMission)
    }

    /**
     * Remove the private mappingMission of owner [ownerUid], if was private amd shared,
     * it will remain shared
     */
    fun removePrivateMappingMission(ownerUid: String, privateId: String) {
        return dao.removePrivateMappingMission(ownerUid,privateId)
    }

    /**
     * Remove the shared [mappingMission] of owner [ownerUid], if it was private and shared,
     * it will remain in the shared repository
     */
    fun removeSharedMappingMission(ownerUid: String, sharedId: String) {
        return dao.removeSharedMappingMission(ownerUid,sharedId)
    }

    /**
     * Remove the [mappingMission] of owner [ownerUid], from the private and shared repository
     */
    fun removeMappingMission(ownerUid: String, privateId: String?, sharedId: String?) {
        return dao.removeMappingMission(ownerUid,privateId,sharedId)
    }
}