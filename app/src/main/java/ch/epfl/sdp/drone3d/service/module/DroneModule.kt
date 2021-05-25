/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.module

import ch.epfl.sdp.drone3d.service.api.drone.DronePhotos
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.impl.drone.DronePhotosImpl
import ch.epfl.sdp.drone3d.service.impl.drone.DroneServerFactoryImpl
import ch.epfl.sdp.drone3d.service.impl.drone.DroneServiceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DroneModule {

    companion object {
        @Singleton
        @Provides
        fun provideDroneProvider(locationService: LocationService): DroneService =
            DroneServiceImpl(DroneServerFactoryImpl(), locationService)
    }

    @Singleton
    @Binds
    abstract fun bindDronePhotos(dronePhotosImpl: DronePhotosImpl): DronePhotos
}