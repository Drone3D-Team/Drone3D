/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.api.drone

import android.content.Context
import io.mavsdk.mission.Mission
import java.util.concurrent.CompletableFuture

/**
 * An interface responsible of launching and controlling the missions of the drone
 */
interface DroneExecutor {

    /**
     * Start a new mission that follows the given [missionPlan]
     *
     * Shows toasts of the [context]
     */
    fun startMission(context: Context, missionPlan: Mission.MissionPlan)

    /**
     * Pause current mission.
     *
     * Has no effect if there are no current mission or if it is already paused
     *
     * Shows toasts of the [context]
     */
    fun pauseMission(context: Context)

    /**
     * Resume current paused mission.
     *
     * Has no effect if there are no current mission or if it is already running
     *
     * Shows toasts of the [context]
     */
    fun resumeMission(context: Context)

    /**
     * Start a mission where the drone goes back to home location and land
     *
     * Shows toasts of the [context]
     */
    fun returnToHomeLocationAndLand(context: Context, future: CompletableFuture<Boolean>)

    /**
     * Start a mission where the drone goes back to user location and land
     *
     * Shows toasts of the [context]
     */
    fun returnToUserLocationAndLand(context: Context, future: CompletableFuture<Boolean>)
}