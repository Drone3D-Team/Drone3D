package ch.epfl.sdp.drone3d.drone

import io.mavsdk.mission.Mission

interface DroneExecutor {

    /**
     *
     */
    fun startMission(missionPlan: Mission.MissionPlan, groupId: String)

    /**
     * Pause current mission.
     *
     * Has no effect if there are no current mission or if it is already paused
     */
    fun pauseMission()

    /**
     * Resume current paused mission.
     *
     * Has no effect if there are no current mission or if it is already running
     */
    fun resumeMission()

    /**
     * Start a mission where the drone goes back to home location and land
     */
    fun returnToHomeLocationAndLand()

    /**
     * Start a mission where the drone goes back to user location and land
     */
    fun returnToUserLocationAndLand()
}