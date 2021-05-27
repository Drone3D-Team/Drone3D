/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.model.mission

import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test

class ParallelogramTest {

    @Test
    fun secondaryConstructorReturnsExpected() {
        val expected = Parallelogram(Point(1.0, 1.0), Vector(1.0, 1.0), Vector(1.0, 0.0))
        val actual = Parallelogram(Point(1.0, 1.0), Point(2.0, 2.0), Point(2.0, 1.0))
        assertEquals(actual, expected)
    }

    @Test
    fun translationReturnsExpected() {
        val expected = Parallelogram(Point(1.0, 1.0), Vector(1.0, 1.0), Vector(1.0, 0.0))
        val actual = Parallelogram(Point(0.0, 0.0), Vector(1.0, 1.0), Vector(1.0, 0.0)).translate(Vector(1.0, 1.0))
        assertEquals(actual, expected)
    }

    @Test
    fun translationByZeroVectorIsIdentity() {
        val expected = Parallelogram(Point(1.0, 1.0), Vector(1.0, 1.0), Vector(1.0, 0.0))
        val actual = Parallelogram(Point(1.0, 1.0), Vector(1.0, 1.0), Vector(1.0, 0.0)).translate(Vector(0.0, 0.0))
        assertEquals(actual, expected)
    }

    @Test
    fun diagonalEquivalentReturnsExpected() {
        val expected = Parallelogram(Point(1.0, 1.0), Vector(0.0, -1.0), Vector(-1.0, 0.0))
        val actual = Parallelogram(Point(0.0, 0.0), Vector(1.0, 0.0), Vector(0.0, 1.0)).diagonalEquivalent()
        assertEquals(actual.origin.x, expected.origin.x, 0.0001)
        assertEquals(actual.origin.y, expected.origin.y, 0.0001)
        assertEquals(actual.dir1Span.x, expected.dir1Span.x, 0.0001)
        assertEquals(actual.dir1Span.y, expected.dir1Span.y, 0.0001)
        assertEquals(actual.dir2Span.x, expected.dir2Span.x, 0.0001)
        assertEquals(actual.dir2Span.y, expected.dir2Span.y, 0.0001)
    }

    @Test
    fun applyingTwiceDiagonalEquivalentIsIdentity() {
        val expected = Parallelogram(Point(1.0, 1.0), Vector(1.0, 1.0), Vector(1.0, 0.0))
        val actual = expected.diagonalEquivalent().diagonalEquivalent()
        assertEquals(actual, expected)
    }

    @Test
    fun getVerticesReturnsExpected() {
        val expected = listOf(Point(0.0, 0.0), Point(1.0, 0.0), Point(1.0, 1.0), Point(0.0, 1.0))
        val actual = Parallelogram(Point(0.0, 0.0), Vector(1.0, 0.0), Vector(0.0, 1.0)).getVertices()
        assertEquals(actual, expected)
    }

    @Test
    fun getClosestEquivalentReturnsExpected() {
        val expected1 = Parallelogram(Point(1.0, 1.0), Vector(-1.0, 0.0), Vector(0.0, -1.0))
        val expected2 = Parallelogram(Point(1.0, 1.0), Vector(0.0, -1.0), Vector(-1.0, 0.0))
        val actual = Parallelogram(Point(0.0, 0.0), Vector(1.0, 0.0), Vector(0.0, 1.0)).getClosestEquivalentParallelogram(
            Point(
                5.0,
                5.0
            )
        )
        assertThat(actual, anyOf(equalTo(expected1), equalTo(expected2)))
    }

    @Test
    fun getFourthPointReturnsExpected() {
        val expected = Point(1.0, 1.0)
        val actual = Parallelogram.getFourthPoint(Point(0.0, 0.0), Point(1.0, 0.0), Point(0.0, 1.0))

        assertEquals(actual, expected)
    }
}