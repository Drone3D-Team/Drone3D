package ch.epfl.sdp.drone3d

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Drone3D : Application() {

    // Singleton pattern
    companion object {
        private var instance: Drone3D? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    init {
        instance = this
    }
}