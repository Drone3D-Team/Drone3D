/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.model.mission

import com.mapbox.mapboxsdk.geometry.LatLng


/**
 * The State is used to specify where the MappingMission is stored
 * It's either:
 * - NOT_STORED: currently the data is only in the ram and may be lost when closing the app
 * - PRIVATE: stored in the private repo of the user
 * - SHARED: stored only in the shared repo (the owner has no private copy)
 * - PRIVATE_AND_SHARED: stored in the private repo of the owner as well as in the shared repo
 */
enum class State {
    NOT_STORED, PRIVATE, SHARED, PRIVATE_AND_SHARED
}

/**
 * A MappingMission is instantiated with a [name], a [flightPath], a [flightHeight] and a [cameraPitch].
 * The [privateId], [sharedId] and [state] are updated according to where the MappingMission are stored.
 * The [ownerUid] is set the first time the mission is either stored or shared.
 */
data class MappingMission(
    //TODO: could be split into 2 substructure, one for the metadata and one for the
    // actual mission(flight path, flight height, camera pitch)
    val name: String = "",
    val flightPath: List<LatLng> = listOf(),
    val flightHeight:Double = 0.0,
    val cameraPitch:Float = 0f,
    var privateId: String? = null,
    var sharedId: String? = null,
    var state: State = State.NOT_STORED,
    var ownerUid: String? = null,
    var nameUpperCase: String = "",
)
