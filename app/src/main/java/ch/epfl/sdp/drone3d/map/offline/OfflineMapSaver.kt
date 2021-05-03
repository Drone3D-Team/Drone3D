package ch.epfl.sdp.drone3d.map.offline

import android.content.Context
import android.util.Log
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject


//TODO: pbm with kotlin imports
//@Serializable
data class OfflineRegionMetadata(val name:String)

class OfflineMapSaver (val context:Context,val map:MapboxMap) {

    companion object{
        const val JSON_CHARSET = "UTF-8"
        const val MAX_ZOOM = 20.0
        private const val TAG = "OfflineDownload"
    }

    private val offlineManager = OfflineManager.getInstance(context)

    /**
     * Asynchronously downloads the region delimited by [regionBounds], it will be saved with the
     * name [regionName], [callback] can be used to monitor the download's progress
     */
    fun downloadRegion(regionName:String,regionBounds:LatLngBounds,callback:OfflineManager.CreateOfflineRegionCallback){

        val offlineManager = OfflineManager.getInstance(context)
        map.getStyle { style ->
            //The regions properties
            val definition = OfflineTilePyramidRegionDefinition(
                style.uri,
                regionBounds,
                map.cameraPosition.zoom,//The minimum zoom is the current camera zoom
                MAX_ZOOM,//Maximum resolution
                context.resources.displayMetrics.density
            )

            //Create metadata for this saved map instance that can be used later
            val metadata = OfflineRegionMetadata(regionName)
            val metadataArray = Json.encodeToString(metadata).toByteArray(charset(JSON_CHARSET))

            //Starts the download, the callback can be used to monitor the download progress
            offlineManager.createOfflineRegion(definition, metadataArray, callback)
        }
    }

    /**
     * Asynchronously deletes the first downloaded region with [regionName] and calls the callback
     * when it is finished
     * Throws no such element exception if no elements with [regionName] where found
     */
    fun deleteRegion(regionName:String,callback: OfflineRegion.OfflineRegionDeleteCallback){
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {

            override fun onList(offlineRegions: Array<OfflineRegion>) {
                val regionToDelete = offlineRegions.first { offlineRegion -> getName(offlineRegion)==regionName}
                regionToDelete.delete(callback)
            }

            override fun onError(error: String?) {
                Log.e(TAG,"Error while querying for the list of downloaded regions")
            }
        })
    }

    private fun getName(region:OfflineRegion):String{
        val regionMetadata = Json.decodeFromString<OfflineRegionMetadata>(String(region.metadata))
        return regionMetadata.name
    }


    //For information purposes:

    private val mapBounds: LatLngBounds = LatLngBounds.Builder()
        .include(LatLng(0.0, 0.0))
        .include(LatLng(0.0, 0.0))
        .build()

    val offline_callback = object: OfflineManager.CreateOfflineRegionCallback {
        override fun onCreate(offlineRegion: OfflineRegion) {
            //Indicate that the region is currently downloading
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
                    // Notify if offline region exceeds maximum tile count
                    Log.e(TAG, "Mapbox tile count limit exceeded: $limit")
                }
            })
        }

        override fun onError(error: String?) {
            Log.e(TAG,"Error in oflline region callback create: $error")
        }

    }
}