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
import io.mavsdk.camera.Camera
import io.mavsdk.camera.CameraProto
import io.reactivex.Single
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import timber.log.Timber
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

class DronePhotosImpl(val service: DroneService) : DronePhotos {

    private val photosCache = mutableMapOf<CameraProto.CaptureInfo, Bitmap>()

    @SuppressLint("CheckResult")
    override fun getNewPhotos(): LiveData<Bitmap> {
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")
        val data = MutableLiveData<Bitmap>()

        drone.camera.captureInfo.subscribe { captureInfo ->
            GlobalScope.async {
                val image = getPhoto(captureInfo.fileUrl)
                if (image != null) {
                    data.postValue(image)
                }
            }
        }

        return data
    }

    override fun getPhotos(): Single<List<Bitmap>> {
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        return drone.camera.listPhotos(Camera.PhotosRange.SINCE_CONNECTION).map { photoList ->
            val coroutine = GlobalScope.async {
                getPhotos(photoList)
            }
            coroutine.getCompleted()
        }
    }

    override fun getLastPhotos(n: Int): Single<List<Bitmap>> {
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        return drone.camera.listPhotos(Camera.PhotosRange.SINCE_CONNECTION).map { photoList ->
            val size = photoList.size
            val list = if (size <= n) photoList else photoList.subList(size - n, size)
            val coroutine = GlobalScope.async {
                getPhotos(list)
            }
            coroutine.getCompleted()
        }
    }

    override fun getFirstPhotos(n: Int): Single<List<Bitmap>> {
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        return drone.camera.listPhotos(Camera.PhotosRange.SINCE_CONNECTION).map { photoList ->
            val size = photoList.size
            val list = if (size <= n) photoList else photoList.subList(0, n)
            val coroutine = GlobalScope.async {
                getPhotos(list)
            }
            coroutine.getCompleted()
        }
    }

    override fun getRandomPhotos(n: Int): Single<List<Bitmap>> {
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        return drone.camera.listPhotos(Camera.PhotosRange.SINCE_CONNECTION).map { photoList ->
            val size = photoList.size
            if (size > n) {
                photoList.shuffle()
            }
            val list = if (size <= n) {
                photoList
            } else {
                photoList.shuffle()
                photoList.subList(0, n)
            }
            val coroutine = GlobalScope.async {
                getPhotos(list)
            }
            coroutine.getCompleted()
        }
    }

    private fun getPhoto(fileUrl: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val url = URL(fileUrl)
            val imageStream = url.openStream()
            bitmap = BitmapFactory.decodeStream(imageStream)
            imageStream.close()
        } catch (e: MalformedURLException) {
            Timber.e(e, "Error with photo url format : $e")
        } catch (e: IOException) {
            Timber.e(e, "Error with the image download : $e")
        }
        return bitmap
    }

    private suspend fun getPhotos(list: List<CameraProto.CaptureInfo>): List<Bitmap> {
        return list.map { captureInfo ->
            GlobalScope.async {
                // First check if image is cached
                var image = photosCache[captureInfo]
                // If not download it
                if (image == null) {
                    image = getPhoto(captureInfo.fileUrl)
                    // If the download didn't fail, cache the image
                    if (image != null) {
                        photosCache[captureInfo] = image
                    }
                }
                image
            }
        }.mapNotNull { it.await() }
    }

}