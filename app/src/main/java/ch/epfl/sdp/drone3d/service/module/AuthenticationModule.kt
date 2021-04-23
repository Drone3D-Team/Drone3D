/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.module

import ch.epfl.sdp.drone3d.service.api.auth.AuthenticationService
import ch.epfl.sdp.drone3d.service.impl.auth.FirebaseAuthenticationService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provide the [AuthenticationService]
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthenticationModule {

    @Singleton
    @Binds
    abstract fun bindAuthenticationService(impl: FirebaseAuthenticationService): AuthenticationService
}
