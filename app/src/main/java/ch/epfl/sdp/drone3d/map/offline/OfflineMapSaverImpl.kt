/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.offline

import android.content.Context
import android.util.Log
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.offline.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.CompletableFuture

data class OfflineRegionMetadata(val name:String,val bounds:LatLngBounds,val zoom:Double)

class OfflineMapSaverImpl(val context:Context,val map:MapboxMap):OfflineMapSaver {

    companion object{
        const val JSON_CHARSET = "UTF-8"
        const val MAX_ZOOM = 20.0
        const val TILE_LIMIT = 6000L //6000 tiles corresponds to Greater London with zoom 0-15
        private const val TAG = "OfflineDownload"

        /**
         * Returns the metadata of the provided [region]
         */
        private fun getMetadata(region:OfflineRegion):OfflineRegionMetadata{
            return Json.decodeFromString(String(region.metadata))
        }
    }

    private val offlineManager = OfflineManager.getInstance(context)

    init {
        offlineManager.setOfflineMapboxTileCountLimit(TILE_LIMIT)
    }

    /**
     * Asynchronously downloads the region delimited by [regionBounds], it will be saved with the
     * name [regionName]. [callback] can be used to monitor the download's progress.
     */
    override fun downloadRegion(regionName:String,regionBounds:LatLngBounds,callback:OfflineRegion.OfflineRegionObserver){

        downloadRegion(regionName,regionBounds,object: OfflineManager.CreateOfflineRegionCallback {

            override fun onCreate(offlineRegion: OfflineRegion) {
                offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)

                // Monitor the download progress using setObserver
                offlineRegion.setObserver(callback)
            }

            override fun onError(error: String?) {
                Log.e(TAG,"Error in offline region callback create: $error")
            }
        })
    }

    /**
     * Returns a completable future for the offline region [id]
     */
    override fun getOfflineRegions():CompletableFuture<Array<OfflineRegion>>{
        val futureRegion: CompletableFuture<Array<OfflineRegion>> = CompletableFuture()
        actOnRegions{offlineRegions -> futureRegion.complete(offlineRegions)}
        return futureRegion
    }

    /**
     * Returns the centered camera position of [offlineRegion]
     */
    override fun getRegionLocation(offlineRegion: OfflineRegion): CameraPosition {
        val metadata = getMetadata(offlineRegion)
        return CameraPosition.Builder().target(metadata.bounds.center).zoom(metadata.zoom).build()
    }

    /**
     * Asynchronously deletes the region identified by [id] and calls the callback
     * when it is finished. If the region does not exist, this method does nothing.
     */
    override fun deleteRegion(id:Long,callback: OfflineRegion.OfflineRegionDeleteCallback){
        actOnRegion(id){region -> region?.delete(callback)}
    }

    /**
     * Asynchronously downloads the region delimited by [regionBounds], it will be saved with the
     * name [regionName]. [callback] can be used to act when the region starts downloading and monitor the download's progress.
     */
    private fun downloadRegion(regionName:String,regionBounds:LatLngBounds,callback:OfflineManager.CreateOfflineRegionCallback){
        
        map.getStyle { style ->
            //The regions properties
            val definition = OfflineTilePyramidRegionDefinition(
                style.uri,
                regionBounds,
                map.cameraPosition.zoom,//The minimum zoom is the current camera zoom
                MAX_ZOOM,//Maximum resolution
                context.resources.displayMetrics.density
            )

            //Create metadata for this saved map instance that can be accessed later
            val metadata = OfflineRegionMetadata(regionName, regionBounds,map.cameraPosition.zoom)
            val metadataArray = Json.encodeToString(metadata).toByteArray(charset(JSON_CHARSET))

            //Starts the download (if offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE) is called in the callback)
            //the callback can be used to monitor the download progress
            offlineManager.createOfflineRegion(definition, metadataArray, callback)
        }
    }

    /**
     * Apply [callback] on the region [regionName]
     */
    private fun actOnRegion(id:Long,callback:((OfflineRegion?) -> Unit)){
        actOnRegions{ offlineRegions->
            val region = offlineRegions.firstOrNull{offlineRegion -> offlineRegion.id == id}
            callback(region)
        }
    }

    /**
     * Applies [callback] on the list of OfflineRegions
     */
    private fun actOnRegions(callback: (regions:Array<OfflineRegion>) -> Unit){
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {

            override fun onList(offlineRegions: Array<OfflineRegion>) {
                callback(offlineRegions)
            }

            override fun onError(error: String?) {
                Log.e(TAG,"Error while querying for the list of downloaded regions")
            }
        })
    }



    //For information purposes:

    private val mapBounds: LatLngBounds = LatLngBounds.Builder()
        .include(LatLng(0.0, 0.0))
        .include(LatLng(0.0, 0.0))
        .build()

    val offline_callback = object: OfflineManager.CreateOfflineRegionCallback {
        override fun onCreate(offlineRegion: OfflineRegion) {
            //IMPORTANT: Starts the download
            offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)

            // Monitor the download progress using setObserver
            offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                override fun onStatusChanged(status: OfflineRegionStatus) {

                    // Calculate the download percentage
                    val percentage = if (status.requiredResourceCount >= 0)
                        100.0 * status.completedResourceCount / status.requiredResourceCount else 0.0

                    // Download complete
                    if (status.isComplete) {
                        Log.d(TAG, "Region downloaded successfully.")
                    } else if (status.isRequiredResourceCountPrecise) {
                        Log.d(TAG, "Download percentage is: $percentage")
                    }
                }

                override fun onError(error: OfflineRegionError) {
                    // If an error occurs, print to logcat
                    Log.e(TAG, "Error during offline map download: " + error.message)
                }

                override fun mapboxTileCountLimitExceeded(limit: Long) {
                    // Notify if offline region exceeds maximum tile count:6000 tiles
                    Log.e(TAG, "Mapbox tile count limit exceeded: $limit")
                }
            })
        }

        override fun onError(error: String?) {
            Log.e(TAG,"Error in offline region callback create: $error")
        }

    }
}