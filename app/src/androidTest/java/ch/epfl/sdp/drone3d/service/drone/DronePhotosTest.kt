/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.drone

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.service.api.drone.DronePhotos
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.impl.drone.DronePhotosImpl
import io.mavsdk.camera.Camera
import io.mavsdk.camera.CameraProto
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class DronePhotosTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        private val DEFAULT_POSITION = Camera.Position(.0, .0, 0f, 0f)
        private val DEFAULT_QUATERNION = Camera.Quaternion(0f, 0f, 0f, 0f)
        private val DEFAULT_EULER_ANGLE = Camera.EulerAngle(0f, 0f, 0f)
        private const val CORRECT_URL = "http://3.239.58.45:8080/photos/210520_162354_854.jpg"
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

        val url = URL(CORRECT_URL)
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
                CORRECT_URL
            )
        )
        // For some reason the downloaded image "photo2" from the exact same link which he downloads the photo is not the same
        //val ctx: Context = getApplicationContext()
        //val expected = BitmapFactory.decodeResource(ctx.resources, R.drawable.photo2)

        val observer = Observer<Bitmap> {
            if (it != null) {
                assertTrue(expected.sameAs(it))
                counter.countDown()
            }
        }

        live.observeForever(observer)

        counter.await(5L, TimeUnit.SECONDS)
        MatcherAssert.assertThat(counter.count, CoreMatchers.equalTo(0L))

    }

    @Test
    fun getNewPhotosDataIsNotUpdatedOnWrongUrl() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)

        val live = photos.getNewPhotos()

        val counter = CountDownLatch(1)

        val url = URL(CORRECT_URL)
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
                CORRECT_URL
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
        // For some reason the downloaded image "photo2" from the exact same link which he downloads the photo is not the same
        //val ctx: Context = getApplicationContext()
        //val expected = BitmapFactory.decodeResource(ctx.resources, R.drawable.photo2)

        val observer = Observer<Bitmap> {
            if (it != null) {
                assertTrue(expected.sameAs(it))
                counter.countDown()
            }
        }

        live.observeForever(observer)

        counter.await(5L, TimeUnit.SECONDS)
        MatcherAssert.assertThat(counter.count, CoreMatchers.equalTo(0L))

    }

    @Test
    fun getPhotosReturnsCorrectPhotos() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val info = CameraProto.CaptureInfo.newBuilder().setFileUrl(CORRECT_URL).build()
        val list = listOf(info)
        setPhotosList(list)

        val photos: DronePhotos = DronePhotosImpl(droneService)

        photos.getPhotos().map {

        }


    }

}