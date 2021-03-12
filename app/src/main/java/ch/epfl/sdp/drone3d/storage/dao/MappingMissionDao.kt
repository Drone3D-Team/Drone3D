package ch.epfl.sdp.drone3d.storage.dao
import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.storage.data.MappingMission

interface MappingMissionDao {
    /**
     * Returns the once updated private mapping mission with id [mappingId] and owned by [ownerUid]
     */
    fun getPrivateMappingMission(ownerUid:String,mappingId:String): LiveData<MappingMission>
    /**
     * Returns the once updated shared mapping mission with id [mappingId] and owned by [ownerUid]
     */
    fun getSharedMappingMission(ownerUid:String,mappingId:String): LiveData<MappingMission>
    /**
     * Returns the list of private mapping missions owned by [ownerUid], this live data list might
     * get modified afterwards if a change occurs in the database
     */
    fun getPrivateMappingMissions(ownerUid:String): LiveData<List<MappingMission>>
    /**
     * Returns the list of all shared mapping missions, this live data is updated in continuous
     * time by other users modifications
     */
    fun getSharedMappingMissions(): LiveData<List<MappingMission>>
    /**
     * Adds the private [mappingMission] of owner [ownerUid]
     */
    fun addPrivateMappingMission(ownerUid: String, mappingMission: MappingMission)
    /**
     * Adds the shared [mappingMission] of owner [ownerUid]
     */
    fun addSharedMappingMission(mappingMission: MappingMission)
    /**
     * Remove the private [mappingMission] of owner [ownerUid]
     */
    fun removePrivateMappingMission(ownerUid:String,mappingId:String)
    /**
     * Remove the shared [mappingMission] of owner [ownerUid]
     */
    fun removeSharedMappingMission(ownerUid:String,mappingId:String)
}