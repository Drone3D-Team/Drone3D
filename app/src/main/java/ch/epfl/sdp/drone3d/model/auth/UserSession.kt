/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.model.auth

import com.google.firebase.auth.FirebaseUser

/**
 * The session of a logged in user
 */
data class UserSession(val user: FirebaseUser)