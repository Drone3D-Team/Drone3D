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
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject

/**
 * This class is an implementation of [DronePhotos].
 * It allows you to retrieve photos taken by the drone.
 */
class DronePhotosImpl @Inject constructor(val service: DroneService) : DronePhotos {

    private val photosCache = mutableMapOf<String, Bitmap>()

    @SuppressLint("CheckResult")
    override fun getNewPhotos(): LiveData<Bitmap> {
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")
        val data = MutableLiveData<Bitmap>()

        drone.camera.captureInfo.subscribe { captureInfo ->
            GlobalScope.async {
                val image = retrievePhoto(captureInfo.fileUrl)
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
            var result: List<Bitmap> = emptyList()
            runBlocking {
                val coroutine = GlobalScope.async {
                    retrievePhotos(photoList)
                }
                coroutine.await()
                result = coroutine.getCompleted()
            }
            result
        }
    }

    override fun getLastPhotos(n: Int): Single<List<Bitmap>> {
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        if (n <= 0) return Single.just(emptyList())

        return drone.camera.listPhotos(Camera.PhotosRange.SINCE_CONNECTION).map { photoList ->
            var result: List<Bitmap> = emptyList()
            val size = photoList.size
            val list = if (size <= n) photoList else photoList.subList(size - n, size)
            runBlocking {
                val coroutine = GlobalScope.async {
                    retrievePhotos(list)
                }
                coroutine.await()
                result = coroutine.getCompleted()
            }
            result
        }
    }

    override fun getFirstPhotos(n: Int): Single<List<Bitmap>> {
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        if (n <= 0) return Single.just(emptyList())

        return drone.camera.listPhotos(Camera.PhotosRange.SINCE_CONNECTION).map { photoList ->
            var result: List<Bitmap> = emptyList()
            val size = photoList.size
            val list = if (size <= n) photoList else photoList.subList(0, n)
            runBlocking {
                val coroutine = GlobalScope.async {
                    retrievePhotos(list)
                }
                coroutine.await()
                result = coroutine.getCompleted()
            }
            result
        }
    }

    override fun getRandomPhotos(n: Int): Single<List<Bitmap>> {
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        if (n <= 0) return Single.just(emptyList())

        return drone.camera.listPhotos(Camera.PhotosRange.SINCE_CONNECTION).map { photoList ->
            var result: List<Bitmap> = emptyList()
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
            runBlocking {
                val coroutine = GlobalScope.async {
                    retrievePhotos(list)
                }
                coroutine.await()
                result = coroutine.getCompleted()
            }
            result
        }
    }

    override fun getPhotosUrl(): Single<List<String>> {
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        return drone.camera.listPhotos(Camera.PhotosRange.SINCE_CONNECTION)
            .map { list -> list.map { captureInfo -> captureInfo.fileUrl } }
    }

    private fun retrievePhoto(fileUrl: String): Bitmap? {
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

    private suspend fun retrievePhotos(list: List<CameraProto.CaptureInfo>): List<Bitmap> {
        return list.map { captureInfo ->
            GlobalScope.async {
                // First check if image is cached
                var image = photosCache[captureInfo.fileUrl]
                // If not download it
                if (image == null) {
                    image = retrievePhoto(captureInfo.fileUrl)
                    // If the download didn't fail, cache the image
                    if (image != null) {
                        photosCache[captureInfo.fileUrl] = image
                    }
                }
                image
            }
        }.mapNotNull { it.await() }
    }

}