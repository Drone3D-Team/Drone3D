package ch.epfl.sdp.drone3d.storage.dao

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import ch.epfl.sdp.drone3d.storage.data.State
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class FirebaseMappingMissionDao(private val database: FirebaseDatabase):MappingMissionDao {

    private val privateMappingMissions: MutableLiveData<List<MappingMission>> = MutableLiveData()
    private val sharedMappingMissions: MutableLiveData<List<MappingMission>> = MutableLiveData()


    companion object {
        private const val TAG = "FirebaseMappingMissionDao"
        private const val PRIVATE_ID_PATH = "privateId"
        private const val SHARED_ID_PATH = "sharedId"
        private const val STATE_PATH = "state"
        private const val MAPPING_MISSIONS_PATH = "mappingMissions"
    }

    private fun privateMappingMissionRef(UID: String): DatabaseReference {
        return database.getReference("users/$UID/$MAPPING_MISSIONS_PATH")
    }

    private fun sharedMappingMissionRef(): DatabaseReference {
        return database.getReference(MAPPING_MISSIONS_PATH)
    }

    override fun getPrivateMappingMission(
        ownerUid: String,
        privateId: String
    ): LiveData<MappingMission> {
        val privateMission: MutableLiveData<MappingMission> = MutableLiveData()

        val missionListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val mission = dataSnapshot.getValue<MappingMission>()
                privateMission.value = mission
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "getPrivateMappingMission:onCancelled", databaseError.toException())
            }
        }
        privateMappingMissionRef(ownerUid).child(privateId).addListenerForSingleValueEvent(missionListener)

        return privateMission;
    }

    override fun getSharedMappingMission(
            ownerUid: String,
            sharedId: String
    ): LiveData<MappingMission> {
        val sharedMission: MutableLiveData<MappingMission> = MutableLiveData()

        val missionListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val mission = dataSnapshot.getValue<MappingMission>()
                sharedMission.value = mission
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "getSharedMappingMission:onCancelled", databaseError.toException())
            }
        }
        sharedMappingMissionRef().child(sharedId).addListenerForSingleValueEvent(missionListener)

        return sharedMission;
    }

    override fun getPrivateMappingMissions(ownerUid: String): LiveData<List<MappingMission>> {
        val missionsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val missions = dataSnapshot.children.map { c ->
                    c.getValue(MappingMission::class.java)!!
                }
                privateMappingMissions.value = missions
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "getPrivateMappingMissions:onCancelled", databaseError.toException())
            }
        }
        privateMappingMissionRef(ownerUid).addValueEventListener(missionsListener)

        return privateMappingMissions
    }

    override fun getSharedMappingMissions(): LiveData<List<MappingMission>> {
        val missionsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val missions = dataSnapshot.children.map { c ->
                    c.getValue(MappingMission::class.java)!!
                }
                sharedMappingMissions.value = missions
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "getSharedMappingMissions:onCancelled", databaseError.toException())
            }
        }
        sharedMappingMissionRef().addValueEventListener(missionsListener)

        return sharedMappingMissions
    }

    override fun storeMappingMission(ownerUid: String, mappingMission: MappingMission) {
        val id = privateMappingMissionRef(ownerUid).push().key
        mappingMission.privateId = id;
        mappingMission.ownerUid = ownerUid;
        when(mappingMission.state){
            // Not already stored => only in private repo
            State.RAM -> mappingMission.state = State.PRIVATE
            // Already stored in shared repo => private and shared
            State.SHARED -> {
                mappingMission.state = State.PRIVATE_AND_SHARED

                // Update state and privateId in shared repo
                val refSharedMission =  sharedMappingMissionRef().child(mappingMission.sharedId!!);
                refSharedMission.child(PRIVATE_ID_PATH).setValue(id)
                refSharedMission.child(STATE_PATH).setValue(State.PRIVATE_AND_SHARED)
            }
            else -> {}
        }
        privateMappingMissionRef(ownerUid).child(id!!).setValue(mappingMission)
    }


    override fun shareMappingMission(ownerUid: String, mappingMission: MappingMission) {
        val sharedId = sharedMappingMissionRef().push().key
        mappingMission.sharedId = sharedId
        mappingMission.ownerUid = ownerUid;
        when(mappingMission.state){
            // Not already stored => only in shared repo
            State.RAM -> mappingMission.state = State.SHARED
            // Already stored in private repo => private and shared
            State.PRIVATE -> {
                mappingMission.state = State.PRIVATE_AND_SHARED

                // Update state and sharedId in private repo
                val refPrivateMission = privateMappingMissionRef(ownerUid).child(mappingMission.privateId!!)
                refPrivateMission.child(SHARED_ID_PATH).setValue(sharedId)
                refPrivateMission.child(STATE_PATH).setValue(State.PRIVATE_AND_SHARED)
            }
            else -> {}
        }
        sharedMappingMissionRef().child(sharedId!!).setValue(mappingMission)
    }

    override fun removePrivateMappingMission(ownerUid: String, privateId: String) {
        val missionListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val mission = dataSnapshot.getValue<MappingMission>()
                if (mission != null) {

                    // We need to update the shared copy in case there is one
                    if(mission.state == State.PRIVATE_AND_SHARED && mission.sharedId != null){

                        // Remove privateId and change state
                        val refSharedMission =  sharedMappingMissionRef().child(mission.sharedId!!);
                        refSharedMission.child(PRIVATE_ID_PATH).removeValue()
                        refSharedMission.child(STATE_PATH).setValue(State.SHARED)
                    }
                    // Remove mission from the private repo
                    privateMappingMissionRef(ownerUid).child(privateId).removeValue()
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "removePrivateMappingMission:onCancelled", databaseError.toException())
            }
        }

        privateMappingMissionRef(ownerUid).child(privateId).addListenerForSingleValueEvent(missionListener)
    }

    override fun removeSharedMappingMission(ownerUid: String, sharedId: String) {
        val missionListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val mission = dataSnapshot.getValue<MappingMission>()
                if (mission != null) {

                    // We need to update the private copy in case there is one
                    if(mission.state == State.PRIVATE_AND_SHARED && mission.privateId != null){

                        // Remove sharedId and change state in private copy
                        val refPrivateMission = privateMappingMissionRef(ownerUid).child(mission.privateId!!)
                        refPrivateMission.child(SHARED_ID_PATH).removeValue()
                        refPrivateMission.child(STATE_PATH).setValue(State.PRIVATE)
                    }
                    // Remove mission from the shared repo
                    sharedMappingMissionRef().child(sharedId).removeValue()
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "removePrivateMappingMission:onCancelled", databaseError.toException())
            }
        }

        // Get the mission data before removing it
        sharedMappingMissionRef().child(sharedId).addListenerForSingleValueEvent(missionListener)
    }

    override fun removeMappingMission(ownerUid: String, privateId: String?, sharedId: String?) {
        if(privateId != null){
            privateMappingMissionRef(ownerUid).child(privateId).removeValue()
        }
        if(sharedId != null){
            sharedMappingMissionRef().child(sharedId).removeValue()
        }

    }
}