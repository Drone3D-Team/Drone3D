/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.drone

import ch.epfl.sdp.drone3d.drone.api.DroneService
import ch.epfl.sdp.drone3d.drone.impl.DroneServerFactoryImpl
import ch.epfl.sdp.drone3d.drone.impl.DroneServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DroneModule {

    @Provides
    fun provideDroneProvider() : DroneService = DroneServiceImpl(DroneServerFactoryImpl())
}