package ch.epfl.sdp.drone3d.service.api.drone

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import io.reactivex.Single

interface DronePhotos {

    fun getNewPhotos(): LiveData<Bitmap>

    fun getPhotos(): Single<List<Bitmap>>

    fun getLastPhotos(n: Int): Single<List<Bitmap>>

    fun getFirstPhotos(n: Int): Single<List<Bitmap>>

    fun getRandomPhotos(n: Int): Single<List<Bitmap>>

}