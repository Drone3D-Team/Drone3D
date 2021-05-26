/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.drone

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.espresso.matcher.ViewMatchers
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
import org.hamcrest.MatcherAssert.assertThat
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
        private val CORRECT_URLS = listOf(
            "https://user-images.githubusercontent.com/44306955/119693549-adf15b80-be4c-11eb-905f-4e1642bf4e68.jpeg",
            "https://user-images.githubusercontent.com/44306955/119693554-af228880-be4c-11eb-9907-4822ba645127.jpeg",
            "https://user-images.githubusercontent.com/44306955/119693556-b053b580-be4c-11eb-926f-3efa9fb9174e.jpeg",
            "https://user-images.githubusercontent.com/44306955/119693559-b0ec4c00-be4c-11eb-9a3b-1a0244578eeb.jpeg",
            "https://user-images.githubusercontent.com/44306955/119693560-b0ec4c00-be4c-11eb-8c1c-2d51ba7e6e64.jpeg",
            "https://user-images.githubusercontent.com/44306955/119693562-b0ec4c00-be4c-11eb-9c0e-48c2896ca43f.jpeg"
        )
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
        `when`(DroneInstanceMock.droneCamera.listPhotos(Camera.PhotosRange.SINCE_CONNECTION))
            .thenReturn(Single.just(CORRECT_URLS.map { url ->
                CameraProto.CaptureInfo.newBuilder().setFileUrl(url).build()
            }))
    }

    @Test
    fun getNewPhotosDataIsCorrectlyUpdated() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)

        val live = photos.getNewPhotos()

        val counter = CountDownLatch(1)

        val url = URL(CORRECT_URLS[0])
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
                CORRECT_URLS[0]
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
    fun getNewPhotosDataIsNotUpdatedOnWrongUrl() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)

        val live = photos.getNewPhotos()

        val counter = CountDownLatch(1)

        val url = URL(CORRECT_URLS[0])
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
                CORRECT_URLS[0]
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

        val photos: DronePhotos = DronePhotosImpl(droneService)
        val mutex = Semaphore(0)

        checkFullList(photos.getPhotos(), mutex)

        ViewMatchers.assertThat(
            mutex.tryAcquire(100, TimeUnit.MILLISECONDS),
            CoreMatchers.`is`(true)
        )
    }

    @Test
    fun get3LastPhotosReturnsCorrect3LastPhotos() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)
        val mutex = Semaphore(0)

        val n = 3

        val expected: MutableList<Bitmap> = mutableListOf()
        for (url in CORRECT_URLS.subList(CORRECT_URLS.size - n, CORRECT_URLS.size)) {
            val imageStream = URL(url).openStream()
            expected.add(BitmapFactory.decodeStream(imageStream))
            imageStream.close()
        }

        photos.getLastPhotos(n).map {
            assertThat(it.size, CoreMatchers.equalTo(n))
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

    @Test
    fun getLastPhotosWithNegativeNumberReturnsEmptyList() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)
        val mutex = Semaphore(0)

        val n = -1

        photos.getLastPhotos(n).map {
            assertThat(it.size, CoreMatchers.equalTo(0))
            mutex.release()
        }.blockingGet()

        ViewMatchers.assertThat(
            mutex.tryAcquire(100, TimeUnit.MILLISECONDS),
            CoreMatchers.`is`(true)
        )
    }

    @Test
    fun getLastPhotosWithNumberBiggerThanTotalPhotosReturnsAllPhotos() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)
        val mutex = Semaphore(0)

        val n = 10

        checkFullList(photos.getLastPhotos(n), mutex)

        ViewMatchers.assertThat(
            mutex.tryAcquire(100, TimeUnit.MILLISECONDS),
            CoreMatchers.`is`(true)
        )
    }

    @Test
    fun get3FirstPhotosReturnsCorrect3FirstPhotos() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)
        val mutex = Semaphore(0)

        val n = 3

        val expected: MutableList<Bitmap> = mutableListOf()
        for (url in CORRECT_URLS.subList(0, n)) {
            val imageStream = URL(url).openStream()
            expected.add(BitmapFactory.decodeStream(imageStream))
            imageStream.close()
        }

        photos.getFirstPhotos(n).map {
            assertThat(it.size, CoreMatchers.equalTo(n))
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

    @Test
    fun getFirstPhotosWithNegativeNumberReturnsEmptyList() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)
        val mutex = Semaphore(0)

        val n = -1

        photos.getFirstPhotos(n).map {
            assertThat(it.size, CoreMatchers.equalTo(0))
            mutex.release()
        }.blockingGet()

        ViewMatchers.assertThat(
            mutex.tryAcquire(100, TimeUnit.MILLISECONDS),
            CoreMatchers.`is`(true)
        )
    }

    @Test
    fun getFirstPhotosWithNumberBiggerThanTotalPhotosReturnsAllPhotos() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)
        val mutex = Semaphore(0)

        val n = 10

        checkFullList(photos.getFirstPhotos(n), mutex)

        ViewMatchers.assertThat(
            mutex.tryAcquire(100, TimeUnit.MILLISECONDS),
            CoreMatchers.`is`(true)
        )
    }

    @Test
    fun get3RandomPhotosReturnsSome3RandomPhotos() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)
        val mutex = Semaphore(0)

        val n = 3

        val expected: MutableList<Bitmap> = mutableListOf()
        for (url in CORRECT_URLS) {
            val imageStream = URL(url).openStream()
            expected.add(BitmapFactory.decodeStream(imageStream))
            imageStream.close()
        }

        photos.getRandomPhotos(n).map {
            assertThat(it.size, CoreMatchers.equalTo(n))
            for (actual in it) {
                var exists = false
                for (bitmap in expected) {
                    if (actual.sameAs(bitmap)) {
                        exists = true
                        // Remove from expected to detect duplicates
                        expected.remove(bitmap)
                        break
                    }
                }
                assertTrue(exists)
            }
            mutex.release()
        }.blockingGet()

        ViewMatchers.assertThat(
            mutex.tryAcquire(100, TimeUnit.MILLISECONDS),
            CoreMatchers.`is`(true)
        )
    }

    @Test
    fun getRandomPhotosWithNegativeNumberReturnsEmptyList() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)
        val mutex = Semaphore(0)

        val n = -1

        photos.getRandomPhotos(n).map {
            assertThat(it.size, CoreMatchers.equalTo(0))
            mutex.release()
        }.blockingGet()

        ViewMatchers.assertThat(
            mutex.tryAcquire(100, TimeUnit.MILLISECONDS),
            CoreMatchers.`is`(true)
        )
    }

    @Test
    fun getRandomPhotosWithNumberBiggerThanTotalPhotosReturnsAllPhotos() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)
        val mutex = Semaphore(0)

        val n = 10

        checkFullList(photos.getRandomPhotos(n), mutex)

        ViewMatchers.assertThat(
            mutex.tryAcquire(100, TimeUnit.MILLISECONDS),
            CoreMatchers.`is`(true)
        )
    }

    @Test
    fun getPhotosUrlReturnsCorrectUrls() {
        setupOwnMocks()

        val droneService = mock(DroneService::class.java)
        `when`(droneService.provideDrone()).thenReturn(DroneInstanceMock.droneSystem)

        val photos: DronePhotos = DronePhotosImpl(droneService)
        val mutex = Semaphore(0)

        val expected = CORRECT_URLS

        photos.getPhotosUrl().map {
            assertThat(it.size, CoreMatchers.equalTo(expected.size))
            for (i in expected.indices) {
                assertThat(it[i], CoreMatchers.equalTo(expected[i]))
            }
            mutex.release()
        }.blockingGet()

        ViewMatchers.assertThat(
            mutex.tryAcquire(100, TimeUnit.MILLISECONDS),
            CoreMatchers.`is`(true)
        )
    }

    private fun checkFullList(list: Single<List<Bitmap>>, mutex: Semaphore) {
        val expected: MutableList<Bitmap> = mutableListOf()
        for (url in CORRECT_URLS) {
            val imageStream = URL(url).openStream()
            expected.add(BitmapFactory.decodeStream(imageStream))
            imageStream.close()
        }

        list.map {
            assertThat(it.size, CoreMatchers.equalTo(expected.size))
            for (i in expected.indices) {
                assertTrue(it[i].sameAs(expected[i]))
            }
            mutex.release()
        }.blockingGet()
    }

}