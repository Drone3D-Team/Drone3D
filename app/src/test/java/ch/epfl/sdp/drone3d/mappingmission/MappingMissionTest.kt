package ch.epfl.sdp.drone3d.mappingmission

//import org.hamcrest.CoreMatchers.equalTo
import com.google.android.gms.maps.model.LatLng
import junit.framework.TestCase.assertNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test


class MappingMissionTest {

    @Test
    fun equalsReturnTrueIfEqual() {
        val mappingMission1 = MappingMission(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0)))
        val mappingMission2 = MappingMission(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0)))
        assertThat(mappingMission1, equalTo(mappingMission2))
    }

    @Test
    fun equalsReturnFalseIfNotEqual() {
        val mappingMission1 = MappingMission(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0)))
        val mappingMission2 = MappingMission(listOf(LatLng(0.0, 0.1), LatLng(1.0, 1.0)))
        assertNotEquals(mappingMission1, mappingMission2)
    }

    @Test
    fun equalsReturnFalseIfDifferentClass() {
        val mappingMission1 = MappingMission(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0)))
        val integer = 0
        assertNotEquals(mappingMission1, integer)
    }

    @Test
    fun serializationAndDeserializationForTwoCoordinatesMissionIsCorrect() {
        val mappingMission = MappingMission(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0)))
        assertThat(
            MappingMission.deserialize(mappingMission.serialized()),
            equalTo(mappingMission)
        )
    }

    @Test
    fun serializationAndDeserializationForEmptyMissionIsCorrect() {
        val mappingMission = MappingMission(emptyList())
        assertThat(
            MappingMission.deserialize(mappingMission.serialized()),
            equalTo(mappingMission)
        )
    }

    @Test
    fun deserializationForIncorrectStringReturnsNull() {
        val serialized = "{ { }"
        assertNull(MappingMission.deserialize(serialized))
    }

    @Test
    fun toStringOutputsExpectedResult() {
        val mappingMission1 = MappingMission(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0)))
        val expected = "[lat/lng: (0.0,0.0), lat/lng: (1.0,1.0)]"
        assertEquals(expected, mappingMission1.toString())
    }

    @Test
    fun hashcodeAndEqualityCoincide() {
        val mappingMission1 = MappingMission(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0)))
        val mappingMission2 = MappingMission(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0)))
        assertEquals(mappingMission1.hashCode(), mappingMission2.hashCode())
    }


}