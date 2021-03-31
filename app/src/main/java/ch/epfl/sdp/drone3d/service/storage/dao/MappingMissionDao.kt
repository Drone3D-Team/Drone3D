/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.storage.dao

import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.service.storage.data.MappingMission

interface MappingMissionDao {

    /**
     * Returns the private mapping mission with id [privateId] and owned by [ownerUid], this liveData is
     * updated once with the mapping mission if it exists when the function is called
     */
    fun getPrivateMappingMission(ownerUid: String, privateId: String): LiveData<MappingMission>

    /**
     * Returns the shared mapping mission with id [sharedId], this liveData is
     * updated once with the mapping mission if it exists when the function is called
     */
    fun getSharedMappingMission(sharedId: String): LiveData<MappingMission>

    /**
     * Returns the list of private mapping missions owned by [ownerUid], this liveData is
     * updated in continuous time if the private missions are modified
     */
    fun getPrivateMappingMissions(ownerUid: String): LiveData<List<MappingMission>>

    /**
     * Returns the list of all shared mapping missions, this liveData is updated in continuous
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
     * Remove the private mapping mission of owner [ownerUid], if it was private and shared,
     * it will remain in the shared repository
     */
    fun removePrivateMappingMission(ownerUid: String, privateId: String)

    /**
     * Remove the shared mapping mission of owner [ownerUid], if it was private and shared,
     * it will remain in the private repository
     */
    fun removeSharedMappingMission(ownerUid: String, sharedId: String)

    /**
     * Remove the [mappingMission] of owner [ownerUid], from the private and shared repository
     */
    fun removeMappingMission(ownerUid: String, privateId: String?, sharedId: String?)

}
