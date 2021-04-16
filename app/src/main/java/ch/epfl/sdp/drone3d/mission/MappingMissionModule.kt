package ch.epfl.sdp.drone3d.mission

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//@Module
//@InstallIn(SingletonComponent::class)
abstract class MappingMissionModule {

//    @Singleton
//    @Binds
    abstract fun bindMappingMissionService(impl: MappingMissionService): MappingMissionService
}