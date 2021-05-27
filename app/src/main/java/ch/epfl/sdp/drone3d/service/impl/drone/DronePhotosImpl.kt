/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.drone

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.drone3d.service.api.drone.DronePhotos
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import io.mavsdk.camera.Camera
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class DronePhotosImpl @Inject constructor(val service: DroneService) : DronePhotos {

    companion object {
        private const val URL_QUERY_RETRIES = 10L
        private const val URL_QUERY_DELAY_ON_ERROR = 300L // millis
    }

    private val photosCache = mutableMapOf<String, Bitmap>()
    private val disposables = CompositeDisposable()

    override fun getNewPhotos(): LiveData<Bitmap> {
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")
        val data = MutableLiveData<Bitmap>()

        disposables.add(
                drone.camera.captureInfo
                        .subscribeOn(Schedulers.io())
                        .subscribe {
                            captureInfo -> retrievePhoto(captureInfo.fileUrl)?.let { data.postValue(it) }
                        }
        )

        return data
    }

    override fun getPhotos(): Single<List<Bitmap>> {
        return getPhotosUrl()
                .map { retrievePhotos(it) }
    }

    override fun getLastPhotos(n: Int): Single<List<Bitmap>> {
        if (n <= 0) return Single.just(emptyList())

        return getPhotosUrl()
                .map { it.subList(max(it.size - n, 0), it.size) }
                .map { retrievePhotos(it) }
    }

    override fun getFirstPhotos(n: Int): Single<List<Bitmap>> {
        if (n <= 0) return Single.just(emptyList())

        return getPhotosUrl()
                .map { it.subList(0, min(it.size, n)) }
                .map { retrievePhotos(it) }
    }

    override fun getRandomPhotos(n: Int): Single<List<Bitmap>> {
        if (n <= 0) return Single.just(emptyList())

        return getPhotosUrl()
                .map { it.shuffled() }
                .map { it.subList(0, min(it.size, n)) }
                .map { retrievePhotos(it) }
    }

    override fun getPhotosUrl(): Single<List<String>> {
        val drone =
            service.provideDrone() ?: throw IllegalStateException("Could not query drone instance")

        return drone.camera.listPhotos(Camera.PhotosRange.SINCE_CONNECTION)
                .subscribeOn(Schedulers.io())
                .retryWhen { it.take(URL_QUERY_RETRIES).delay(URL_QUERY_DELAY_ON_ERROR, TimeUnit.MILLISECONDS) }
                .map { it.map { captureInfo -> captureInfo.fileUrl } }
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

    private fun retrievePhotos(list: List<String>): List<Bitmap> {
        return list.mapNotNull { url ->
            // First check if image is cached
            var image = photosCache[url]
            if (image == null) {
                // If not download it
                image = retrievePhoto(url)
                // If the download didn't fail, cache the image
                if (image != null)
                    photosCache[url] = image
            }

            image
        }
    }


    protected fun finalize() {
        disposables.dispose()
    }
}