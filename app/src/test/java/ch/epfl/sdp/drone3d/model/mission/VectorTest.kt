package ch.epfl.sdp.drone3d.model.mission

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sqrt

class VectorTest {

    @Test
    fun secondaryConstructorReturnsExpected() {
        val expected = Vector(1.0, -2.0)
        val actual = Vector(Point(2.0, 2.0), Point(3.0, 0.0))
        assertEquals(expected, actual)
    }

    @Test
    fun unaryMinusReturnsExpected() {
        val expected = Vector(-1.0, 1.0)
        val actual = -Vector(1.0, -1.0)
        assertEquals(expected, actual)
    }

    @Test
    fun plusReturnsExpected() {
        val expected = Vector(0.0, 1.0)
        val actual = Vector(1.0, -1.0) + Vector(-1.0, 2.0)
        assertEquals(expected, actual)
    }

    @Test
    fun minusReturnsExpected() {
        val expected = Vector(0.0, 1.0)
        val actual = Vector(1.0, -1.0) - Vector(1.0, -2.0)
        assertEquals(expected, actual)
    }

    @Test
    fun timesReturnsExpected() {
        val expected = Vector(2.0, -2.0)
        val actual = Vector(1.0, -1.0) * 2.0
        assertEquals(expected, actual)
    }

    @Test
    fun divReturnsExpected() {
        val expected = Vector(1.0, -1.0)
        val actual = Vector(2.0, -2.0) / 2.0
        assertEquals(expected, actual)
    }

    @Test
    fun normReturnsExpected() {
        val expected1 = sqrt(8.0)
        val actual1 = Vector(2.0, -2.0).norm()
        assertEquals(expected1, actual1, 0.0001)

        val expected2 = 1.0
        val actual2 = Vector(sqrt(2.0) / 2, -sqrt(2.0) / 2).norm()
        assertEquals(expected2, actual2, 0.0001)
    }

    @Test
    fun normalizedReturnsExpected() {
        val expected1 = Vector(1.0, 0.0)
        val actual1 = Vector(7.0, 0.0).normalized()
        assertEquals(expected1.x, actual1.x, 0.0001)
        assertEquals(expected1.y, actual1.y, 0.0001)

        val expected2 = Vector(sqrt(2.0) / 2.0, sqrt(2.0) / 2.0)
        val actual2 = Vector(1.0, 1.0).normalized()
        assertEquals(expected2.x, actual2.x, 0.0001)
        assertEquals(expected2.y, actual2.y, 0.0001)
    }

    @Test
    fun normalizedVectorHasNorm1() {
        assertEquals(1.0, Vector(2.0, 3.0).normalized().norm(), 0.0001)
        assertEquals(1.0, Vector(6.0, 7.0).normalized().norm(), 0.0001)
    }

    @Test
    fun reversedReturnsExpected() {
        val expected = Vector(1.0, -1.0)
        val actual = Vector(-1.0, 1.0).reverse()
        assertEquals(expected, actual)
    }
}
