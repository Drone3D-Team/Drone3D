package ch.epfl.sdp.drone3d.auth

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Test for the authentication service
 */
class AuthenticationServiceTest {

    private val MAIL: String = "mail@domain.com"
    private val PASSWORD: String = "password"

    @Test
    fun testCurrentUser() {
        val auth = mock(FirebaseAuth::class.java)
        val service = AuthenticationServiceImpl(auth)

        val user = mock(FirebaseUser::class.java)
        `when`(auth.currentUser).thenReturn(user)

        assertTrue(service.hasActiveSession())
        assertNotNull(service.getCurrentSession())
        assertEquals(user, service.getCurrentSession()?.user)
    }

    @Test
    fun testFunctionCalls() {
        val auth = mock(FirebaseAuth::class.java)
        val service = AuthenticationServiceImpl(auth)

        `when`(auth.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(Tasks.forResult(null))
        `when`(auth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(Tasks.forResult(null))
        `when`(auth.sendPasswordResetEmail(anyString())).thenReturn(Tasks.forResult(null))

        service.register(MAIL, PASSWORD)
        verify(auth).createUserWithEmailAndPassword(MAIL, PASSWORD)

        service.login(MAIL, PASSWORD)
        verify(auth).signInWithEmailAndPassword(MAIL, PASSWORD)

        service.sendPasswordReset(MAIL)
        verify(auth).sendPasswordResetEmail(MAIL)

        service.signOut()
        verify(auth).signOut()
    }
}