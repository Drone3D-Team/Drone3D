package ch.epfl.sdp.drone3d.ui.map.offline

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.map.offline.OfflineMapSaver
import ch.epfl.sdp.drone3d.map.offline.OfflineMapSaverImpl
import ch.epfl.sdp.drone3d.ui.ToastHandler
import ch.epfl.sdp.drone3d.ui.map.BaseMapActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.OfflineRegion
import com.mapbox.mapboxsdk.offline.OfflineRegionError
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus
import timber.log.Timber
import java.lang.StringBuilder
import java.lang.System.currentTimeMillis
import kotlin.math.min

class ManageOfflineMapActivity : BaseMapActivity(), OnMapReadyCallback {

    companion object{
        private const val DOWNLOAD_STATUS_TIME_DELAY = 1000
    }

    private lateinit var offlineMapSaver: OfflineMapSaver
    private lateinit var mapboxMap: MapboxMap
    private lateinit var downloadButton: FloatingActionButton
    private var timeOfLastDownloadToast = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        super.initMapView(savedInstanceState, R.layout.activity_manage_offline_map, R.id.mapView)

        mapView.contentDescription = getString(R.string.map_not_ready)
        mapView.getMapAsync(this)

        downloadButton = findViewById(R.id.buttonToSaveOfflineMap)
        //We must wait for the map to be available to download
        downloadButton.isEnabled = false


        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        // Used to detect when the map is ready in tests
        mapView.contentDescription = getString(R.string.map_ready)

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->

            offlineMapSaver = OfflineMapSaverImpl(this@ManageOfflineMapActivity, style.uri)
            bindOfflineRegionsToRecycler()
            downloadButton.isEnabled = true
            bindTileCount()
        }
    }

    fun downloadOfflineMap(@Suppress("UNUSED_PARAMETER") view:View){
        val bounds = mapboxMap.projection.visibleRegion.latLngBounds
        val zoom = mapboxMap.cameraPosition.zoom
        val regionName = "TO_REPLACE"

        offlineMapSaver.downloadRegion(regionName,bounds,zoom,object:OfflineRegion.OfflineRegionObserver{
            override fun onStatusChanged(status: OfflineRegionStatus) {

                if(status.isComplete){
                    ToastHandler.showToast(applicationContext, getString(R.string.download_succeeded,regionName))
                }
                else if(currentTimeMillis()-timeOfLastDownloadToast>DOWNLOAD_STATUS_TIME_DELAY){
                    timeOfLastDownloadToast = currentTimeMillis()
                    val percentage = if (status.requiredResourceCount >= 0) 100.0 * status.completedResourceCount/status.requiredResourceCount else 0.0
                    ToastHandler.showToast(applicationContext, getString(R.string.download_progress,"%.2f".format(percentage)+"%"))
                }
            }

            override fun onError(error: OfflineRegionError) {
                Timber.e("DownloadError $error")
            }

            override fun mapboxTileCountLimitExceeded(limit: Long) {
                Timber.e("mapboxTileCountLimitExceeded")
                ToastHandler.showToast(applicationContext, R.string.tile_limit_exceeded)
            }
        })
    }
    /**
     * Get the recyclerView, create an adapter and bind it to the offlineRegions by displaying them.
     */
    private fun bindOfflineRegionsToRecycler() {
        val savedRegionsRecycler = findViewById<RecyclerView>(R.id.saved_regions)
        val offlineRegions = offlineMapSaver.getOfflineRegions()
        val adapter = OfflineRegionViewAdapter(offlineMapSaver)
        savedRegionsRecycler.adapter = adapter

        offlineRegions.observe(this, androidx.lifecycle.Observer {
            it.let {
                adapter.submitList(it.sortedWith(Comparator<OfflineRegion> { r0, r1 ->
                    OfflineMapSaverImpl.getMetadata(r0).name.compareTo(OfflineMapSaverImpl.getMetadata(r1).name)}))
            }
        })

        offlineRegions.observe(this, {
            it.forEach {offlineRegion -> (display(offlineRegion))}
        })
    }

    /**
    * Add observer to the tileCount so that the progress bar and the text are updated on change.
    */
    private fun bindTileCount(){
        val tilesUsedTextView = findViewById<TextView>(R.id.tiles_used)
        val tilesBar = findViewById<ProgressBar>(R.id.tile_count_bar)

        val maxTileCount = offlineMapSaver.getMaxTileCount()
        val actualTileCount = offlineMapSaver.getTotalTileCount()

        tilesBar.max = maxTileCount.toInt()

        actualTileCount.observe(this, {
            it.let{
                val builder = StringBuilder()
                builder.append(min(it, maxTileCount)).append("/").append(maxTileCount)
                tilesUsedTextView.text = builder.toString()

                tilesBar.setProgress(min(it, maxTileCount).toInt(), true)
            }
        })
    }

    /**
     * Display the [offlineRegion] on the map by putting a square surrounding the region on the map
     */
    private fun display(offlineRegion: OfflineRegion){
        //TODO("Implement")
    }

}
