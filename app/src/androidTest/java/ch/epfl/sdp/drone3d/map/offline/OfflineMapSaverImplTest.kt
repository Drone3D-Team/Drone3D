/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.offline

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import com.mapbox.mapboxsdk.offline.OfflineRegion
import androidx.test.platform.app.InstrumentationRegistry
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.OfflineRegionError
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class OfflineMapSaverImplTest {

    companion object{
        const val rolexName = "Rolex"
        val rolexBounds: LatLngBounds =  LatLngBounds.Builder()
                            .include(LatLng(46.517804, 6.567261))
                            .include(LatLng(46.518907783162064, 6.569428157806775))
                            .build()

        const val defaultZoom = 14.0
        const val style = Style.MAPBOX_STREETS
        const val API_KEY = "pk.eyJ1IjoiZDNkIiwiYSI6ImNrbTRrc244djA1bGkydXRwbGphajZkbHAifQ.T_Ygz9WvhOHjPiOpZEJ8Zw"
        const val TIMEOUT = 10L
        const val DOUBLE_PRECISION = 0.0001

        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        lateinit var offlineSaver:OfflineMapSaver
    }

    init {
        UiThreadStatement.runOnUiThread {
            Mapbox.getInstance(context,API_KEY)
            offlineSaver = OfflineMapSaverImpl(context,style)
        }
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    @Test
    fun getMaxTileCountReturnsTileLimit(){
        assertEquals(offlineSaver.getMaxTileCount(), OfflineMapSaverImpl.TILE_LIMIT)
    }

    @Test
    fun serializerAndDeserializerActAsIdentity(){
        val expectedOfflineMetadata = OfflineRegionMetadata(rolexName, rolexBounds,0,defaultZoom, true)
        val obtainedOfflineMetadata = OfflineMapSaverImpl.deserializeMetadata(OfflineMapSaverImpl.serializeMetadata(expectedOfflineMetadata))
        assertEquals(expectedOfflineMetadata,obtainedOfflineMetadata)
    }
}