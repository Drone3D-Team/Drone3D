package ch.epfl.sdp.drone3d.mappingmission

import com.google.android.gms.maps.model.LatLng
import junit.framework.TestCase.assertNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import org.junit.Test


class MappingMissionTest {
    @Test
    fun serializationAndDeserializationForTwoCoordinatesMissionIsCorrect() {
        val mappingMission = MappingMission(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0)))
        MatcherAssert.assertThat(
            MappingMission.deserialize(mappingMission.serialized()),
            equalTo(mappingMission)
        )
    }

    @Test
    fun serializationAndDeserializationForEmptyMissionIsCorrect() {
        val mappingMission = MappingMission(emptyList())
        MatcherAssert.assertThat(
            MappingMission.deserialize(mappingMission.serialized()),
            equalTo(mappingMission)
        )
    }

    @Test
    fun deserializationForIncorrectStringReturnsNull() {
        val serialized = "{ { }"
        assertNull(MappingMission.deserialize(serialized))
    }
}