/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.module

import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.impl.drone.DroneServerFactoryImpl
import ch.epfl.sdp.drone3d.service.impl.drone.DroneServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DroneModule {

    @Singleton
    @Provides
    fun provideDroneProvider() : DroneService = DroneServiceImpl(DroneServerFactoryImpl())
}