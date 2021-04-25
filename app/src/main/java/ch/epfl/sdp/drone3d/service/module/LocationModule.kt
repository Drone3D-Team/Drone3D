/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.module

import android.content.Context
import android.location.Criteria
import android.location.LocationManager
import ch.epfl.sdp.drone3d.service.api.location.LocationService
import ch.epfl.sdp.drone3d.service.impl.location.AndroidLocationService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt module that provides [LocationService]
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    internal annotation class LocationProvider

    @Singleton
    @Binds
    abstract fun bindLocationService(locationServiceImpl: AndroidLocationService): LocationService

    companion object {
        @Singleton
        @Provides
        fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
            return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        @Provides
        @LocationProvider
        fun provideLocationProvider(
            locationManager: LocationManager,
            locationCriteria: Criteria
        ): String? {
            return locationManager.getBestProvider(locationCriteria, true)
        }

        @Singleton
        @Provides
        fun provideCriteria(): Criteria {
            val criteria = Criteria()
            criteria.powerRequirement = Criteria.POWER_MEDIUM
            criteria.accuracy = Criteria.ACCURACY_FINE
            return criteria
        }
    }

}