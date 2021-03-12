package ch.epfl.sdp.drone3d.storage.repositories

import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.storage.dao.FirebaseMappingMissionDao
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.storage.data.MappingMission

class MappingMissionRepository {
    companion object{
        val DEFAULT_DAO = FirebaseMappingMissionDao()

        var daoProvider: () -> MappingMissionDao = { DEFAULT_DAO }
    }
    private val dao: MappingMissionDao = daoProvider()

    /**
     * Returns the once updated private mapping mission with id [mappingId] and owned by [ownerUid]
     */
    fun getPrivateMappingMission(ownerUid:String,mappingId:String): LiveData<MappingMission> {
        return dao.getPrivateMappingMission(ownerUid,mappingId)
    }
    /**
     * Returns the once updated shared mapping mission with id [mappingId] and owned by [ownerUid]
     */
    fun getSharedMappingMission(ownerUid:String,mappingId:String): LiveData<MappingMission> {
        return dao.getSharedMappingMission(ownerUid,mappingId)
    }
    /**
     * Returns the list of private mapping missions owned by [ownerUid], this live data list might
     * get modified afterwards if a change occurs in the database
     */
    fun getPrivateMappingMissions(ownerUid:String): LiveData<List<MappingMission>> {
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
     * Adds the private [mappingMission] of owner [ownerUid]
     */
    fun addPrivateMappingMission(ownerUid: String, mappingMission: MappingMission) {
        return dao.addPrivateMappingMission(ownerUid,mappingMission)
    }
    /**
     * Adds the shared [mappingMission] of owner [ownerUid]
     */
    fun addSharedMappingMission(mappingMission: MappingMission) {
        return dao.addSharedMappingMission(mappingMission)
    }
    /**
     * Remove the private [mappingMission] of owner [ownerUid]
     */
    fun removePrivateMappingMission(ownerUid:String,mappingId:String) {
        return dao.removePrivateMappingMission(ownerUid,mappingId)
    }
    /**
     * Remove the shared [mappingMission] of owner [ownerUid]
     */
    fun removeSharedMappingMission(ownerUid:String,mappingId:String) {
        return dao.removeSharedMappingMission(ownerUid,mappingId)
    }






}