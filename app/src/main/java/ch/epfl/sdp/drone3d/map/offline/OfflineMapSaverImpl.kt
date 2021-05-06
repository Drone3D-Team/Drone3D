/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.offline

import android.content.Context
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.offline.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.concurrent.CompletableFuture

/**
 * Stores the metadata associated to a downloaded region
 */
data class OfflineRegionMetadata(val name:String, val bounds:LatLngBounds, val zoom:Double)


/**
 * Used internally to facilitate the serialization of the OfflineRegionMetadata data class
 */
@Serializable
private data class SerializableRegionMetadata(val name:String, val latSouth:Double, val lonWest:Double,
                                              val latNorth:Double, val lonEast:Double, val zoom:Double)

class OfflineMapSaverImpl(val context:Context,val map:MapboxMap):OfflineMapSaver {

    companion object{
        private const val JSON_CHARSET = "UTF-8"
        const val MAX_ZOOM = 20.0
        const val TILE_LIMIT = 6000L //6000 tiles corresponds to Greater London with zoom 0-15

        /**
         * Serialize the metadata of the provided [region]
         */
        fun serializeMetadata(metadata: OfflineRegionMetadata):ByteArray{
            val internalMetadata = SerializableRegionMetadata(metadata.name,metadata.bounds.latSouth,
                metadata.bounds.lonWest,metadata.bounds.latNorth,metadata.bounds.lonEast,metadata.zoom)

            return Json.encodeToString(internalMetadata).toByteArray(charset(JSON_CHARSET))
        }

        /**
         * Deserialize the metadata [serialized]
         */
         fun deserializeMetadata(serialized: ByteArray):OfflineRegionMetadata{
            val metadata:SerializableRegionMetadata = Json.decodeFromString(String(serialized))
            val bounds = LatLngBounds.Builder()
                            .include(LatLng(metadata.latSouth, metadata.lonWest))
                            .include(LatLng(metadata.latNorth, metadata.lonEast))
                            .build()

            return OfflineRegionMetadata(metadata.name,bounds,metadata.zoom)
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
                Timber.e("Error in offline region callback create: $error")
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
        val metadata = deserializeMetadata(offlineRegion.metadata)
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
            //The region's properties
            val definition = OfflineTilePyramidRegionDefinition(
                style.uri,
                regionBounds,
                map.cameraPosition.zoom,//The minimum zoom is the current camera zoom
                MAX_ZOOM,//Maximum resolution
                context.resources.displayMetrics.density
            )

            //Create metadata for this saved map instance that can be accessed later
            val metadata = OfflineRegionMetadata(regionName, regionBounds,map.cameraPosition.zoom)
            val metadataArray = serializeMetadata(metadata)

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
                Timber.e("Error while querying for the list of downloaded regions")
            }
        })
    }
}