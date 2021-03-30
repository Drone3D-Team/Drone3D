package ch.epfl.sdp.drone3d.service.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

/**
 * Service taking care of the authentication part of the system
 */
interface AuthenticationService {

    /**
     * Returns true if a user is currently logged in
     */
    fun hasActiveSession(): Boolean

    /**
     * Get the current user session
     *
     * Can be null if no users are logged in
     */
    fun getCurrentSession(): UserSession?

    /**
     * Register a new user with an [email] and a [password]
     * The registered user is automatically logged in upon success
     *
     * @return a task that will be complete once the server responds. It may fail.
     */
    fun register(email: String, password: String): Task<AuthResult>

    /**
     * Logs a user in with an [email] and a [password]
     *
     * @return a task that will be complete once the server responds. It may fail.
     */
    fun login(email: String, password: String): Task<AuthResult>

    /**
     * Sends a password reset email to the provided [email] if it has already been registered
     */
    fun sendPasswordReset(email: String): Task<Void>

    /**
     * Sign the current user out
     */
    fun signOut()
}
