package ch.epfl.sdp.drone3d.storage.dao

import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import com.google.android.gms.tasks.Task

interface MappingMissionDao {

    fun getPrivateMappingMission(ownerUid: String, privateId: String): LiveData<MappingMission>
    fun getSharedMappingMission(ownerUid: String, privateId: String): LiveData<MappingMission>
    fun getPrivateMappingMissions(ownerUid: String): LiveData<List<MappingMission>>
    fun getSharedMappingMissions(): LiveData<List<MappingMission>>
    fun storeMappingMission(ownerUid: String, mappingMission: MappingMission)
    fun shareMappingMission(ownerUid: String, mappingMission: MappingMission)
    fun removePrivateMappingMission(ownerUid: String, privateId: String)
    fun removeSharedMappingMission(ownerUid: String, sharedId: String)
    fun removeMappingMission(ownerUid: String, privateId: String?, sharedId: String?)

}
