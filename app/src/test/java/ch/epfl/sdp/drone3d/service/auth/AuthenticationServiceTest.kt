/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.auth

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*

/**
 * Test for the authentication service
 */
class AuthenticationServiceTest {

    private val mail: String = "mail@domain.com"
    private val password: String = "password"

    @Test
    fun testCurrentUser() {
        val auth = mock(FirebaseAuth::class.java)
        val service = FirebaseAuthenticationService(auth)

        val user = mock(FirebaseUser::class.java)
        `when`(auth.currentUser).thenReturn(user)

        Assert.assertTrue(service.hasActiveSession())
        Assert.assertNotNull(service.getCurrentSession())
        Assert.assertEquals(user, service.getCurrentSession()?.user)
    }

    @Test
    fun testFunctionCalls() {
        val auth = mock(FirebaseAuth::class.java)
        val service = FirebaseAuthenticationService(auth)

        `when`(auth.createUserWithEmailAndPassword(anyString(), anyString()))
            .thenReturn(Tasks.forResult(null))
        `when`(auth.signInWithEmailAndPassword(anyString(), anyString()))
            .thenReturn(Tasks.forResult(null))
        `when`(auth.sendPasswordResetEmail(anyString())).thenReturn(Tasks.forResult(null))

        service.register(mail, password)
        verify(auth).createUserWithEmailAndPassword(mail, password)

        service.login(mail, password)
        verify(auth).signInWithEmailAndPassword(mail, password)

        service.sendPasswordReset(mail)
        verify(auth).sendPasswordResetEmail(mail)

        service.signOut()
        verify(auth).signOut()
    }
}