/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.storage.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.drone3d.model.mission.MappingMission
import ch.epfl.sdp.drone3d.model.mission.State
import ch.epfl.sdp.drone3d.service.api.storage.dao.MappingMissionDao
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class FirebaseMappingMissionDao @Inject constructor(
    private val database: FirebaseDatabase
) : MappingMissionDao {

    private val privateMappingMissions: MutableLiveData<List<MappingMission>> = MutableLiveData()
    private var privateMappingMissionsIsInit: Boolean = false
    private val sharedMappingMissions: MutableLiveData<List<MappingMission>> = MutableLiveData()
    private var sharedMappingMissionsIsInit: Boolean = false

    private val privateFilteredMappingMissions: MutableLiveData<List<MappingMission>> =
        MutableLiveData()
    private var privateFilteredMappingMissionsFilter: String? = null
    private var privateFilteredMappingMissionsQuery: Query? = null
    private var privateFilteredMappingMissionsListener: ValueEventListener? = null

    private val sharedFilteredMappingMissions: MutableLiveData<List<MappingMission>> =
        MutableLiveData()
    private var sharedFilteredMappingMissionsFilter: String? = null
    private var sharedFilteredMappingMissionsQuery: Query? = null
    private var sharedFilteredMappingMissionsListener: ValueEventListener? = null

    companion object {
        private const val TAG = "FirebaseMappingMissionDao"
        private const val PRIVATE_ID_PATH = "privateId"
        private const val SHARED_ID_PATH = "sharedId"
        private const val STATE_PATH = "state"
        private const val MAPPING_MISSIONS_PATH = "mappingMissions"
    }

    /**
     * Return the firebase database reference to the repo of private mapping missions of the user specified by [UID]
     */
    private fun privateMappingMissionRef(UID: String): DatabaseReference {
        return database.getReference("users/$UID/$MAPPING_MISSIONS_PATH")
    }

    /**
     * Return the firebase database reference to the repo of shared mapping missions
     */
    private fun sharedMappingMissionRef(): DatabaseReference {
        return database.getReference(MAPPING_MISSIONS_PATH)
    }

    /**
     * Update the given [missionList] liveData from the dataSnapshot taken from the database
     */
    private fun createValueEventListenerForMissionList(missionList: MutableLiveData<List<MappingMission>>): ValueEventListener{
        return object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val missionsSnapshot = dataSnapshot.children.map { c ->
                    c.getValue(MappingMission::class.java)!!
                }
                missionList.value = missionsSnapshot
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.tag(TAG).w(databaseError.toException(), "getMappingMissions:onCancelled")
            }
        }
    }

    /**
     * Return a the mapping mission specified by its [id]
     * If the [ownerUid] is not null the mission is fetch from private repo of the owner
     * Otherwise in the shared repo
     */
    private fun getMappingMission(ownerUid: String?, id: String): LiveData<MappingMission> {
        val mission: MutableLiveData<MappingMission> = MutableLiveData()

        val missionListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val missionSnapshot = dataSnapshot.getValue<MappingMission>()
                mission.value = missionSnapshot
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.tag(TAG).w(databaseError.toException(), "getMappingMission:onCancelled")
            }
        }

        val rootRef =
            if (ownerUid != null) privateMappingMissionRef(ownerUid) else sharedMappingMissionRef()
        rootRef.child(id).addListenerForSingleValueEvent(missionListener)
        return mission
    }

    override fun getPrivateMappingMission(
        ownerUid: String,
        privateId: String
    ): LiveData<MappingMission> {
        return getMappingMission(ownerUid, privateId)
    }

    override fun getSharedMappingMission(sharedId: String): LiveData<MappingMission> {
        return getMappingMission(null, sharedId)
    }

    /**
     * Return a all mapping mission of a repo
     * If the [ownerUid] is not null the missions are fetch from private repo of the owner
     * Otherwise in the shared repo
     */
    private fun getMappingMissions(ownerUid: String?): LiveData<List<MappingMission>> {
        val mappingMissionIsInit =
            if (ownerUid != null) privateMappingMissionsIsInit else sharedMappingMissionsIsInit
        val rootRef =
            if (ownerUid != null) privateMappingMissionRef(ownerUid) else sharedMappingMissionRef()
        val mappingMissions =
            if (ownerUid != null) privateMappingMissions else sharedMappingMissions

        if (!mappingMissionIsInit) {
            val missionsListener = createValueEventListenerForMissionList(mappingMissions)

            rootRef.addValueEventListener(missionsListener)

            if (ownerUid != null) {
                privateMappingMissionsIsInit = true
            } else {
                sharedMappingMissionsIsInit = true
            }
        }

        return mappingMissions
    }

    override fun getPrivateMappingMissions(ownerUid: String): LiveData<List<MappingMission>> {
        return getMappingMissions(ownerUid)
    }

    override fun getSharedMappingMissions(): LiveData<List<MappingMission>> {
        return getMappingMissions(null)
    }

    override fun getPrivateFilteredMappingMissions(): LiveData<List<MappingMission>> {
        privateFilteredMappingMissions.value = emptyList()
        return privateFilteredMappingMissions
    }

    override fun getSharedFilteredMappingMissions(): LiveData<List<MappingMission>> {
        sharedFilteredMappingMissions.value = emptyList()
        return sharedFilteredMappingMissions
    }

    /**
     * Update the list of mapping missions filtered by the new [filter]
     * If the [ownerUid] is not null the missions are fetch from private repo of the owner
     * Otherwise in the shared repo
     */
    private fun updateFilteredMappingMissions(ownerUid: String?, filter: String) {
        val rootRef =
            if (ownerUid != null) privateMappingMissionRef(ownerUid) else sharedMappingMissionRef()
        val mappingMissions =
            if (ownerUid != null) privateFilteredMappingMissions else sharedFilteredMappingMissions
        val query =
            if (ownerUid != null) privateFilteredMappingMissionsQuery else sharedFilteredMappingMissionsQuery
        val listener =
            if (ownerUid != null) privateFilteredMappingMissionsListener else sharedFilteredMappingMissionsListener

        if (query != null && listener != null) {
            query.removeEventListener(listener)
        }

        val missionsQuery =
            rootRef.orderByChild("nameUpperCase").startAt(filter).endAt(filter + "\uf8ff")
        val missionsListener = missionsQuery.addValueEventListener(createValueEventListenerForMissionList(mappingMissions))

        if (ownerUid != null) {
            privateFilteredMappingMissionsFilter = filter
            privateFilteredMappingMissionsQuery = missionsQuery
            privateFilteredMappingMissionsListener = missionsListener
        } else {
            sharedFilteredMappingMissionsFilter = filter
            sharedFilteredMappingMissionsQuery = missionsQuery
            sharedFilteredMappingMissionsListener = missionsListener
        }
    }

    override fun updatePrivateFilteredMappingMissions(ownerUid: String, filter: String?) {
        if (filter != null && privateFilteredMappingMissionsFilter != filter.toUpperCase(Locale.ROOT)) {
            updateFilteredMappingMissions(ownerUid, filter.toUpperCase(Locale.ROOT))
        }
    }

    override fun updateSharedFilteredMappingMissions(filter: String?) {
        if (filter != null && sharedFilteredMappingMissionsFilter != filter.toUpperCase(Locale.ROOT)) {
            updateFilteredMappingMissions(null, filter.toUpperCase(Locale.ROOT))
        }
    }

    override fun storeMappingMission(ownerUid: String, mappingMission: MappingMission): LiveData<Boolean> {
        val privateId = privateMappingMissionRef(ownerUid).push().key
        mappingMission.privateId = privateId
        mappingMission.ownerUid = ownerUid
        mappingMission.nameUpperCase = mappingMission.name.toUpperCase(Locale.ROOT)
        when (mappingMission.state) {
            // Not already stored => only in private repo
            State.NOT_STORED -> mappingMission.state = State.PRIVATE
            // Already stored in shared repo => private and shared
            State.SHARED -> {
                mappingMission.state = State.PRIVATE_AND_SHARED

                // Update state and privateId in shared repo
                val refSharedMission = sharedMappingMissionRef().child(mappingMission.sharedId!!)
                refSharedMission.child(PRIVATE_ID_PATH).setValue(privateId)
                refSharedMission.child(STATE_PATH).setValue(State.PRIVATE_AND_SHARED)
            }
            else -> {
            }
        }
        val result = MutableLiveData<Boolean>()
        privateMappingMissionRef(ownerUid).child(privateId!!)
            .setValue(mappingMission).addOnSuccessListener { result.value = true }
            .addOnFailureListener { result.value = false }
        return result
    }

    override fun shareMappingMission(
        ownerUid: String,
        mappingMission: MappingMission
    ): LiveData<Boolean> {
        val sharedId = sharedMappingMissionRef().push().key
        mappingMission.sharedId = sharedId
        mappingMission.ownerUid = ownerUid
        mappingMission.nameUpperCase = mappingMission.name.toUpperCase(Locale.ROOT)
        when (mappingMission.state) {
            // Not already stored => only in shared repo
            State.NOT_STORED -> mappingMission.state = State.SHARED
            // Already stored in private repo => private and shared
            State.PRIVATE -> {
                mappingMission.state = State.PRIVATE_AND_SHARED

                // Update state and sharedId in private repo
                val refPrivateMission =
                    privateMappingMissionRef(ownerUid).child(mappingMission.privateId!!)
                refPrivateMission.child(SHARED_ID_PATH).setValue(sharedId)
                refPrivateMission.child(STATE_PATH).setValue(State.PRIVATE_AND_SHARED)
            }
            else -> {
            }
        }
        val result = MutableLiveData<Boolean>()
        sharedMappingMissionRef().child(sharedId!!).setValue(mappingMission)
            .addOnSuccessListener { result.value = true }
            .addOnFailureListener { result.value = false }
        return result
    }

    override fun removePrivateMappingMission(ownerUid: String, privateId: String) {
        val missionListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.getValue<MappingMission>()?.run {

                    // We need to update the shared copy in case there is one
                    if (this.state == State.PRIVATE_AND_SHARED) {
                        // Remove privateId and change state
                        val refSharedMission = sharedMappingMissionRef().child(this.sharedId!!)
                        refSharedMission.child(PRIVATE_ID_PATH).removeValue()
                        refSharedMission.child(STATE_PATH).setValue(State.SHARED)
                    }
                    // Remove mission from the private repo
                    privateMappingMissionRef(ownerUid).child(privateId).removeValue()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.tag(TAG)
                    .w(databaseError.toException(), "removePrivateMappingMission:onCancelled")
            }
        }

        privateMappingMissionRef(ownerUid).child(privateId)
            .addListenerForSingleValueEvent(missionListener)
    }

    override fun removeSharedMappingMission(ownerUid: String, sharedId: String) {
        val missionListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.getValue<MappingMission>()?.run {

                    // We need to update the private copy in case there is one
                    if (this.state == State.PRIVATE_AND_SHARED) {
                        // Remove sharedId and change state in private copy
                        val refPrivateMission =
                            privateMappingMissionRef(ownerUid).child(this.privateId!!)
                        refPrivateMission.child(SHARED_ID_PATH).removeValue()
                        refPrivateMission.child(STATE_PATH).setValue(State.PRIVATE)
                    }
                    // Remove mission from the shared repo
                    sharedMappingMissionRef().child(sharedId).removeValue()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.tag(TAG)
                    .w(databaseError.toException(), "removePrivateMappingMission:onCancelled")
            }
        }

        // Get the mission data before removing it
        sharedMappingMissionRef().child(sharedId).addListenerForSingleValueEvent(missionListener)
    }

    override fun removeMappingMission(ownerUid: String, privateId: String?, sharedId: String?) {
        if (privateId != null) {
            privateMappingMissionRef(ownerUid).child(privateId).removeValue()
        }
        if (sharedId != null) {
            sharedMappingMissionRef().child(sharedId).removeValue()
        }
    }
}
