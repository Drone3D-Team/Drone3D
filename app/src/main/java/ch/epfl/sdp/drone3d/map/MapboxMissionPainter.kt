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
import com.mapbox.mapboxsdk.plugins.annotation.Line
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import com.mapbox.mapboxsdk.utils.ColorUtils

/**
 * This class is taken from the Fly2Find project.
 */
class MapboxMissionPainter(mapView: MapView, mapboxMap: MapboxMap, style: Style) : MapboxPainter {
    companion object {
        private const val PATH_THICKNESS: Float = 2F
    }

    private var lineManager: LineManager = LineManager(mapView, mapboxMap, style)
    private lateinit var lineArea: Line
    private var reset: Boolean = false

    fun paint(path: List<LatLng>?) {
        if (path == null || path.isEmpty()) {
            lineManager.deleteAll()
            reset = true
        } else if (!::lineArea.isInitialized || reset) {
            lineManager.deleteAll()
            val lineOptions = LineOptions()
                .withLatLngs(path)
                .withLineWidth(PATH_THICKNESS)
                .withLineColor(ColorUtils.colorToRgbaString(Color.BLUE))
            lineArea = lineManager.create(lineOptions)
            reset = false
        } else {
            lineArea.latLngs = path
            lineManager.update(lineArea)
        }
    }

    override fun onDestroy() {
        lineManager.deleteAll()
        lineManager.onDestroy()
    }
}