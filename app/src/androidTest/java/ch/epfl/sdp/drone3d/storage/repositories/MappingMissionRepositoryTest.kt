package ch.epfl.sdp.drone3d.storage.repositories

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.storage.data.LatLong
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class MappingMissionRepositoryTest {

    private val testOwnerUid = "123456"
    private val testPrivateId = "privateId"
    private val testSharedId = "privateId"
    private val testMission =
        MappingMission("Lausanne", listOf(LatLong(0.0, 0.0), LatLong(1.0, 1.0)))

    private var dao = Mockito.mock(MappingMissionDao::class.java)
    private var repo = MappingMissionRepository()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()//Makes all actions synchronous

    @Before
    fun beforeTests() {
        dao = Mockito.mock(MappingMissionDao::class.java)
        MappingMissionRepository.daoProvider = { dao }
        repo = MappingMissionRepository()
    }

    @Test
    fun getPrivateMappingMissionCallsGetPrivateMappingMissionOfDao() {

        val expectedData = MutableLiveData<MappingMission>()
        expectedData.value = testMission
        Mockito.`when`(dao.getPrivateMappingMission(testOwnerUid, testPrivateId))
            .thenReturn(expectedData)

        assertThat(
            repo.getPrivateMappingMission(testOwnerUid, testPrivateId),
            equalTo(expectedData as LiveData<MappingMission>)
        )
        Mockito.verify(dao, Mockito.times(1)).getPrivateMappingMission(testOwnerUid, testPrivateId)
    }

    @Test
    fun getSharedMappingMissionCallsGetSharedMappingMissionOfDao() {

        val expectedData = MutableLiveData<MappingMission>()
        expectedData.value = testMission
        Mockito.`when`(dao.getSharedMappingMission(testOwnerUid, testSharedId))
            .thenReturn(expectedData)

        assertThat(
            repo.getSharedMappingMission(testOwnerUid, testSharedId),
            equalTo(expectedData as LiveData<MappingMission>)
        )
        Mockito.verify(dao, Mockito.times(1)).getSharedMappingMission(testOwnerUid, testSharedId)
    }

    @Test
    fun getPrivateMappingMissionsCallsGetPrivateMappingMissionsOfDao() {

        val expectedData = MutableLiveData<List<MappingMission>>()
        expectedData.value = listOf(testMission)
        Mockito.`when`(dao.getPrivateMappingMissions(testOwnerUid)).thenReturn(expectedData)

        assertThat(
            repo.getPrivateMappingMissions(testOwnerUid),
            equalTo(expectedData as LiveData<List<MappingMission>>)
        )
        Mockito.verify(dao, Mockito.times(1)).getPrivateMappingMissions(testOwnerUid)
    }

    @Test
    fun getSharedMappingMissionsCallsGetSharedMappingMissionsOfDao() {

        val expectedData = MutableLiveData<List<MappingMission>>()
        expectedData.value = listOf(testMission)
        Mockito.`when`(dao.getSharedMappingMissions()).thenReturn(expectedData)

        assertThat(
            repo.getSharedMappingMissions(),
            equalTo(expectedData as LiveData<List<MappingMission>>)
        )
        Mockito.verify(dao, Mockito.times(1)).getSharedMappingMissions()
    }

    @Test
    fun storeMappingMissionsCallsStoreMappingMissionsOfDao() {
        repo.storeMappingMission(testOwnerUid, testMission)
        Mockito.verify(dao, Mockito.times(1)).storeMappingMission(testOwnerUid, testMission)
    }

    @Test
    fun shareMappingMissionsCallsShareMappingMissionsOfDao() {
        repo.shareMappingMission(testOwnerUid, testMission)
        Mockito.verify(dao, Mockito.times(1)).shareMappingMission(testOwnerUid, testMission)
    }

    @Test
    fun removePrivateMappingMissionsCallsRemovePrivateMappingMissionsOfDao() {
        repo.removePrivateMappingMission(testOwnerUid, testPrivateId)
        Mockito.verify(dao, Mockito.times(1))
            .removePrivateMappingMission(testOwnerUid, testPrivateId)
    }

    @Test
    fun removeSharedMappingMissionsCallsRemoveSharedMappingMissionsOfDao() {
        repo.removeSharedMappingMission(testOwnerUid, testSharedId)
        Mockito.verify(dao, Mockito.times(1)).removeSharedMappingMission(testOwnerUid, testSharedId)
    }

    @Test
    fun removeMappingMissionsCallsRemovedMappingMissionsOfDao() {
        repo.removeMappingMission(testOwnerUid, testPrivateId, testSharedId)
        Mockito.verify(dao, Mockito.times(1))
            .removeMappingMission(testOwnerUid, testPrivateId, testSharedId)
    }
}