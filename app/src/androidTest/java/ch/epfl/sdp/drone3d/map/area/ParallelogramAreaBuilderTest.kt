/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */


package ch.epfl.sdp.drone3d.map.area

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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

    companion object{
        private val AREA = listOf<LatLng>(LatLng(46.518732896473644, 6.5628454889064365), LatLng(46.51874120200868, 6.563415458311842), LatLng(46.518398828344715, 6.563442280401509))
    }


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
    fun getSizeWork() {
        val area = ParallelogramBuilder()

        area.addVertex(AREA[0])
        area.addVertex(AREA[1])
        area.addVertex(AREA[2])

        assertThat(area.getAreaSize(), equalTo(1664.119287956441))
    }
}