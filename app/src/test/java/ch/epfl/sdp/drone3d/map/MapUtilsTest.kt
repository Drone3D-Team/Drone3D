/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map

import ch.epfl.sdp.drone3d.service.storage.data.LatLong
import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MapUtilsTest {
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    @Test
    fun toLatLngWorksWithNullLatLong() {
        assertEquals(null, MapUtils.toLatLng(null))
    }

    @Test
    fun toLatLngWorksWithNullLatOrLong() {
        assertEquals(null, MapUtils.toLatLng(LatLong(null, longitude)))
        assertEquals(null, MapUtils.toLatLng(LatLong(latitude, null)))
    }

    @Test
    fun toLatLngWorks() {
        assertEquals(LatLng(latitude, longitude), MapUtils.toLatLng(LatLong(latitude, longitude)))
    }
}