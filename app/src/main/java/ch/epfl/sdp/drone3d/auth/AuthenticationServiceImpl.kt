package ch.epfl.sdp.drone3d.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthenticationServiceImpl : AuthenticationService{

    private lateinit var auth: FirebaseAuth

    private fun getAuth(): FirebaseAuth {
        if (!this::auth.isInitialized)
            auth = Firebase.auth
        return auth
    }

    override fun hasActiveSession(): Boolean {
        return auth.currentUser != null
    }

    override fun getCurrentSession(): UserSession? {
        val fbUser = auth.currentUser
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