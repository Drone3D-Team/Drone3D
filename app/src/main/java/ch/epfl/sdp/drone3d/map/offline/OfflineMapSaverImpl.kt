/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map.offline

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber


/**
 * Used internally to facilitate the serialization of the OfflineRegionMetadata data class
 */
@Serializable
private data class SerializableRegionMetadata(val name:String, val latSouth:Double, val lonWest:Double,
                                              val latNorth:Double, val lonEast:Double,val tileCount:Long, val zoom:Double)

class OfflineMapSaverImpl(val context:Context,val style: Style):OfflineMapSaver {

    companion object{
        private const val JSON_CHARSET = "UTF-8"
        const val MAX_ZOOM = 20.0
        const val TILE_LIMIT = 6000L //6000 tiles corresponds to Greater London with zoom 0-15
        private val totalTileCount = MutableLiveData<Long>(0)
        private val offlineRegions = MutableLiveData<Array<OfflineRegion>>()

        /**
         * Serialize the metadata of the provided [region]
         */
        fun serializeMetadata(metadata: OfflineRegionMetadata):ByteArray{
            val internalMetadata = SerializableRegionMetadata(metadata.name,metadata.bounds.latSouth,
                metadata.bounds.lonWest,metadata.bounds.latNorth,metadata.bounds.lonEast,metadata.tileCount,metadata.zoom)

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

            return OfflineRegionMetadata(metadata.name,bounds,metadata.tileCount,metadata.zoom)
        }

        /**
         * Returns the metadata of the region
         */
        fun getMetadata(region:OfflineRegion):OfflineRegionMetadata{
            return deserializeMetadata(region.metadata)
        }

        /**
         * Returns the centered camera position on [offlineRegion]
         */
        fun getRegionLocation(offlineRegion: OfflineRegion): CameraPosition {
            val metadata = getMetadata(offlineRegion)
            return CameraPosition.Builder().target(metadata.bounds.center).zoom(metadata.zoom).build()
        }
    }

    private val offlineManager = OfflineManager.getInstance(context)

    init {
        offlineManager.setOfflineMapboxTileCountLimit(TILE_LIMIT)
        refreshOfflineRegions()
    }

    override fun downloadRegion(regionName:String,regionBounds:LatLngBounds,zoom:Double,callback:OfflineRegion.OfflineRegionObserver){

        downloadRegion(regionName,regionBounds,zoom,object: OfflineManager.CreateOfflineRegionCallback {

            override fun onCreate(offlineRegion: OfflineRegion) {
                offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)

                // Monitor the download progress using setObserver
                offlineRegion.setObserver(callback)

                //Used to update the metadata tile count at the end
                offlineRegion.setObserver(object:OfflineRegion.OfflineRegionObserver{
                    override fun onStatusChanged(status: OfflineRegionStatus) {
                       if(status.isComplete){
                           val tileCount = status.completedTileCount
                           val oldMetadata = getMetadata(offlineRegion)
                           val newMetadata = OfflineRegionMetadata(oldMetadata.name,oldMetadata.bounds,tileCount,oldMetadata.zoom)
                           offlineRegion.updateMetadata(serializeMetadata(newMetadata),object:OfflineRegion.OfflineRegionUpdateMetadataCallback{
                               override fun onUpdate(metadata: ByteArray?) {}
                               override fun onError(error: String?) {
                                   Timber.e("Could not update metadata")
                               }
                           })
                           refreshOfflineRegions()
                       }
                    }
                    override fun onError(error: OfflineRegionError) {}
                    override fun mapboxTileCountLimitExceeded(limit: Long) {}
                })
            }

            override fun onError(error: String?) {
                Timber.e("Error in offline region callback create: $error")
            }
        })
    }

    override fun getOfflineRegions():LiveData<Array<OfflineRegion>>{
        return offlineRegions
    }


    override fun deleteRegion(id:Long,callback: OfflineRegion.OfflineRegionDeleteCallback){
        actOnRegion(id){region ->
            region?.delete(callback)
            refreshOfflineRegions()
        }
    }

    override fun getTotalTileCount(): MutableLiveData<Long> {
        return totalTileCount
    }

    override fun getMaxTileCount(): Long {
        return TILE_LIMIT
    }

    /**
     * Asynchronously downloads the region delimited by [regionBounds], it will be saved with the
     * name [regionName]. [callback] can be used to act when the region starts downloading and monitor the download's progress.
     */
    private fun downloadRegion(regionName:String,regionBounds:LatLngBounds,zoom: Double,callback:OfflineManager.CreateOfflineRegionCallback){

            //The region's properties
            val definition = OfflineTilePyramidRegionDefinition(
                style.uri,
                regionBounds,
                zoom,//The minimum zoom is the current camera zoom
                MAX_ZOOM,//Maximum resolution
                context.resources.displayMetrics.density
            )

            //Create metadata for this saved map instance that can be accessed later, tile count is
            //unknown at this tine and is initialized to 0
            val metadata = OfflineRegionMetadata(regionName, regionBounds,0,zoom)

            //Starts the download (if offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE) is called in the callback)
            //the callback can be used to monitor the download progress
            offlineManager.createOfflineRegion(definition, serializeMetadata(metadata), callback)

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
     * Apply [callback] on the list of OfflineRegions
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

    /**
     * Refreshes the list of offline regions by querying the offlineManager
     */
    private fun refreshOfflineRegions(){
        actOnRegions {
            regions ->
            offlineRegions.value = regions
            totalTileCount.value = offlineRegions.value?.map{region->getMetadata(region).tileCount}?.fold(0L){acc,count->acc+count}
        }
    }
}