/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */
/*
 * This class was taken from the project Fly2Find and adapted for our project
 */

package ch.epfl.sdp.drone3d.service.drone

import ch.epfl.sdp.drone3d.service.api.drone.DroneData
import ch.epfl.sdp.drone3d.service.api.drone.DroneService
import ch.epfl.sdp.drone3d.service.impl.drone.DroneDataImpl
import io.mavsdk.System
import io.mavsdk.action.Action
import io.mavsdk.camera.Camera
import io.mavsdk.core.Core
import io.mavsdk.mission.Mission
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

object DroneInstanceMock {

    val droneSystem: System = mock(System::class.java)
    val droneTelemetry: Telemetry = mock(Telemetry::class.java)
    val droneCore: Core = mock(Core::class.java)
    val droneMission: Mission = mock(Mission::class.java)
    val droneAction: Action = mock(Action::class.java)
    val droneCamera: Camera = mock(Camera::class.java)

    init {
        `when`(droneSystem.telemetry)
            .thenReturn(droneTelemetry)
        `when`(droneSystem.core)
            .thenReturn(droneCore)
        `when`(droneSystem.mission)
            .thenReturn(droneMission)
        `when`(droneSystem.action)
            .thenReturn(droneAction)
        `when`(droneSystem.camera)
            .thenReturn(droneCamera)
    }

    /**
     * Returns a mocked version of [DroneService] that provides a mocked version of
     * [DroneData] through getData() and a mock [System] through provideDrone().
     *
     * The [System] can be parametrised using the different values in [DroneInstanceMock]
     */
    fun mockService(): DroneService {
        setupDefaultMocks()

        return mock(DroneService::class.java).apply {
            val mockData = mock(DroneData::class.java)
            `when`(this.getData()).thenReturn(mockData)
            `when`(this.provideDrone()).thenReturn(droneSystem)
        }
    }

    /**
     * Returns a mocked version of [DroneService] that provides an instance of
     * [DroneDataImpl] through getData() and a mock [System] through provideDrone().
     *
     * The [System] can be parametrised using the different values in [DroneInstanceMock]
     */
    fun mockServiceWithDefaultData(): DroneService {
        setupDefaultMocks()

        return mock(DroneService::class.java).apply {
            `when`(this.provideDrone()).thenReturn(droneSystem)
            val data = DroneDataImpl(this)
            `when`(this.getData()).thenReturn(data)
        }
    }

    fun setupDefaultMocks() {
        // Telemetry Mocks
        `when`(droneTelemetry.flightMode)
            .thenReturn(
                Flowable.fromArray(
                Telemetry.FlightMode.LAND,
                Telemetry.FlightMode.MISSION,
                Telemetry.FlightMode.HOLD,
                Telemetry.FlightMode.MISSION
            ))
        `when`(droneTelemetry.armed)
            .thenReturn(Flowable.fromArray(
                true
            ))
        `when`(droneTelemetry.position)
            .thenReturn(Flowable.fromArray(
                Telemetry.Position(0.0, 0.0, 0.0f, 0.0f),
                Telemetry.Position(0.1, 0.0, 0.0f, 0.0f),
                Telemetry.Position(0.2, 0.0, 0.0f, 0.0f),
                Telemetry.Position(0.3, 0.0, 0.0f, 0.0f)
            ))
        `when`(droneTelemetry.battery)
            .thenReturn(Flowable.fromArray(
                Telemetry.Battery(5.0f, 10.0f)
            ))
        `when`(droneTelemetry.positionVelocityNed)
            .thenReturn(Flowable.fromArray(
                Telemetry.PositionVelocityNed(
                    Telemetry.PositionNed(0.0f, 0.0f, 0.0f),
                    Telemetry.VelocityNed(0.0f, 0.0f, 0.0f)
                )
            ))
        `when`(droneTelemetry.home)
            .thenReturn(Flowable.fromArray(
                Telemetry.Position(0.0, 0.0, 0.0f, 0.0f)
            ))
        `when`(droneTelemetry.inAir)
            .thenReturn(Flowable.fromArray(
                true
            ))

        //Core mocks
        `when`(droneCore.connectionState)
            .thenReturn(Flowable.fromArray(
                Core.ConnectionState(0L, true)
            ))

        //Mission mocks
        `when`(droneMission.pauseMission())
            .thenReturn(Completable.complete())
        `when`(droneMission.setReturnToLaunchAfterMission(ArgumentMatchers.anyBoolean()))
            .thenReturn(Completable.complete())
        `when`(droneMission.uploadMission(ArgumentMatchers.any()))
            .thenReturn(Completable.complete())
        `when`(droneMission.startMission())
            .thenReturn(Completable.complete())
        `when`(droneMission.clearMission())
            .thenReturn(Completable.complete())
        `when`(droneMission.missionProgress)
            .thenReturn(Flowable.fromArray(
                Mission.MissionProgress(0, 4),
                Mission.MissionProgress(1, 4),
                Mission.MissionProgress(2, 4),
                Mission.MissionProgress(3, 4),
                Mission.MissionProgress(4, 4)
            ))
        `when`(droneMission.isMissionFinished)
            .thenReturn(Single.just(true))

        //Action mocks
        `when`(droneAction.arm())
            .thenReturn(Completable.complete())
        `when`(droneAction.takeoff())
            .thenReturn(Completable.complete())
        `when`(droneAction.gotoLocation(
            ArgumentMatchers.anyDouble(),
            ArgumentMatchers.anyDouble(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat()))
            .thenReturn(Completable.complete())
        `when`(droneAction.returnToLaunch())
            .thenReturn(Completable.complete())
        `when`(droneAction.land())
            .thenReturn(Completable.complete())
        `when`(droneAction.disarm())
            .thenReturn(Completable.complete())

        // Camera
        `when`(droneCamera.information)
            .thenReturn(
                Flowable.fromArray(Camera.Information(
                    "vendor",
                    "model",
                                45f,
                                15f,
                                10f,
                                2500,
                                2000))
            )
        `when`(droneCamera.videoStreamInfo)
            .thenReturn(
                Flowable.fromArray(Camera.VideoStreamInfo(
                    Camera.VideoStreamSettings(
                        30f,
                        2500,
                        2500,
                        60,
                        0,
                        "uri",
                        0f
                            ),
                    Camera.VideoStreamInfo.VideoStreamStatus.IN_PROGRESS,
                    Camera.VideoStreamInfo.VideoStreamSpectrum.VISIBLE_LIGHT
                ))
            )
    }
}