/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.offline

import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineMapSaverImplTest {

    companion object{
        val rolexName = "Rolex"
        val rolexBounds =  LatLngBounds.Builder()
                            .include(LatLng(46.517804, 6.567261))
                            .include(LatLng(46.518907783162064, 6.569428157806775))
                            .build()
        val defaultZoom = 5.0
    }

    @Test
    fun serializerAndDeserializerActAsIdentity(){
        val expectedOfflineMetadata = OfflineRegionMetadata(rolexName, rolexBounds,0,defaultZoom)
        val obtainedOfflineMetadata = OfflineMapSaverImpl.deserializeMetadata(OfflineMapSaverImpl.serializeMetadata(expectedOfflineMetadata))
        assertEquals(expectedOfflineMetadata,obtainedOfflineMetadata)
    }
}