/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

/**
 * Implementation of the authentication service based on firebase
 */
class FirebaseAuthenticationService : AuthenticationService {

    /**
     * Default constructor, injected through hilt
     */
    @Inject constructor()

    /**
     * Constructor to specify the firebase auth to use.
     *
     * This is mostly for testing purpose
     */
    constructor(auth: FirebaseAuth) : this() {
        this.auth = auth
    }

    private lateinit var auth: FirebaseAuth

    private fun getAuth(): FirebaseAuth {
        // Query the auth if it is not initialized yet
        if (!this::auth.isInitialized)
            auth = Firebase.auth
        return auth
    }

    override fun hasActiveSession(): Boolean {
        return getAuth().currentUser != null
    }

    override fun getCurrentSession(): UserSession? {
        val fbUser = getAuth().currentUser
        return if (fbUser == null) null else UserSession(fbUser)
    }

    override fun register(email: String, password: String): Task<AuthResult> {
        return getAuth().createUserWithEmailAndPassword(email, password)
    }

    override fun login(email: String, password: String): Task<AuthResult> {
        return getAuth().signInWithEmailAndPassword(email, password)
    }

    override fun sendPasswordReset(email: String): Task<Void> {
        return getAuth().sendPasswordResetEmail(email)
    }

    override fun signOut() {
        getAuth().signOut()
    }
}