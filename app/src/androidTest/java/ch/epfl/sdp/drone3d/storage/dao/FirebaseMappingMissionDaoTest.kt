package ch.epfl.sdp.drone3d.storage.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import ch.epfl.sdp.drone3d.storage.data.LatLong
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import ch.epfl.sdp.drone3d.storage.data.State
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import junit.framework.Assert.assertNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FirebaseMappingMissionDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val database = Firebase.database("https://drone3d-6819a-default-rtdb.europe-west1.firebasedatabase.app/")
    private val db = FirebaseMappingMissionDao(database)

    private val timeout = 5L

    private val OWNERID: String = "Jean-Jean"
    private var mappingMission1: MappingMission =  MappingMission("MyMission1", listOf(LatLong(10.2,42.69)))
    private var mappingMission2: MappingMission = MappingMission("MyMission2", listOf(LatLong(0.2,2.9)))

    @Before
    fun beforeTests() {
        database.goOffline()
        database.reference.removeValue()
        mappingMission1 =  MappingMission("MyMission1", listOf(LatLong(10.2,42.69)))
        mappingMission1 = MappingMission("MyMission2", listOf(LatLong(0.2,2.9)))
    }

    @Test
    fun getPrivateMappingMissionReturnStoreMappingMission(){
        val counter = CountDownLatch(1)

        db.storeMappingMission(OWNERID, mappingMission1)
        val live = db.getPrivateMappingMission(OWNERID, mappingMission1.privateId!!)
        live.observeForever {
            if (it != null) {
                assertThat(mappingMission1.ownerUid, equalTo(OWNERID))
                assertThat(mappingMission1.state, equalTo(State.PRIVATE))
                assertNull(mappingMission1.sharedId)

                assertThat(it, equalTo(mappingMission1))

                counter.countDown()

            }
        }

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removePrivateMappingMission(OWNERID, mappingMission1.privateId!!)
    }

    @Test
    fun getSharedMappingMissionReturnShareMappingMission(){
        val counter = CountDownLatch(1)

        db.shareMappingMission(OWNERID, mappingMission1)
        val live = db.getSharedMappingMission(OWNERID, mappingMission1.sharedId!!)
        live.observeForever {
            if (it != null) {
                assertThat(mappingMission1.ownerUid, equalTo(OWNERID))
                assertThat(mappingMission1.state, equalTo(State.SHARED))
                assertNull(mappingMission1.privateId)

                assertThat(it, equalTo(mappingMission1))

                counter.countDown()
            }
        }

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removeSharedMappingMission(OWNERID, mappingMission1.sharedId!!)
    }

    @Test
    fun getPrivateMappingMissionsReturnMultipleStoreMappingMission(){
        val counter = CountDownLatch(1)

        db.storeMappingMission(OWNERID, mappingMission1)
        db.storeMappingMission(OWNERID, mappingMission2)

        val live = db.getPrivateMappingMissions(OWNERID)
        live.observeForever {
            if (it != null && it.size > 1) {
                assertThat(mappingMission1.ownerUid, equalTo(OWNERID))
                assertThat(mappingMission1.state, equalTo(State.PRIVATE))
                assertNull(mappingMission1.sharedId)
                assertThat(mappingMission2.ownerUid, equalTo(OWNERID))
                assertThat(mappingMission2.state, equalTo(State.PRIVATE))
                assertNull(mappingMission2.sharedId)

                assertThat(it.toSet(), equalTo(setOf(mappingMission1, mappingMission2)))
                counter.countDown()

            }
        }

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removePrivateMappingMission(OWNERID, mappingMission1.privateId!!)
        db.removePrivateMappingMission(OWNERID, mappingMission2.privateId!!)

    }

    @Test
    fun getSharedMappingMissionsReturnMultipleShareMappingMission(){
        val counter = CountDownLatch(1)

        db.shareMappingMission(OWNERID, mappingMission1)
        db.shareMappingMission(OWNERID, mappingMission2)

        val live = db.getSharedMappingMissions()
        live.observeForever {
            if (it != null && it.size > 1) {
                assertThat(mappingMission2.ownerUid, equalTo(OWNERID))
                assertThat(mappingMission2.state, equalTo(State.SHARED))
                assertNull(mappingMission2.privateId)
                assertThat(mappingMission1.ownerUid, equalTo(OWNERID))
                assertThat(mappingMission1.state, equalTo(State.SHARED))
                assertNull(mappingMission1.privateId)

                assertThat(it.toSet(), equalTo(setOf(mappingMission1, mappingMission2)))
                counter.countDown()

            }
        }

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removeSharedMappingMission(OWNERID, mappingMission1.sharedId!!)
        db.removeSharedMappingMission(OWNERID, mappingMission2.sharedId!!)
    }

    private fun checkStatePRIVATE_SHARED(counter: CountDownLatch){
        val livePrivate = db.getPrivateMappingMission(OWNERID, mappingMission1.privateId!!)

        livePrivate.observeForever {
            if (it != null) {
                assertThat(mappingMission1.state, equalTo(State.PRIVATE_AND_SHARED))

                assertThat(it, equalTo(mappingMission1))

                counter.countDown()
            }
        }
        val liveShared = db.getSharedMappingMission(OWNERID, mappingMission1.sharedId!!)

        liveShared.observeForever {
            if (it != null) {
                assertThat(mappingMission1.state, equalTo(State.PRIVATE_AND_SHARED))

                assertThat(it, equalTo(mappingMission1))

                counter.countDown()
            }
        }
    }

    @Test
    fun shareMappingMissionUpdateGetPrivateMappingMissionIfAlreadyStored(){
        val counter = CountDownLatch(2)

        assertThat(mappingMission1.state, equalTo(State.RAM))
        db.storeMappingMission(OWNERID, mappingMission1)
        db.shareMappingMission(OWNERID, mappingMission1)

        checkStatePRIVATE_SHARED(counter)
        assertThat(mappingMission1.ownerUid, equalTo(OWNERID))


        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removePrivateMappingMission(OWNERID, mappingMission1.privateId!!)
        db.removeSharedMappingMission(OWNERID, mappingMission1.sharedId!!)
    }

    @Test
    fun storeMappingMissionUpdateGetSharedMappingMissionIfAlreadyShared(){
        val counter = CountDownLatch(2)

        assertThat(mappingMission1.state, equalTo(State.RAM))
        db.shareMappingMission(OWNERID, mappingMission1)
        db.storeMappingMission(OWNERID, mappingMission1)

        checkStatePRIVATE_SHARED(counter)
        assertThat(mappingMission1.ownerUid, equalTo(OWNERID))

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removePrivateMappingMission(OWNERID, mappingMission1.privateId!!)
        db.removeSharedMappingMission(OWNERID, mappingMission1.sharedId!!)
    }
}