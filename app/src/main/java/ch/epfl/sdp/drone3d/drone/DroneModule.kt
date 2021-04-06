package ch.epfl.sdp.drone3d.drone

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DroneModule {

    @Provides
    fun provideDroneProvider() : DroneService = DroneServiceImpl
}