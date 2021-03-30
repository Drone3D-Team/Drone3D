package ch.epfl.sdp.drone3d.service.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

/**
 * Implementation of the authentication service based on firebase
 */
class FirebaseAuthenticationService @Inject constructor(
        private val auth: FirebaseAuth
    ) : AuthenticationService {

    override fun hasActiveSession(): Boolean {
        return auth.currentUser != null
    }

    override fun getCurrentSession(): UserSession? {
        val fbUser = auth.currentUser
        return if (fbUser == null) null else UserSession(fbUser)
    }

    override fun register(email: String, password: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    override fun login(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    override fun sendPasswordReset(email: String): Task<Void> {
        return auth.sendPasswordResetEmail(email)
    }

    override fun signOut() {
        auth.signOut()
    }
}