package ch.epfl.sdp.drone3d

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Drone3D : Application() {

    init {
        instance = this
    }

    // Singleton pattern
    companion object {
        private var instance: Drone3D? = null

        fun getInstance(): Drone3D {
            return instance!!
        }
    }
}