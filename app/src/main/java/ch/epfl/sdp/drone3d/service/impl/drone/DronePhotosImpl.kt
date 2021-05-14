/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.drone

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.drone3d.service.api.drone.DronePhotos
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import io.reactivex.Single
import timber.log.Timber
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

class DronePhotosImpl(val service: DroneService) : DronePhotos {

    @SuppressLint("CheckResult")
    override fun getNewPhotos(): LiveData<Bitmap> {
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")
        val data = MutableLiveData<Bitmap>()

        drone.camera.captureInfo.subscribe {
            Thread {
                try {
                    val url = URL(it.fileUrl)
                    val imageStream = url.openStream()
                    val bitmap = BitmapFactory.decodeStream(imageStream)
                    imageStream.close()
                    data.postValue(bitmap)
                } catch (e: MalformedURLException) {
                    Timber.e(e, "Error with photo url format : $e")
                } catch (e: IOException) {
                    Timber.e(e, "Error with the image download : $e")
                }
            }
        }

        return data
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