/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.offline

import android.app.Instrumentation
import android.util.Log
import com.mapbox.mapboxsdk.offline.OfflineRegion
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.drone3d.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.OfflineRegionError
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OfflineMapSaverImplTest {

    companion object{
        val rolexName = "Rolex"
        val rolexBounds =  LatLngBounds.Builder()
                            .include(LatLng(46.517804, 6.567261))
                            .include(LatLng(46.518907783162064, 6.569428157806775))
                            .build()
        val defaultZoom = 5.0
        val style = Style.MAPBOX_STREETS
        val TAG = "OfflineMapSaverImplTest"
        val API_KEY = "pk.eyJ1IjoiZDNkIiwiYSI6ImNrbTRrc244djA1bGkydXRwbGphajZkbHAifQ.T_Ygz9WvhOHjPiOpZEJ8Zw"
    }

    @Before
    fun beforeAll(){

    }

    /*
    @Test
    fun testDownload(){
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        Mapbox.getInstance(context,API_KEY)


        val offlineSaver = OfflineMapSaverImpl(context,style)

//        offlineSaver.downloadRegion(rolexName, rolexBounds, defaultZoom,object: OfflineRegion.OfflineRegionObserver{
//            override fun onStatusChanged(status: OfflineRegionStatus?) {
//                Log.e(TAG, "Status changed ")
//            }
//
//            override fun onError(error: OfflineRegionError?) {
//                Log.e(TAG, "Error")
//            }
//
//            override fun mapboxTileCountLimitExceeded(limit: Long) {
//                Log.e(TAG, "MapBoxCount exceeded")
//            }
//
//        })

    }

     */

    @Test
    fun serializerAndDeserializerActAsIdentity(){
        val expectedOfflineMetadata = OfflineRegionMetadata(rolexName, rolexBounds,0,defaultZoom, true)
        val obtainedOfflineMetadata = OfflineMapSaverImpl.deserializeMetadata(OfflineMapSaverImpl.serializeMetadata(expectedOfflineMetadata))
        assertEquals(expectedOfflineMetadata,obtainedOfflineMetadata)
    }
}