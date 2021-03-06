/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.api.storage.dao

import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.model.mission.MappingMission

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
     * Returns the list of all private mapping missions filtered by the current filter.
     * When initially called, the return list is empty until updatePrivateFilteredMappingMissions is called.
     * This live data is updated in continuous time if the private missions are modified
     * and also when updatePrivateFilteredMappingMissions is called with another filter
     */
    fun getPrivateFilteredMappingMissions(): LiveData<List<MappingMission>>

    /**
     * Returns the list of all shared mapping missions filtered by the current filter.
     * When initially called, the return list is empty until updateSharedFilteredMappingMissions is called.
     * This live data is updated in continuous time if the shared missions are modified
     * and also when updateSharedFilteredMappingMissions is called with another filter
     */
    fun getSharedFilteredMappingMissions(): LiveData<List<MappingMission>>

    /**
     * Updates the list of all private filtered mapping missions
     * by a new list of all private mapping missions filtered by the new filter
     */
    fun updatePrivateFilteredMappingMissions(ownerUid: String, filter: String?)

    /**
     * Updates the list of all shared filtered mapping missions
     * by a new list of all shared mapping missions filtered by the new filter
     */
    fun updateSharedFilteredMappingMissions(filter: String?)

    /**
     * Store the private [mappingMission] of owner [ownerUid]
     * Return if the operation has been successful when it completes
     */
    fun storeMappingMission(ownerUid: String, mappingMission: MappingMission): LiveData<Boolean>

    /**
     * Share the [mappingMission] of owner [ownerUid]
     * Return if the operation has been successful when it completes
     */
    fun shareMappingMission(ownerUid: String, mappingMission: MappingMission): LiveData<Boolean>

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
     * Remove the [MappingMission] of owner [ownerUid], from the private and shared repository
     */
    fun removeMappingMission(ownerUid: String, privateId: String?, sharedId: String?)

}
