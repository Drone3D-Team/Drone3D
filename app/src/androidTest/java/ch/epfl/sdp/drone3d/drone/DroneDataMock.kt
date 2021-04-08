/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.drone

import androidx.lifecycle.MutableLiveData
import org.mockito.Mockito
import org.mockito.Mockito.`when`

object DroneDataMock {
    private val dataMock: DroneData = Mockito.mock(DroneData::class.java)

    val droneService: DroneService = Mockito.mock(DroneService::class.java)

    private val isConnected: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        `when`(droneService.getData()).thenReturn(dataMock)
        `when`(dataMock.isConnected()).thenReturn(isConnected)
    }

    fun setupDefaultMock() {

    }
}