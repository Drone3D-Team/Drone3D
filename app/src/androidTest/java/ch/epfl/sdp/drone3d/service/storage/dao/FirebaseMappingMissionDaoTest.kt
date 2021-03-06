/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.storage.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import ch.epfl.sdp.drone3d.model.mission.MappingMission
import ch.epfl.sdp.drone3d.model.mission.State
import ch.epfl.sdp.drone3d.service.impl.storage.dao.FirebaseMappingMissionDao
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidTest
class FirebaseMappingMissionDaoTest {

    companion object {
        private const val OWNER_ID: String = "Jean-Jean"
        private val MAPPING_MISSION_1: MappingMission = MappingMission()
        private val MAPPING_MISSION_2: MappingMission = MappingMission()
        private const val timeout = 5L
    }

    @get:Rule
    val rule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: FirebaseDatabase

    private lateinit var db: FirebaseMappingMissionDao


    @Before
    fun beforeTests() {
        rule.inject()

        database.goOffline()
        database.reference.removeValue()

        db = FirebaseMappingMissionDao(database)
    }

    @After
    fun afterTests() {
        database.reference.removeValue()

        db = FirebaseMappingMissionDao(database)
    }

    @Test
    fun getPrivateMappingMissionReturnStoreMappingMission() {
        val counter = CountDownLatch(1)

        val mappingMission1 = MAPPING_MISSION_1.copy()

        db.storeMappingMission(OWNER_ID, mappingMission1)
        val live = db.getPrivateMappingMission(OWNER_ID, mappingMission1.privateId!!)
        val observer = Observer<MappingMission> {
            if (it != null) {
                assertThat(mappingMission1.ownerUid, equalTo(OWNER_ID))
                assertThat(mappingMission1.state, equalTo(State.PRIVATE))
                assertThat(mappingMission1.sharedId, nullValue())

                assertThat(it, equalTo(mappingMission1))

                counter.countDown()

            }
        }
        live.observeForever(observer)

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removePrivateMappingMission(OWNER_ID, mappingMission1.privateId!!)
        live.removeObserver(observer)

    }

    @Test
    fun getSharedMappingMissionReturnShareMappingMission() {
        val counter = CountDownLatch(1)

        val mappingMission1 = MAPPING_MISSION_1.copy()

        db.shareMappingMission(OWNER_ID, mappingMission1)
        val live = db.getSharedMappingMission(mappingMission1.sharedId!!)

        val observer = Observer<MappingMission> {
            if (it != null) {
                assertThat(mappingMission1.ownerUid, equalTo(OWNER_ID))
                assertThat(mappingMission1.state, equalTo(State.SHARED))
                assertThat(mappingMission1.privateId, nullValue())

                assertThat(it, equalTo(mappingMission1))

                counter.countDown()
            }
        }
        live.observeForever(observer)


        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removeSharedMappingMission(OWNER_ID, mappingMission1.sharedId!!)
        live.removeObserver(observer)
    }

    @Test
    fun getPrivateMappingMissionsReturnMultipleStoreMappingMission() {
        val counter = CountDownLatch(1)

        val mappingMission1 = MAPPING_MISSION_1.copy()
        val mappingMission2 = MAPPING_MISSION_2.copy()

        db.storeMappingMission(OWNER_ID, mappingMission1)
        db.storeMappingMission(OWNER_ID, mappingMission2)

        val live = db.getPrivateMappingMissions(OWNER_ID)

        val observer = Observer<List<MappingMission>> {
            if (it != null && it.size > 1) {
                assertThat(mappingMission1.ownerUid, equalTo(OWNER_ID))
                assertThat(mappingMission1.state, equalTo(State.PRIVATE))
                assertThat(mappingMission1.sharedId, nullValue())
                assertThat(mappingMission2.ownerUid, equalTo(OWNER_ID))
                assertThat(mappingMission2.state, equalTo(State.PRIVATE))
                assertThat(mappingMission2.sharedId, nullValue())

                assertThat(it.toSet(), equalTo(setOf(mappingMission1, mappingMission2)))
                counter.countDown()

            }
        }
        live.observeForever(observer)


        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removePrivateMappingMission(OWNER_ID, mappingMission1.privateId!!)
        db.removePrivateMappingMission(OWNER_ID, mappingMission2.privateId!!)

        live.removeObserver(observer)
    }

