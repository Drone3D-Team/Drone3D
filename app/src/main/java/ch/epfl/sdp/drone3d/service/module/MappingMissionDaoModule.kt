/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.module

import ch.epfl.sdp.drone3d.service.api.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.service.impl.storage.dao.FirebaseMappingMissionDao
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provide the [MappingMissionDao]
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MappingMissionDaoModule {

    @Singleton
    @Binds
    abstract fun bindMappingMissionDao(impl: FirebaseMappingMissionDao): MappingMissionDao
}
