/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.drone.api

import androidx.lifecycle.MutableLiveData
import io.mavsdk.mission.Mission
import io.reactivex.disposables.Disposable

interface DroneDataEditable : DroneData {

    /**
     * Returns the MutableLiveData of the mission plan
     */
    fun getMutableMission(): MutableLiveData<List<Mission.MissionItem>>

    /**
     * Returns the MutableLiveData keeping the mission pause state
     */
    fun getMutableMissionPaused(): MutableLiveData<Boolean>

    /**
     * Add a disposable to the subscription list
     */
    fun addSubscription(disposable: Disposable)
}