    @Test
    fun getPrivateMappingMissionsReturnEmptyList() {
        val counter = CountDownLatch(1)

        val live = db.getPrivateMappingMissions(OWNER_ID)


        val observer = Observer<List<MappingMission>> {
            assertThat(it, equalTo(listOf()))
            counter.countDown()
        }

        live.observeForever(observer)


        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))
        live.removeObserver(observer)
    }

    @Test
    fun getPrivateMappingMissionsIsUpdatedOnRemove() {
        val counter1 = CountDownLatch(2)
        val counter2 = CountDownLatch(1)

        val observer = Observer<List<MappingMission>> {
            if (it.isEmpty()) {
                counter1.countDown()
            } else {
                if (it.size == 1) {
                    counter2.countDown()
                }
            }
        }
        val live = db.getPrivateMappingMissions(OWNER_ID)
        live.observeForever(observer)

        val mappingMission1 = MAPPING_MISSION_1.copy()
        db.storeMappingMission(OWNER_ID, mappingMission1)
        db.removePrivateMappingMission(OWNER_ID, mappingMission1.privateId!!)



        counter1.await(timeout, TimeUnit.SECONDS)
        counter2.await(timeout, TimeUnit.SECONDS)

        assertThat(counter1.count, equalTo(0L))
        assertThat(counter2.count, equalTo(0L))
        live.removeObserver(observer)
    }

    @Test
    fun getSharedMappingMissionsReturnMultipleShareMappingMission() {
        val counter = CountDownLatch(1)

        val mappingMission1 = MAPPING_MISSION_1.copy()
        val mappingMission2 = MAPPING_MISSION_2.copy()

        db.shareMappingMission(OWNER_ID, mappingMission1)
        db.shareMappingMission(OWNER_ID, mappingMission2)

        val live = db.getSharedMappingMissions()
        val observer = Observer<List<MappingMission>> {
            if (it != null && it.size > 1) {
                assertThat(mappingMission2.ownerUid, equalTo(OWNER_ID))
                assertThat(mappingMission2.state, equalTo(State.SHARED))
                assertThat(mappingMission2.privateId, nullValue())
                assertThat(mappingMission1.ownerUid, equalTo(OWNER_ID))
                assertThat(mappingMission1.state, equalTo(State.SHARED))
                assertThat(mappingMission1.privateId, nullValue())

                assertThat(it.toSet(), equalTo(setOf(mappingMission1, mappingMission2)))
                counter.countDown()

            }
        }
        live.observeForever(observer)


        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removeSharedMappingMission(OWNER_ID, mappingMission1.sharedId!!)
        db.removeSharedMappingMission(OWNER_ID, mappingMission2.sharedId!!)
        live.removeObserver(observer)

    }

    private fun checkStatePRIVATE_SHARED(counter: CountDownLatch, mappingMission1: MappingMission) {

        val livePrivate = db.getPrivateMappingMission(OWNER_ID, mappingMission1.privateId!!)

        livePrivate.observeForever {
            if (it != null) {
                assertThat(mappingMission1.state, equalTo(State.PRIVATE_AND_SHARED))

                assertThat(it, equalTo(mappingMission1))

                counter.countDown()
            }
        }
        val liveShared = db.getSharedMappingMission(mappingMission1.sharedId!!)

        liveShared.observeForever {
            if (it != null) {
                assertThat(mappingMission1.state, equalTo(State.PRIVATE_AND_SHARED))

                assertThat(it, equalTo(mappingMission1))

                counter.countDown()
            }
        }
    }

    @Test
    fun shareMappingMissionUpdateGetPrivateMappingMissionIfAlreadyStored() {
        val counter = CountDownLatch(2)

        val mappingMission1 = MAPPING_MISSION_1.copy()

        assertThat(mappingMission1.state, equalTo(State.NOT_STORED))
        db.storeMappingMission(OWNER_ID, mappingMission1)
        db.shareMappingMission(OWNER_ID, mappingMission1)

        checkStatePRIVATE_SHARED(counter, mappingMission1)
        assertThat(mappingMission1.ownerUid, equalTo(OWNER_ID))


        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removePrivateMappingMission(OWNER_ID, mappingMission1.privateId!!)
        db.removeSharedMappingMission(OWNER_ID, mappingMission1.sharedId!!)
    }

    @Test
    fun storeMappingMissionUpdateGetSharedMappingMissionIfAlreadyShared() {

        val mappingMission1 = MAPPING_MISSION_1.copy()

        val counter = CountDownLatch(2)

        assertThat(mappingMission1.state, equalTo(State.NOT_STORED))
        db.shareMappingMission(OWNER_ID, mappingMission1)
        db.storeMappingMission(OWNER_ID, mappingMission1)

        checkStatePRIVATE_SHARED(counter, mappingMission1)
        assertThat(mappingMission1.ownerUid, equalTo(OWNER_ID))

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removePrivateMappingMission(OWNER_ID, mappingMission1.privateId!!)
        db.removeSharedMappingMission(OWNER_ID, mappingMission1.sharedId!!)
    }

    @Test
    fun testRemovePrivate() {

        val mappingMission1 = MAPPING_MISSION_1.copy()

        val counter = CountDownLatch(2)

        val listener = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                counter.countDown()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val map = snapshot.getValue<MappingMission>()!!


                assertThat(map.state, equalTo(State.PRIVATE))
                assertThat(map.ownerUid, equalTo(OWNER_ID))
                assertThat(map.privateId, equalTo(mappingMission1.privateId))
                assertThat(map.sharedId, nullValue())

                counter.countDown()

            }
        }

        database.getReference("users/$OWNER_ID/mappingMissions/").addChildEventListener(listener)

        db.storeMappingMission(OWNER_ID, mappingMission1)
        db.removePrivateMappingMission(OWNER_ID, mappingMission1.privateId!!)

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        database.getReference("users/$OWNER_ID/mappingMissions/").removeEventListener(listener)

    }

    @Test
    fun testRemovePrivateWhenAlsoShared() {

        val mappingMission1 = MAPPING_MISSION_1.copy()

        val counter = CountDownLatch(4)

        val listenerPrivate = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {


            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val map = snapshot.getValue<MappingMission>()!!

                assertThat(map.state, equalTo(State.PRIVATE))

                counter.countDown()

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val map = snapshot.getValue<MappingMission>()!!
                assertThat(map.state, equalTo(State.PRIVATE_AND_SHARED))
                assertThat(map.ownerUid, equalTo(OWNER_ID))

                counter.countDown()
            }
        }

        val listenerShared = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val map = snapshot.getValue<MappingMission>()!!

                assertThat(map.ownerUid, equalTo(OWNER_ID))
                assertThat(map.state, equalTo(State.PRIVATE_AND_SHARED))

                counter.countDown()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val map = snapshot.getValue<MappingMission>()!!

                assertThat(map.ownerUid, equalTo(OWNER_ID))
                assertThat(map.state, equalTo(State.SHARED))
                assertThat(map.privateId, nullValue())

                counter.countDown()
            }
        }

        database.getReference("users/$OWNER_ID/mappingMissions/")
            .addChildEventListener(listenerPrivate)
        database.getReference("mappingMissions/").addChildEventListener(listenerShared)

        db.storeMappingMission(OWNER_ID, mappingMission1)
        db.shareMappingMission(OWNER_ID, mappingMission1)

        db.removePrivateMappingMission(OWNER_ID, mappingMission1.privateId!!)
        db.removeSharedMappingMission(OWNER_ID, mappingMission1.sharedId!!)


        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        database.getReference("users/$OWNER_ID/mappingMissions/")
            .removeEventListener(listenerPrivate)
        database.getReference("mappingMissions/").removeEventListener(listenerShared)
    }

    @Test
    fun testRemoveShared() {

        val mappingMission1 = MAPPING_MISSION_1.copy()

        val counter = CountDownLatch(2)

        val listener = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                counter.countDown()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                assertThat(mappingMission1.ownerUid, equalTo(OWNER_ID))
                assertThat(mappingMission1.state, equalTo(State.SHARED))
                assertThat(snapshot.getValue<MappingMission>(), equalTo(mappingMission1))

                counter.countDown()
            }
        }

        database.getReference("mappingMissions/").addChildEventListener(listener)

        db.shareMappingMission(OWNER_ID, mappingMission1)
        db.removeSharedMappingMission(OWNER_ID, mappingMission1.sharedId!!)

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        database.getReference("mappingMissions/").removeEventListener(listener)
    }

    @Test
    fun testRemoveSharedWhenAlsoPrivate() {

        val mappingMission1 = MAPPING_MISSION_1.copy()

        val counter = CountDownLatch(4)

        val listenerPrivate = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val map = snapshot.getValue<MappingMission>()!!

                assertThat(map.state, equalTo(State.PRIVATE_AND_SHARED))

                counter.countDown()

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val map = snapshot.getValue<MappingMission>()!!
                assertThat(map.state, equalTo(State.PRIVATE))
                assertThat(map.ownerUid, equalTo(OWNER_ID))

                counter.countDown()
            }
        }

        val listenerShared = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val map = snapshot.getValue<MappingMission>()!!

                assertThat(map.state, equalTo(State.SHARED))
                assertThat(map.name, equalTo(mappingMission1.name))

                counter.countDown()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val map = snapshot.getValue<MappingMission>()!!

                assertThat(map.ownerUid, equalTo(OWNER_ID))
                assertThat(map.state, equalTo(State.PRIVATE_AND_SHARED))

                counter.countDown()
            }
        }

        database.getReference("users/$OWNER_ID/mappingMissions/")
            .addChildEventListener(listenerPrivate)
        database.getReference("mappingMissions/").addChildEventListener(listenerShared)

        db.shareMappingMission(OWNER_ID, mappingMission1)
        db.storeMappingMission(OWNER_ID, mappingMission1)

        db.removeSharedMappingMission(OWNER_ID, mappingMission1.sharedId!!)
        db.removePrivateMappingMission(OWNER_ID, mappingMission1.privateId!!)


        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        database.getReference("users/$OWNER_ID/mappingMissions/")
            .removeEventListener(listenerPrivate)
        database.getReference("mappingMissions/").removeEventListener(listenerShared)
    }

    @Test
    fun testRemoveMappingMissionWithBothPrivateAndShared() {

        val mappingMission1 = MAPPING_MISSION_1.copy()

        val counter = CountDownLatch(4)

        val listenerPrivate = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                counter.countDown()

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val map = snapshot.getValue<MappingMission>()!!
                assertThat(map.state, equalTo(State.PRIVATE_AND_SHARED))
                assertThat(map.ownerUid, equalTo(OWNER_ID))

                counter.countDown()
            }
        }

        val listenerShared = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                counter.countDown()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val map = snapshot.getValue<MappingMission>()!!

                assertThat(map.ownerUid, equalTo(OWNER_ID))
                assertThat(map.state, equalTo(State.PRIVATE_AND_SHARED))

                counter.countDown()
            }
        }

        database.getReference("users/$OWNER_ID/mappingMissions/")
            .addChildEventListener(listenerPrivate)
        database.getReference("mappingMissions/").addChildEventListener(listenerShared)

        db.shareMappingMission(OWNER_ID, mappingMission1)
        db.storeMappingMission(OWNER_ID, mappingMission1)

        db.removeMappingMission(OWNER_ID, mappingMission1.privateId, mappingMission1.sharedId)

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        database.getReference("users/$OWNER_ID/mappingMissions/")
            .removeEventListener(listenerPrivate)
        database.getReference("mappingMissions/").removeEventListener(listenerShared)
    }

    @Test
    fun testRemoveMappingMissionWithOnlyPrivate() {

        val mappingMission1 = MAPPING_MISSION_1.copy()

        val counter = CountDownLatch(2)

        val listenerPrivate = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {


            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                counter.countDown()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val map = snapshot.getValue<MappingMission>()!!
                assertThat(map.state, equalTo(State.PRIVATE))
                assertThat(map.ownerUid, equalTo(OWNER_ID))

                counter.countDown()
            }
        }

        val listenerShared = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}
        }

        database.getReference("users/$OWNER_ID/mappingMissions/")
            .addChildEventListener(listenerPrivate)
        database.getReference("mappingMissions/").addChildEventListener(listenerShared)

        db.storeMappingMission(OWNER_ID, mappingMission1)

        db.removeMappingMission(OWNER_ID, mappingMission1.privateId, mappingMission1.sharedId)

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        database.getReference("users/$OWNER_ID/mappingMissions/")
            .removeEventListener(listenerPrivate)
        database.getReference("mappingMissions/").removeEventListener(listenerShared)
    }

    @Test
    fun testRemoveMappingMissionWithOnlyShared() {

        val mappingMission1 = MAPPING_MISSION_1.copy()

        val counter = CountDownLatch(2)

        val listenerPrivate = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}
        }

        val listenerShared = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                counter.countDown()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val map = snapshot.getValue<MappingMission>()!!

                assertThat(map.ownerUid, equalTo(OWNER_ID))
                assertThat(map.state, equalTo(State.SHARED))

                counter.countDown()
            }
        }

        database.getReference("users/$OWNER_ID/mappingMissions/")
            .addChildEventListener(listenerPrivate)
        database.getReference("mappingMissions/").addChildEventListener(listenerShared)

        db.shareMappingMission(OWNER_ID, mappingMission1)

        db.removeMappingMission(OWNER_ID, mappingMission1.privateId, mappingMission1.sharedId)

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        database.getReference("users/$OWNER_ID/mappingMissions/")
            .removeEventListener(listenerPrivate)
        database.getReference("mappingMissions/").removeEventListener(listenerShared)
    }

    @Test
    fun getPrivateFilteredMappingMissionsReturnsEmptyList() {
        val counter = CountDownLatch(1)

        val mappingMission1 = MAPPING_MISSION_1.copy()
        db.storeMappingMission(OWNER_ID, mappingMission1)

        val live = db.getPrivateFilteredMappingMissions()

        val observer = Observer<List<MappingMission>> {
            assertThat(it, equalTo(listOf()))
            counter.countDown()
        }

        live.observeForever(observer)

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removePrivateMappingMission(OWNER_ID, mappingMission1.privateId!!)
        live.removeObserver(observer)
    }

    @Test
    fun getSharedFilteredMappingMissionsReturnsEmptyList() {
        val counter = CountDownLatch(1)

        val mappingMission1 = MAPPING_MISSION_1.copy()
        db.shareMappingMission(OWNER_ID, mappingMission1)

        val live = db.getSharedFilteredMappingMissions()

        val observer = Observer<List<MappingMission>> {
            assertThat(it, equalTo(listOf()))
            counter.countDown()
        }

        live.observeForever(observer)


        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removeSharedMappingMission(OWNER_ID, mappingMission1.sharedId!!)
        live.removeObserver(observer)
    }

    @Test
    fun updatePrivateFilteredMappingMissionsUpdatesLiveDataWithCorrectMissions() {
        val counter = CountDownLatch(1)

        val mappingMission1 = MappingMission("A_Mission")
        val mappingMission2 = MappingMission("B_Mission")
        db.storeMappingMission(OWNER_ID, mappingMission1)
        db.storeMappingMission(OWNER_ID, mappingMission2)

        val live = db.getPrivateFilteredMappingMissions()
        db.updatePrivateFilteredMappingMissions(OWNER_ID, "A")

        val observer = Observer<List<MappingMission>> {
            if (it != null && it.isNotEmpty()) {
                assertThat(mappingMission1.ownerUid, equalTo(OWNER_ID))
                assertThat(mappingMission1.state, equalTo(State.PRIVATE))
                assertThat(mappingMission1.sharedId, nullValue())

                assertThat(it.toSet(), equalTo(setOf(mappingMission1)))
                counter.countDown()
            }
        }

        live.observeForever(observer)


        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removePrivateMappingMission(OWNER_ID, mappingMission1.privateId!!)
        db.removePrivateMappingMission(OWNER_ID, mappingMission2.privateId!!)
        live.removeObserver(observer)
    }

    @Test
    fun updateSharedFilteredMappingMissionsUpdatesLiveDataWithCorrectMissions() {
        val counter = CountDownLatch(1)

        val mappingMission1 = MappingMission("A_Mission")
        val mappingMission2 = MappingMission("B_Mission")
        db.shareMappingMission(OWNER_ID, mappingMission1)
        db.shareMappingMission(OWNER_ID, mappingMission2)

        val live = db.getSharedFilteredMappingMissions()
        db.updateSharedFilteredMappingMissions("A")

        val observer = Observer<List<MappingMission>> {
            if (it != null && it.isNotEmpty()) {
                assertThat(mappingMission1.ownerUid, equalTo(OWNER_ID))
                assertThat(mappingMission1.state, equalTo(State.SHARED))
                assertThat(mappingMission1.privateId, nullValue())

                assertThat(it.toSet(), equalTo(setOf(mappingMission1)))
                counter.countDown()
            }
        }

        live.observeForever(observer)

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

        db.removeSharedMappingMission(OWNER_ID, mappingMission1.sharedId!!)
        db.removeSharedMappingMission(OWNER_ID, mappingMission2.sharedId!!)
        live.removeObserver(observer)
    }

}