/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.offline

import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.offline.OfflineRegion
import java.util.concurrent.CompletableFuture

interface OfflineMapSaver {

    /**
     * Asynchronously downloads the region delimited by [regionBounds], it will be saved with the
     * name [regionName]. The [callback] can be used to monitor the download's progress.
     */
    fun downloadRegion(regionName:String, regionBounds: LatLngBounds, callback: OfflineRegion.OfflineRegionObserver)

    /**
     * Returns a future for the offline region [id]
     */
    fun getOfflineRegions(): CompletableFuture<Array<OfflineRegion>>

    /**
     * Returns the centered camera position on [offlineRegion]
     */
    fun getRegionLocation(offlineRegion: OfflineRegion): CameraPosition

    /**
     * Returns a mutable live data that will be updated with the current tile count
     */
    fun getTotalTileCount():MutableLiveData<Long>

    /**
     * Asynchronously deletes the region identified by [id] and calls [callback]
     * when it is finished. If the region does not exist, this method does nothing.
     */
    fun deleteRegion(id:Long,callback: OfflineRegion.OfflineRegionDeleteCallback)
}