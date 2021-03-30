package ch.epfl.sdp.drone3d.service.storage.dao

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
