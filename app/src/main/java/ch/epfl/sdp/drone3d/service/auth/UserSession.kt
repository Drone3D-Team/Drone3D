package ch.epfl.sdp.drone3d.service.auth

import com.google.firebase.auth.FirebaseUser

data class UserSession(val user: FirebaseUser)