package ch.epfl.sdp.drone3d.model.mission

import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class SphereToPlaneProjectorTest {

    private val LATITUDE_ERROR_1_DEGREE = 0.0000001
    private val LONGITUDE_ERROR_1_DEGREE = 0.00002


    @Test
    fun originIsMappedToZeroZeroAsPoint(){
        val origin = LatLng(70.0,30.0)
        val projector = SphereToPlaneProjector(origin)
        val originPoint = projector.toPoint(origin)
        val expected = Point(0.0,0.0)

        assertEquals(expected, originPoint)
    }

    @Test
    fun pointZeroZeroIsMappedToOriginalOriginLatlng(){
        val originLatLng = LatLng(70.0,30.0)
        val projector = SphereToPlaneProjector(originLatLng)

        val point = Point(0.0,0.0)
        val expected = projector.toLatLng(point)

        assertEquals(expected, originLatLng)

    }

    @Test
    fun transformingInPointAndBackInLatlngGiveCloseResultToOriginalCloseToPole(){

        val input = LatLng(89.0, 179.00)

        val origin = LatLng(88.0,178.0)
        val projector = SphereToPlaneProjector(origin)
        val originPoint = projector.toPoint(input)
        val output = projector.toLatLng(originPoint)

        assertEquals(input.latitude, output.latitude, LATITUDE_ERROR_1_DEGREE)
        assertEquals(input.longitude, output.longitude, LONGITUDE_ERROR_1_DEGREE)

    }

    @Test
    fun toLatlngsGiveSameResultAsToLatlngOnEachPointIndividually(){

        val point1 = Point(3.0,6.0)
        val point2 = Point(5.0,1.0)
        val point3 = Point(8.0,30.0)

        val points = listOf<Point>(point1, point2, point3)

        val origin = LatLng(70.0,30.0)
        val projector = SphereToPlaneProjector(origin)

        val latLng1 = projector.toLatLng(point1)
        val latLng2 = projector.toLatLng(point2)
        val latLng3 = projector.toLatLng(point3)

        val expected = listOf<LatLng>(latLng1, latLng2, latLng3)

        val actual = projector.toLatLngs(points)

        assertEquals(expected, actual)
    }

    @Test
    fun toPointsGiveSameResultAsToPointOnEachLatlngIndividually(){

        val latLng1 = LatLng(69.0, 29.0)
        val latLng2 = LatLng(70.0, 30.0)
        val latLng3 = LatLng(71.0, 31.0)

        val latlngs = listOf<LatLng>(latLng1, latLng2, latLng3)

        val origin = LatLng(70.0,30.0)
        val projector = SphereToPlaneProjector(origin)

        val point1 = projector.toPoint(latLng1)
        val point2 = projector.toPoint(latLng2)
        val point3 = projector.toPoint(latLng3)

        val expected = listOf<Point>(point1, point2, point3)

        val actual = projector.toPoints(latlngs)

        assertEquals(expected, actual)
    }

    @Test
    fun smallerLatlngThanOriginGiveNegativPoints(){

        val origin = LatLng(70.0,30.0)
        val projector = SphereToPlaneProjector(origin)

        val test = LatLng(69.0, 29.0)
        val resultPoint = projector.toPoint(test)

        assertTrue(resultPoint.x<0.0)
        assertTrue(resultPoint.y<0.0)
    }

    @Test
    fun toLatLongBeyond180LongitudeWrapAroundWorld(){

        val origin = LatLng(0.0,179.0)
        val projector = SphereToPlaneProjector(origin)

        val point = Point(120000.0, 0.0)
        val resultLatLng = projector.toLatLng(point)

        val expected = -origin.longitude

        assertEquals(expected, resultLatLng.longitude, 1.0)

        val origin2 = LatLng(0.0, -179.0)
        val projector2 = SphereToPlaneProjector(origin2)

        val point2 = Point(-120000.0, 0.0)
        val resultLatLng2 = projector2.toLatLng(point2)

        val expected2 = -origin2.longitude

        assertEquals(expected2, resultLatLng2.longitude, 1.0)
    }

    @Test
    fun toLatLngBeyond90LatitudeWrapAroundWorld(){

        val origin = LatLng(89.0,0.0)
        val projector = SphereToPlaneProjector(origin)

        val point = Point(0.0, 120000.0)
        val resultLatLng = projector.toLatLng(point)

        val expectedLongitude = origin.longitude+180
        val expectedLatitude = origin.latitude

        assertEquals(expectedLongitude, resultLatLng.longitude, 0.000000000001)
        assertEquals(expectedLatitude, resultLatLng.latitude, 1.0)

        val origin2 = LatLng(-89.0,0.0)
        val projector2 = SphereToPlaneProjector(origin2)

        val point2 = Point(0.0, -120000.0)
        val resultLatLng2 = projector2.toLatLng(point2)

        val expectedLongitude2 = origin2.longitude+180
        val expectedLatitude2 = origin2.latitude

        assertEquals(expectedLongitude2, resultLatLng2.longitude, 0.000000000001)
        assertEquals(expectedLatitude2, resultLatLng2.latitude, 1.0)
    }


}