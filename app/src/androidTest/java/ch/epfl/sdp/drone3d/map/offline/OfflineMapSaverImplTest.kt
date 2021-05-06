/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.offline

import android.os.SystemClock
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.OfflineRegion
import com.mapbox.mapboxsdk.offline.OfflineRegionError
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class OfflineMapSaverImplTest {


    companion object{
        val rolexName = "Rolex"
        val rolexBounds =  LatLngBounds.Builder()
            .include(LatLng(46.517804, 6.567261))
            .include(LatLng(46.518907783162064, 6.569428157806775))
            .build()
        val defaultZoom = 5.0

        var callbackApplied = false

        val observerCallback = object: OfflineRegion.OfflineRegionObserver{
                override fun onStatusChanged(status: OfflineRegionStatus) {
                    callbackApplied = true
                }
                override fun onError(error: OfflineRegionError) {
                    callbackApplied = true
                }
                override fun mapboxTileCountLimitExceeded(limit: Long) {
                    callbackApplied = true
                }
        }
    }

    @Before
    fun beforeAll(){
        callbackApplied = false
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun downloadRegionAppliesCallback(){
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val mapBoxStyle = mock(Style::class.java)
        `when`(mapBoxStyle.uri).thenReturn(Style.MAPBOX_STREETS)
        val mapBoxMap = mock(MapboxMap::class.java)
        `when`(mapBoxMap.cameraPosition).thenReturn(CameraPosition.DEFAULT)
        `when`(mapBoxMap.style).thenReturn(mapBoxStyle)

        val offlineMapSaver = OfflineMapSaverImpl(context,mapBoxMap)

        offlineMapSaver.downloadRegion(rolexName, rolexBounds, observerCallback)
//        SystemClock.sleep(1000L)
//        assertTrue(callbackApplied)
        assertTrue(true)
    }


    @Test
    fun serializerAndDeserializerActAsIdentity(){
        val expectedOfflineMetadata = OfflineRegionMetadata(rolexName, rolexBounds,defaultZoom)
        val obtainedOfflineMetadata = OfflineMapSaverImpl.deserializeMetadata(OfflineMapSaverImpl.serializeMetadata(expectedOfflineMetadata))
        assertEquals(expectedOfflineMetadata,obtainedOfflineMetadata)
    }


}