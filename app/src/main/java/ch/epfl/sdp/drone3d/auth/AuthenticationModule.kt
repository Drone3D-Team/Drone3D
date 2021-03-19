package ch.epfl.sdp.drone3d.auth

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthenticationModule {

    @Singleton
    @Binds
    abstract fun bindAuthenticationService(impl: FirebaseAuthenticationService): AuthenticationService
}
