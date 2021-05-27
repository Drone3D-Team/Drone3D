/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.api.drone

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import io.reactivex.Single

/**
 * An interface that allows you get photos taken by the drone
 */
interface DronePhotos {

    /**
     * Return a live data containing a bitmap of the last image taken.
     */
    fun getNewPhotos(): LiveData<Bitmap>

    /**
     * Return all the image of the drone since its connection.
     * It may not return all photos in case a download failed.
     */
    fun getPhotos(): Single<List<Bitmap>>

    /**
     * Return the last [n] images taken by the drone.
     * If [n] is negative, it will return an empty list.
     * If the drone took less than [n] images, it will return all of them.
     * It may return less than [n] photos in case a download failed.
     */
    fun getLastPhotos(n: Int): Single<List<Bitmap>>

    /**
     * Return the first [n] images taken by the drone.
     * If [n] is negative, it will return an empty list.
     * If the drone took less than [n] images, it will return all of them.
     * It may return less than [n] photos in case a download failed.
     */
    fun getFirstPhotos(n: Int): Single<List<Bitmap>>

    /**
     * Return [n] random images taken by the drone.
     * If [n] is negative, it will return an empty list.
     * If the drone took less than [n] images, it will return all of them.
     * It may return less than [n] photos in case a download failed.
     */
    fun getRandomPhotos(n: Int): Single<List<Bitmap>>

    /**
     * Return all the url of the photos of the drone since its connection.
     */
    fun getPhotosUrl(): Single<List<String>>

}