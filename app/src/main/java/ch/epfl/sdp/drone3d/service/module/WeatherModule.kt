/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.module

import ch.epfl.sdp.drone3d.service.api.weather.WeatherService
import ch.epfl.sdp.drone3d.service.impl.weather.OpenWeatherWeatherService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provide the [WeatherService]
 */
@Module
abstract class WeatherModule {

    @Binds
    abstract fun bindWeatherService(impl: OpenWeatherWeatherService): WeatherService
}
