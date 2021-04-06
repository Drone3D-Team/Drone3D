/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map

import android.graphics.Color
import ch.epfl.sdp.drone3d.service.storage.data.LatLong
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Circle
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions
import com.mapbox.mapboxsdk.utils.ColorUtils

/**
 * This class is taken from the Fly2Find project.
 */
class MapboxDronePainter(mapView: MapView, mapboxMap: MapboxMap, style: Style) : MapboxPainter {
    private var circleManager = CircleManager(mapView, mapboxMap, style)
    private lateinit var marker: Circle
    private var reset: Boolean = false

    fun paint(locationLatLong: LatLong?) {
        val location = MapUtils.toLatLng(locationLatLong)
        if (location == null) {
            circleManager.deleteAll()
            reset = true
        } else if (!::marker.isInitialized || reset) {
            val circleOptions = CircleOptions()
                .withLatLng(location)
                .withCircleColor(ColorUtils.colorToRgbaString(Color.YELLOW))
            marker = circleManager.create(circleOptions)
            reset = false
        } else {
            marker.latLng = location
            circleManager.update(marker)
        }
    }

    override fun onDestroy() {
        circleManager.deleteAll()
        circleManager.onDestroy()
    }
}