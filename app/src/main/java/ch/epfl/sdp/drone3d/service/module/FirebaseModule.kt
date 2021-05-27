/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.module

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    var database: FirebaseDatabase? = null

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth =
        Firebase.auth

    @Singleton
    @Provides
    @Synchronized
    fun provideFirebaseDatabase(): FirebaseDatabase {
        if (database == null) {
            database =
                FirebaseDatabase.getInstance("https://drone3d-6819a-default-rtdb.europe-west1.firebasedatabase.app/")
            database?.setPersistenceEnabled(true)
        }
        return database!!
    }
}
