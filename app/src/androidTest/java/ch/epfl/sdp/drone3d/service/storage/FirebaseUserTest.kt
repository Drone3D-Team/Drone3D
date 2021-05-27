/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.storage

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import ch.epfl.sdp.drone3d.service.impl.storage.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidTest
class FirebaseUserTest {

    companion object{
        private const val timeout = 5L
    }

    @get:Rule
    val rule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: FirebaseDatabase

    private lateinit var db: FirebaseUser

    @Before
    fun beforeTests() {
        rule.inject()

        database.goOffline()
        database.reference.removeValue()

        db = FirebaseUser(database)
    }

    @Test
    fun loadUserPseudoReturnsExpectedValues() {
        val expectedPseudo = "Masachouette"
        val uid = "123456"
        val counter = CountDownLatch(1)

        database.getReference("users/$uid/pseudo")
            .setValue(expectedPseudo)

        val data = db.loadUserPseudo(uid)
        data.observeForever { pseudo ->
            assertThat(pseudo, equalTo(expectedPseudo))
            counter.countDown()
        }


        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))
    }

    @Test
    fun storeUserPseudoCreateExpectedValueWhenNoPreviousPseudo() {

        val expectedPseudo = "Masachouette"
        val uid = "123456"
        lateinit var pseudo: String
        val counter = CountDownLatch(1)

        val pseudoListener = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val fetchedPseudo = snapshot.getValue<String>()

                if (fetchedPseudo != null && snapshot.key.equals("pseudo")) {
                    pseudo = fetchedPseudo
                    counter.countDown()
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
        }

        database.getReference("users/$uid").addChildEventListener(pseudoListener)

        db.storeUserPseudo(uid, expectedPseudo)

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        assertThat(pseudo, equalTo(expectedPseudo))

        database.getReference("users/$uid").removeEventListener(pseudoListener)
    }

    @Test
    fun storeUserPseudoWriteExpectedValueWhenPreviousPseudo() {
        val expectedPseudo = "Masachouette"
        val uid = "123456"
        lateinit var pseudo: String
        val counter = CountDownLatch(2)

        val pseudoListener = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val fetchedPseudo = snapshot.getValue<String>()

                if (fetchedPseudo != null && snapshot.key.equals("pseudo")) {
                    pseudo = fetchedPseudo
                    counter.countDown()
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                counter.countDown()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
        }

        database.getReference("users/$uid").addChildEventListener(pseudoListener)

        db.storeUserPseudo(uid, "previousPseudo")
        db.storeUserPseudo(uid, expectedPseudo)

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        assertThat(pseudo, equalTo(expectedPseudo))

        database.getReference("users/$uid").removeEventListener(pseudoListener)
    }

    @Test
    fun removeUserPseudoDoesRemoveThePseudo() {
        val expectedPseudo = "Masachouette"
        val uid = "123456"
        val counter = CountDownLatch(2)

        val pseudoListener = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                counter.countDown()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val fetchedPseudo = snapshot.getValue<String>()

                if (fetchedPseudo.equals(expectedPseudo) && snapshot.key.equals("pseudo")) {
                    counter.countDown()
                }
            }
        }
        database.getReference("users/$uid").addChildEventListener(pseudoListener)

        db.storeUserPseudo(uid, expectedPseudo)
        db.removeUserPseudo(uid)

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        database.getReference("users/$uid").removeEventListener(pseudoListener)
    }
}

