package ch.epfl.sdp.drone3d.storage.dao
import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.storage.data.MappingMission

interface MappingMissionDao {

    fun getPrivateMappingMission(ownerUid:String,mappingId:String):LiveData<MappingMission>
    fun getSharedMappingMission(ownerUid:String,mappingId:String): LiveData<MappingMission>
    fun getPrivateMappingMissions(ownerUid:String): LiveData<List<MappingMission>>
    fun getSharedMappingMissions(): LiveData<List<MappingMission>>
    fun addPrivateMappingMission(mappingMission: MappingMission)
    fun addSharedMappingMission(mappingMission: MappingMission)
    fun removePrivateMappingMission(ownerUid:String,mappingId:String)
    fun removeSharedMappingMission(ownerUid:String,mappingId:String)
}