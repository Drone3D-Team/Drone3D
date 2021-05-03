package ch.epfl.sdp.drone3d.ui.map

import android.os.Bundle
import ch.epfl.sdp.drone3d.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.Style

class ManageOfflineMapActivity : BaseMapActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        super.initMapView(savedInstanceState, R.layout.activity_manage_offline_map, R.id.mapView)


        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now we can add data or make other map adjustments
            }
        }

        //Create a "back button" in the action bar up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}