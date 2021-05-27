/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map

import ch.epfl.sdp.drone3d.R
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions


/**
 * This class draw the location of the home of the drone on the map
 *
 * This class is taken from the Fly2Find project with one adaptation :
 * - the SymbolManager was replaced by a CircleManager, and the modifications implied by this change
 */
class MapboxHomeDrawer(mapView: MapView, mapboxMap: MapboxMap, style: Style) : MapboxDrawer {
    private var symbolManager = SymbolManager(mapView, mapboxMap, style)
    private lateinit var marker: Symbol
    private var reset: Boolean = true

    /**
     * Draws the [location] of the home on the map
     */
    fun showHome(location: LatLng?) {
        when {
            location == null -> {
                symbolManager.deleteAll()
                reset = true
            }
            reset -> {
                val symbolOptions = SymbolOptions()
                    .withLatLng(location)
                    .withIconImage(R.drawable.ic_home_black_24dp.toString())
                marker = symbolManager.create(symbolOptions)
                reset = false
            }
            else -> {
                marker.latLng = location
                symbolManager.update(marker)
            }
        }
    }

    override fun onDestroy() {
        symbolManager.deleteAll()
        symbolManager.onDestroy()
    }
}