/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */
/*
 * This class was taken from the project Fly2Find and adapted for our project
 */

package ch.epfl.sdp.drone3d.drone

import io.mavsdk.System
import io.mavsdk.action.Action
import io.mavsdk.core.Core
import io.mavsdk.mission.Mission
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import io.reactivex.Flowable
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

object DroneInstanceMock {

    val mockProvider: DroneProvider = mock(DroneProvider::class.java)

    val droneSystem: System = mock(System::class.java)
    val droneTelemetry: Telemetry = mock(Telemetry::class.java)
    val droneCore: Core = mock(Core::class.java)
    val droneMission: Mission = mock(Mission::class.java)
    val droneAction: Action = mock(Action::class.java)

    init {
        resetProviderMock()

        `when`(droneSystem.telemetry)
            .thenReturn(droneTelemetry)
        `when`(droneSystem.core)
            .thenReturn(droneCore)
        `when`(droneSystem.mission)
            .thenReturn(droneMission)
        `when`(droneSystem.action)
            .thenReturn(droneAction)
    }

    fun resetProviderMock() {
        reset(mockProvider)

        `when`(mockProvider.provideDrone())
            .thenReturn(droneSystem)
        `when`(mockProvider.setSimulation(anyString(), anyString()))
                .thenAnswer {
                    an -> DroneProviderImpl.setSimulation(an.getArgument(0), an.getArgument(1))
                }
        `when`(mockProvider.getIP())
            .thenAnswer { DroneProviderImpl.getIP() }
        `when`(mockProvider.getPort())
            .thenAnswer { DroneProviderImpl.getPort() }
        `when`(mockProvider.isConnected())
            .thenAnswer { DroneProviderImpl.isConnected() }
        `when`(mockProvider.isSimulation())
            .thenAnswer { DroneProviderImpl.isSimulation() }
        `when`(mockProvider.disconnect())
            .then { DroneProviderImpl.disconnect() }
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
                Telemetry.Battery(0.0f, 0.0f)
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

        //Action mocks
        `when`(droneAction.arm())
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
    }
}