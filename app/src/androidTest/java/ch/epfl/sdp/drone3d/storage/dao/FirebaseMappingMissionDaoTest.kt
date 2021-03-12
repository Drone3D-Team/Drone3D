package ch.epfl.sdp.drone3d.storage.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import ch.epfl.sdp.drone3d.storage.data.LatLong
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
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
    private val timeout = 5L

    private val OWNERID: String = "Jean-Jean"
    private val MAPPING_MISSION: MappingMission = MappingMission("MyMission", listOf(LatLong(10.2,42.69)))

    @Before
    fun beforeTests() {
        database.goOffline()
        database.reference.removeValue()
    }

    @Test
    fun getPrivateMappingMissionReturnStoreMappingMission(){
        val db = FirebaseMappingMissionDao(database)
        val counter = CountDownLatch(1)

        db.storeMappingMission(OWNERID, MAPPING_MISSION)
        //counter.await(timeout, TimeUnit.SECONDS)
        val live = db.getPrivateMappingMission(OWNERID, MAPPING_MISSION.privateId!!)
        live.observeForever {
            if (it != null) {
                counter.countDown()
                assertThat(MAPPING_MISSION, equalTo(it))
           }
        }

        counter.await(timeout, TimeUnit.SECONDS)
        assertThat(counter.count, equalTo(0L))

    }
}