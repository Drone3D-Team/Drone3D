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
                var mapmission = MappingMission("EPFL", listOf(LatLong(), LatLong(46.52061, 6.56794), LatLong(), LatLong(46.52304, 6.56358), LatLong(46.53304, 6.56958), LatLong(46.52061, 6.56794)))
                val missionDrawer = MapboxMissionDrawer(mapView!!,mapboxMap, mapboxMap.style!!)
                missionDrawer.showMission(mapmission)
                MapboxUtility.zoomOnMission(mapmission, mapboxMap)
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

}