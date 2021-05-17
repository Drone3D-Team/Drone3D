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
import ch.epfl.sdp.drone3d.R
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

class OfflineMapSaverImplTest {

    companion object{
        const val rolexName = "Rolex"
        val rolexBounds: LatLngBounds =  LatLngBounds.Builder()
                            .include(LatLng(46.517804, 6.567261))
                            .include(LatLng(46.518907783162064, 6.569428157806775))
                            .build()

        const val defaultZoom = 14.0
        const val style = Style.MAPBOX_STREETS
        const val TIMEOUT = 10L
        const val DOUBLE_PRECISION = 0.0001
        const val MAP_API_KEY = "pk.eyJ1IjoiZDNkIiwiYSI6ImNrbTRrc244djA1bGkydXRwbGphajZkbHAifQ.T_Ygz9WvhOHjPiOpZEJ8Zw"

        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        lateinit var offlineSaver:OfflineMapSaver
    }

    init {
        UiThreadStatement.runOnUiThread {
            Mapbox.getInstance(context, MAP_API_KEY)
            offlineSaver = OfflineMapSaverImpl(context,style)
        }
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun beforeAll(){
        clearOfflineMapDatabase()
    }

    private fun clearOfflineMapDatabase() {
        val liveRegions = offlineSaver.getOfflineRegions()

        //Wait for live regions to be updated
        SystemClock.sleep(1000L)

        val regions:Array<OfflineRegion> = liveRegions.value!!
        val counterRegionsDelete = CountDownLatch(regions!!.size)

        regions!!.forEach {region->
            offlineSaver.deleteRegion(region.id,object:OfflineRegion.OfflineRegionDeleteCallback{
                override fun onDelete() {
                    counterRegionsDelete.countDown()
                }
                override fun onError(error: String?) {}
            })
        }

        assert(counterRegionsDelete.await(TIMEOUT, TimeUnit.SECONDS))
    }

    /**
     * Downloads the regions and waits for completion of download
     */
    private fun downloadRegionSync(regionName:String, bounds:LatLngBounds, zoom:Double){
        val counter = CountDownLatch(1)

        offlineSaver.downloadRegion(regionName, bounds, zoom,object: OfflineRegion.OfflineRegionObserver {
            override fun onStatusChanged(status: OfflineRegionStatus) {
                if(status.isComplete){
                    counter.countDown()
                    Log.e("Test","Downloaded a region")
                }
            }
            override fun onError(error: OfflineRegionError?) {}

            override fun mapboxTileCountLimitExceeded(limit: Long) {}
        })

        assert(counter.await(TIMEOUT,TimeUnit.SECONDS))
    }

    private fun deleteRegionAsync(regionId:Long){
        offlineSaver.deleteRegion(regionId,object :OfflineRegion.OfflineRegionDeleteCallback{
            override fun onDelete() {}
            override fun onError(error: String?) {}
        })
    }

    @Test
    fun downloadAddsCorrectRegion(){
        downloadRegionSync(rolexName,rolexBounds,defaultZoom)
        val regions = offlineSaver.getOfflineRegions()

        val counter = CountDownLatch(1)

        regions.observeForever { regions ->
            if (regions.isNotEmpty()) {
                val metadata = OfflineMapSaverImpl.getMetadata(regions.first())
                assertEquals(rolexName,metadata.name)
                assertEquals(rolexBounds,metadata.bounds)
                assertEquals(defaultZoom,metadata.zoom, DOUBLE_PRECISION)
                counter.countDown()
                deleteRegionAsync(regions.first().id)
            }
        }

        assert(counter.await(TIMEOUT,TimeUnit.SECONDS))
    }

    @Test
    fun deleteRegionsRemovesRegion(){
        downloadRegionSync(rolexName, rolexBounds, defaultZoom)

        val counterLiveDataInit = CountDownLatch(1)
        val liveRegions = offlineSaver.getOfflineRegions()
        var regions: Array<OfflineRegion>? = null
        liveRegions.observeForever {
            if (it.isNotEmpty()) {
                regions = it
                counterLiveDataInit.countDown()
            }
        }

        //Wait until live data is init
        assert(counterLiveDataInit.await(TIMEOUT, TimeUnit.SECONDS))

        val counterDeleteComplete = CountDownLatch(1)
        offlineSaver.deleteRegion(regions!!.first().id,
            object : OfflineRegion.OfflineRegionDeleteCallback {
                override fun onDelete() {
                    counterDeleteComplete.countDown()
                }

                override fun onError(error: String?) {}
            })

        assert(counterDeleteComplete.await(TIMEOUT, TimeUnit.SECONDS))
    }

    @Test
    fun regionsAreEmptyAtBeginning(){
        val counter = CountDownLatch(1)
        val regions = offlineSaver.getOfflineRegions()

        regions.observeForever { offlineRegions ->
            assert(offlineRegions.isEmpty())
            counter.countDown()
        }

        assert(counter.await(TIMEOUT, TimeUnit.SECONDS))
    }

    @Test
    fun afterDownloadTotalTileCountIsNotZero(){
        val counter = CountDownLatch(1)
        downloadRegionSync(rolexName, rolexBounds, defaultZoom)

        offlineSaver.getTotalTileCount().observeForever { tileCount ->
            if (tileCount != 0L) {
                counter.countDown()
            }
        }
        assert(counter.await(TIMEOUT, TimeUnit.SECONDS))
    }


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