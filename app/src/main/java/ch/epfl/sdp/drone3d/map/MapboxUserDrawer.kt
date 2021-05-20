/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map

import android.graphics.Color
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Circle
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions
import com.mapbox.mapboxsdk.utils.ColorUtils

/**
 * This class draw the location of the home of the drone on the map
 *
 * This class is taken from the Fly2Find project with one adaptation :
 * - the SymbolManager was replaced by a CircleManager, and the modifications implied by this change
 */
class MapboxUserDrawer(mapView: MapView, mapboxMap: MapboxMap, style: Style) : MapboxDrawer {
    private var circleManager = CircleManager(mapView, mapboxMap, style)
    private lateinit var marker: Circle
    private var reset: Boolean = true

    /**
     * Draws the [location] of the home on the map
     */
    fun showUser(location: LatLng?) {
        when {
            location == null -> {
                circleManager.deleteAll()
                reset = true
            }
            reset -> {
                val circleOptions = CircleOptions()
                    .withLatLng(location)
                    .withCircleColor(ColorUtils.colorToRgbaString(Color.BLUE))
                marker = circleManager.create(circleOptions)
                reset = false
            }
            else -> {
                marker.latLng = location
                circleManager.update(marker)
            }
        }
    }

    override fun onDestroy() {
        circleManager.deleteAll()
        circleManager.onDestroy()
    }
}