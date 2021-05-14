/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.drone

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.service.api.drone.DronePhotos
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import io.reactivex.Single

class DronePhotosImpl(val service: DroneService) : DronePhotos {

    override fun getNewPhotos(): LiveData<Bitmap> {
        TODO("Not yet implemented")
    }

    override fun getPhotos(): Single<List<Bitmap>> {
        TODO("Not yet implemented")
    }

    override fun getLastPhotos(n: Int): Single<List<Bitmap>> {
        TODO("Not yet implemented")
    }

    override fun getFirstPhotos(n: Int): Single<List<Bitmap>> {
        TODO("Not yet implemented")
    }

    override fun getRandomPhotos(n: Int): Single<List<Bitmap>> {
        TODO("Not yet implemented")
    }

}