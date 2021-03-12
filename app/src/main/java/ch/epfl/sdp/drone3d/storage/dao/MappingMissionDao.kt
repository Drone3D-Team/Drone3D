package ch.epfl.sdp.drone3d.storage.dao

import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import com.google.android.gms.tasks.Task

interface MappingMissionDao {

    /**
     * Returns the once updated private mapping mission with id [mappingId] and owned by [privateId]
     */
    fun getPrivateMappingMission(ownerUid: String, privateId: String): LiveData<MappingMission>
    /**
     * Returns the once updated shared mapping mission with id [mappingId] and owned by [privateId]
     */
    fun getSharedMappingMission(ownerUid: String, privateId: String): LiveData<MappingMission>
    /**
     * Returns the list of private mapping missions owned by [ownerUid], this live data list might
     * get modified afterwards if a change occurs in the database
     */
    fun getPrivateMappingMissions(ownerUid: String): LiveData<List<MappingMission>>
    /**
     * Returns the list of all shared mapping missions, this live data is updated in continuous
     * time by other users modifications
     */
    fun getSharedMappingMissions(): LiveData<List<MappingMission>>
    /**
     * Store the private [mappingMission] of owner [ownerUid]
     */
    fun storeMappingMission(ownerUid: String, mappingMission: MappingMission)
    /**
     * Share the [mappingMission] of owner [ownerUid]
     */
    fun shareMappingMission(ownerUid: String, mappingMission: MappingMission)
    /**
     * Remove the private mappingMission of owner [ownerUid], if was private amd shared,
     * it will remain shared
     */
    fun removePrivateMappingMission(ownerUid: String, privateId: String)
    /**
     * Remove the shared [mappingMission] of owner [ownerUid], if it was private and shared,
     * it will remain in the shared repository
     */
    fun removeSharedMappingMission(ownerUid: String, sharedId: String)
    /**
     * Remove the [mappingMission] of owner [ownerUid], from the private and shared repository
     */
    fun removeMappingMission(ownerUid: String, privateId: String?, sharedId: String?)

}
