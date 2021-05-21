/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.api.drone

import android.content.Context
import io.mavsdk.mission.Mission
import io.reactivex.Completable
import java.util.concurrent.CompletableFuture

/**
 * An interface responsible of launching and controlling the missions of the drone
 */
interface DroneExecutor {

    /**
     * Setup a new mission that follows the given [missionPlan]
     *
     * Ues [executeMission] to start it
     *
     * Shows toasts on the [ctx]
     */
    fun setupMission(ctx: Context, missionPlan: Mission.MissionPlan): Completable

    /**
     * Execute a mission set up by [setupMission]
     *
     * Shows toasts on the [ctx]
     */
    fun executeMission(ctx: Context): Completable

    /**
     * Pause current mission.
     *
     * Has no effect if there are no current mission or if it is already paused
     *
     * Shows toasts on the [ctx]
     */
    fun pauseMission(ctx: Context): Completable

    /**
     * Resume current paused mission.
     *
     * Has no effect if there are no current mission or if it is already running
     *
     * Shows toasts on the [ctx]
     */
    fun resumeMission(ctx: Context): Completable

    /**
     * Start a mission where the drone goes back to home location and land
     *
     * Shows toasts on the [ctx]
     *
     * Returns a [CompletableFuture] then will complete once the drone has landed
     */
    fun returnToHomeLocationAndLand(ctx: Context): Completable

    /**
     * Start a mission where the drone goes back to user location and land
     *
     * Shows toasts on the [ctx]
     *
     * Returns a [CompletableFuture] then will complete once the drone has landed
     */
    fun returnToUserLocationAndLand(ctx: Context): Completable
}