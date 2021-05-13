/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.offline

import androidx.lifecycle.LiveData
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineRegion

/**
 * Represents a class that offers the possibility to save a mapping region for later offline use.
 */
interface OfflineMapSaver {

    /**
     * Asynchronously downloads the region delimited by [regionBounds], it will be saved with the
     * name [regionName]. The [callback] can be used to monitor the download's progress.
     */
    fun downloadRegion(regionName:String, regionBounds: LatLngBounds,zoom:Double, callback: OfflineRegion.OfflineRegionObserver)

    /**
     * Asynchronously deletes the region identified by [id] and calls [callback]
     * when it is finished. If the region does not exist, this method does nothing.
     */
    fun deleteRegion(id:Long,callback: OfflineRegion.OfflineRegionDeleteCallback)

    /**
     * Returns a live data for the offline region [id]
     */
    fun getOfflineRegions(): LiveData<Array<OfflineRegion>>

    /**
     * Returns a mutable live data that will be updated with the current tile count
     */
    fun getTotalTileCount():LiveData<Long>

    /**
     * Returns the maximum number of tiles a user can store
     * A tile can be seen as a unit of storage for a region on the map
     */
    fun getMaxTileCount():Long
}