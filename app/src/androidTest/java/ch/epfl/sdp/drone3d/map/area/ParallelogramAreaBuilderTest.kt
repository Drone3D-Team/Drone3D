/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */


package ch.epfl.sdp.drone3d.map.area

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone3d.model.util.GeometryUtils
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class ParallelAreaBuilderTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun canAddThreeCorners() {
        val size = 3
        val area = ParallelogramBuilder()
        repeat(size) {
            area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        }
        assertThat(area.vertices.size, equalTo(size))
    }

    @Test
    fun isCompleteWhenThreeCorner() {
        val area = ParallelogramBuilder()
        assertThat(area.isComplete(), equalTo(false))

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), equalTo(false))

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), equalTo(false))

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), equalTo(true))
    }

    @Test
    fun resetIsEffective() {
        val area = ParallelogramBuilder()

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.vertices.size, equalTo(1))
        area.reset()
        assertThat(area.vertices.size, equalTo(0))
    }

    @Test
    fun getAreaSizeIsCorrect() {
        val area = ParallelogramBuilder()

        val a = LatLng(Random.nextDouble(), Random.nextDouble())
        val b = LatLng(Random.nextDouble(), Random.nextDouble())
        val c = LatLng(Random.nextDouble(), Random.nextDouble())

        area.addVertex(a)
        area.addVertex(b)
        area.addVertex(c)

        val distab = a.distanceTo(b)
        val distbc = b.distanceTo(c)
        val diag = a.distanceTo(c)

        val s = (distab + distbc + diag) / 2.0
        
        assertThat(area.getAreaSize(), equalTo(2 * Math.sqrt(s * (s - distab) * (s - distbc) * (s - diag))))
    }
}