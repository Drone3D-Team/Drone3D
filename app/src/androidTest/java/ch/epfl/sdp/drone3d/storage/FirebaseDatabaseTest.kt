package ch.epfl.sdp.drone3d.storage

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class FirebaseDatabaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val database = Firebase.database("https://drone3d-6819a-default-rtdb.europe-west1.firebasedatabase.app/")
    private val timeout = 5L

    @Before
    fun beforeTests() {
        database.goOffline()
        database.reference.removeValue()
    }

    @Test
    fun loadUserPseudoReturnsExpectedValues() {
        val db = FirebaseDatabase()
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
        val db = FirebaseDatabase()
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
        val db = FirebaseDatabase()
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
        val db = FirebaseDatabase()
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

