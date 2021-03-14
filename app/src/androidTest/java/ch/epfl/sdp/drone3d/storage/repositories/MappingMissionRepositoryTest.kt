package ch.epfl.sdp.drone3d.storage.repositories

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.storage.dao.MappingMissionDao
import ch.epfl.sdp.drone3d.storage.data.LatLong
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import ch.epfl.sdp.drone3d.storage.repositories.MappingMissionRepository
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class MappingMissionRepositoryTest {

    private val testOwnerUid = "123456"
    private val testPrivateId = "privateId"
    private val testPublicId = "privateId"
    private val testMission = MappingMission("Lausanne", listOf(LatLong(0.0,0.0),LatLong(1.0,1.0)))

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()//Makes all actions synchronous

    @Test
    fun getPrivateMappingMissionCallsGetPrivateMappingMissionOfDao(){
        val dao = Mockito.mock(MappingMissionDao::class.java)
        val expectedData = MutableLiveData<MappingMission>()
        expectedData.value = testMission
        Mockito.`when`(dao.getPrivateMappingMission(testOwnerUid,testPrivateId)).thenReturn(expectedData)

        MappingMissionRepository.daoProvider = { dao }
        val repo =
            MappingMissionRepository()

        assertThat(repo.getPrivateMappingMission(testOwnerUid,testPrivateId),
            CoreMatchers.equalTo(expectedData as LiveData<MappingMission>)
        )
        Mockito.verify(dao, Mockito.times(1)).getPrivateMappingMission(testOwnerUid,testPrivateId)
    }


}