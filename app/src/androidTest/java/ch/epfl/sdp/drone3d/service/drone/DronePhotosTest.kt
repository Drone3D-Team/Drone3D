/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.drone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.service.api.drone.DronePhotos
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.impl.drone.DronePhotosImpl
import io.mavsdk.camera.Camera
import io.mavsdk.camera.CameraProto
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class DronePhotosTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

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
    }

}