/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.drone

import android.util.Log
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import io.mavsdk.camera.Camera
import io.reactivex.Completable
import io.reactivex.disposables.Disposable

class DronePhotosImpl(val service: DroneService) {

    lateinit var disposable: Disposable

    fun printPhoto() {
        Log.d("MAVphoto", "Call print photo")
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")
        val camera = drone.camera ?: throw IllegalStateException("Could not query the drone camera")
        Log.d("MAVphoto", "Settup listener photo")
        disposable = camera.captureInfo.subscribe {
            Log.d("MAVphoto", "New photo")
            Log.d("MAVphoto", it.fileUrl)
        }
    }

    fun printAllPhotos(): Completable {
        Log.d("MAVphoto", "Call print all photos")
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")
        val camera = drone.camera ?: throw IllegalStateException("Could not query the drone camera")
        Log.d("MAVphoto", "Settup listener all photos")

        return camera.listPhotos(Camera.PhotosRange.ALL).doOnSuccess {
            Log.d("MAVphoto", "New " + it.size + " photos !")
            for (captureInfo in it) {
                Log.d("MAVphoto", "Photo URL " + captureInfo.fileUrl)
            }
        }.doOnError {
            Log.d("MAVphoto", "Fail photos !")
        }.toCompletable()
    }

}