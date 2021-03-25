package ch.epfl.sdp.drone3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.storage.data.LatLong
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*

/**
 * The activity that allows the user to create itinerary using a map.
 */
class ItineraryCreateActivity : AppCompatActivity() {
    private val ZOOM_VALUE = 14.0

    private var mapView: MapView? = null
    private var symbols: List<Symbol> = mutableListOf()
    private var lines: List<Line> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_itinerary_create)

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now we can add data or make other map adjustments
                showMission(MappingMission("EPFL", listOf(LatLong(), LatLong(46.52061, 6.56794), LatLong(), LatLong(46.52304, 6.56358), LatLong(46.53304, 6.56958), LatLong(46.52061, 6.56794))), mapboxMap, mapView!!)

            }

        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    /**
     * Show a MappingMission [mission] on the map [mapboxMap] with view [mapView].
     */
    private fun showMission(mission: MappingMission, mapboxMap: MapboxMap, mapView: MapView) {
        val style = mapboxMap.style!!

        val symbolManager = SymbolManager(mapView, mapboxMap, style)
        val lineManager = LineManager(mapView, mapboxMap, style)

        val flightPathNotNull = mission.flightPath.filter { latLong -> latLong.latitude!=null && latLong.longitude!=null }

        if(flightPathNotNull.isNotEmpty()){
            val coordinates: List<LatLng> = flightPathNotNull.map { pos: LatLong ->
                LatLng(pos.latitude!!, pos.longitude!!)
            }

            val mapOptions = coordinates.mapIndexed(){index: Int, latlng: LatLng ->
                symbolManager.create(SymbolOptions()
                        .withLatLng(LatLng(latlng))
                        .withTextField(index.toString()))
            }

            this.symbols = this.symbols + mapOptions

            val lineOptions = LineOptions()
                    .withLatLngs(coordinates)

            val mapLines = lineManager.create(lineOptions)

            this.lines = this.lines + mapLines

            zoomOnMission(mission, mapboxMap)
        }
    }

    /**
     * Zoom on the first step of a mission [mission] on the map [mapboxMap].
     */
    private fun zoomOnMission(mission: MappingMission, mapboxMap: MapboxMap){

        val flightPathNotNull = mission.flightPath.filter { latLong -> latLong.latitude!=null && latLong.longitude!=null }

        if(flightPathNotNull.isNotEmpty()){

            val firstCoordinates = LatLng(flightPathNotNull[0].latitude!!, flightPathNotNull[0].longitude!!)

            mapboxMap.cameraPosition =  CameraPosition.Builder()
                    .target(firstCoordinates)
                    .zoom(ZOOM_VALUE)
                    .build()
        }
    }

}