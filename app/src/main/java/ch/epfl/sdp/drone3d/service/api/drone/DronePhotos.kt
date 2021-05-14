package ch.epfl.sdp.drone3d.service.api.drone

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import io.reactivex.Single

/**
 * An interface that allows you get photos taken by the drone
 */
interface DronePhotos {

    /**
     * Returns a live data containing a bitmap of the last image taken.
     */
    fun getNewPhotos(): LiveData<Bitmap>

    /**
     * Returns all the image of the drone since its connection.
     */
    fun getPhotos(): Single<List<Bitmap>>

    /**
     * Returns the last [n] images taken by the drone.
     * If the drone took less than [n] images, it will return all of them.
     */
    fun getLastPhotos(n: Int): Single<List<Bitmap>>

    /**
     * Returns the first [n] images taken by the drone.
     * If the drone took less than [n] images, it will return all of them.
     */
    fun getFirstPhotos(n: Int): Single<List<Bitmap>>

    /**
     * Returns [n] random images taken by the drone.
     * If the drone took less than [n] images, it will return all of them.
     */
    fun getRandomPhotos(n: Int): Single<List<Bitmap>>

}