package ch.epfl.sdp.drone3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.storage.data.LatLong
import ch.epfl.sdp.drone3d.storage.data.MappingMission
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions

/**
 * The activity that allows the user to create itinerary using a map.
 */
class ItineraryCreateActivity : AppCompatActivity() {
    private var mapView: MapView? = null

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
                val style = mapboxMap.style!!
                // Map is set up and the style has loaded. Now we can add data or make other map adjustments
                showMission(MappingMission("EPFL", listOf(LatLong(46.52061, 6.56794), LatLong(46.52304, 6.56358))), mapboxMap, mapView!!, style)

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

    private fun showMission(mission: MappingMission, mapboxMap: MapboxMap, mapView: MapView, style:Style) {

        val symbolManager = SymbolManager(mapView, mapboxMap, style)

        if(mission.flightPath.isNotEmpty()){
            val coordinates: List<LatLng?> = mission.flightPath.map { pos: LatLong ->
                if(pos.latitude != null && pos.longitude != null){
                    LatLng(pos.latitude, pos.longitude)
                }
                else{
                    null
                }
            }

            val coordinatesNotNull: List<LatLng> = coordinates.filterNotNull()

            coordinatesNotNull.forEachIndexed{index: Int, latlng: LatLng ->
                symbolManager.create(SymbolOptions()
                        .withLatLng(LatLng(latlng))
                        .withTextField(index.toString()
                                ))
            }

            /*
            val symbols = coordinatesNotNull.mapIndexed{index: Int, latlng: LatLng ->
                 symbolManager.create(SymbolOptions()
                         .withLatLng(LatLng(latlng))
                         .withTextField(index.toString()))
            }
            */
        }
    }
}