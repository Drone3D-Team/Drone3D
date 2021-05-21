/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.drone

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.api.drone.DronePhotos
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.impl.drone.DronePhotosImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mavsdk.camera.Camera
import io.mavsdk.camera.CameraProto
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.*
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class DronePhotosTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        private val DEFAULT_POSITION = Camera.Position(.0, .0, 0f, 0f)
        private val DEFAULT_QUATERNION = Camera.Quaternion(0f, 0f, 0f, 0f)
        private val DEFAULT_EULER_ANGLE = Camera.EulerAngle(0f, 0f, 0f)
        private const val CORRECT_URL_1 =
            "https://raw.githubusercontent.com/Drone3D-Team/Drone3D/work-jose-retrieve-photos/app/src/main/res/drawable/photo1.jpeg"
        private const val CORRECT_URL_2 =
            "https://raw.githubusercontent.com/Drone3D-Team/Drone3D/work-jose-retrieve-photos/app/src/main/res/drawable/photo2.jpeg"
        private const val WRONG_URL = "a wrong url"
    }

    private val captureInfoPublisher: PublishSubject<Camera.CaptureInfo> = PublishSubject.create()

    private fun setupOwnMocks() {
        DroneInstanceMock.setupDefaultMocks()

        `when`(DroneInstanceMock.droneCamera.captureInfo)
            .thenReturn(
                captureInfoPublisher.toFlowable(BackpressureStrategy.BUFFER)
                    .cacheWithInitialCapacity(1)
            )
    }

    private fun setPhotosList(list: List<CameraProto.CaptureInfo>) {
        `when`(DroneInstanceMock.droneCamera.listPhotos(Camera.PhotosRange.SINCE_CONNECTION))
            .thenReturn(Single.just(list))
    }

    @Test
    fun getNewPhotosDataIsCorrectlyUpdated() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)

        val live = photos.getNewPhotos()

        val counter = CountDownLatch(1)

        val url = URL(CORRECT_URL_1)
        val imageStream = url.openStream()
        val expected = BitmapFactory.decodeStream(imageStream)
        imageStream.close()

        captureInfoPublisher.onNext(
            Camera.CaptureInfo(
                DEFAULT_POSITION,
                DEFAULT_QUATERNION,
                DEFAULT_EULER_ANGLE,
                0,
                true,
                0,
                CORRECT_URL_1
            )
        )
        // For some reason the downloaded image "photo1" from the exact same link which he downloads the photo is not the same
        //val ctx: Context = getApplicationContext()
        //val expected = BitmapFactory.decodeResource(ctx.resources, R.drawable.photo1)

        val observer = Observer<Bitmap> {
            if (it != null) {
                assertTrue(expected.sameAs(it))
                counter.countDown()
            }
        }

        live.observeForever(observer)

        counter.await(5L, TimeUnit.SECONDS)
        assertThat(counter.count, CoreMatchers.equalTo(0L))

    }

    @Test
    fun getNewPhotosDataIsNotUpdatedOnWrongUrl() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)

        val live = photos.getNewPhotos()

        val counter = CountDownLatch(1)

        val url = URL(CORRECT_URL_1)
        val imageStream = url.openStream()
        val expected = BitmapFactory.decodeStream(imageStream)
        imageStream.close()

        captureInfoPublisher.onNext(
            Camera.CaptureInfo(
                DEFAULT_POSITION,
                DEFAULT_QUATERNION,
                DEFAULT_EULER_ANGLE,
                0,
                true,
                0,
                CORRECT_URL_1
            )
        )
        captureInfoPublisher.onNext(
            Camera.CaptureInfo(
                DEFAULT_POSITION,
                DEFAULT_QUATERNION,
                DEFAULT_EULER_ANGLE,
                0,
                true,
                0,
                WRONG_URL
            )
        )
        val observer = Observer<Bitmap> {
            if (it != null) {
                assertTrue(expected.sameAs(it))
                counter.countDown()
            }
        }

        live.observeForever(observer)

        counter.await(5L, TimeUnit.SECONDS)
        assertThat(counter.count, CoreMatchers.equalTo(0L))

    }

    @Test
    fun getPhotosReturnsCorrectPhotos() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val info1 = CameraProto.CaptureInfo.newBuilder().setFileUrl(CORRECT_URL_1).build()
        val info2 = CameraProto.CaptureInfo.newBuilder().setFileUrl(CORRECT_URL_2).build()
        val list = listOf(info1, info2)
        setPhotosList(list)

        val photos: DronePhotos = DronePhotosImpl(droneService)
        val mutex = Semaphore(0)

        val expected: MutableList<Bitmap> = mutableListOf()
        for (url in list.map { it.fileUrl }) {
            val imageStream = URL(url).openStream()
            expected.add(BitmapFactory.decodeStream(imageStream))
            imageStream.close()
        }

        photos.getPhotos().map {
            assertThat(it.size, CoreMatchers.equalTo(2))
            for (i in expected.indices) {
                assertTrue(it[i].sameAs(expected[i]))
            }
            mutex.release()
        }.blockingGet()

        ViewMatchers.assertThat(
            mutex.tryAcquire(100, TimeUnit.MILLISECONDS),
            CoreMatchers.`is`(true)
        )

    }

}