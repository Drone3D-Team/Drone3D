/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map

import ch.epfl.sdp.drone3d.model.mission.MappingMission
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*

/**
 * This class draws the mission of the drone on the map
 */
class MapboxMissionDrawer(mapView: MapView, mapboxMap: MapboxMap, style: Style): MapboxDrawer {

    private var lineManager: LineManager = LineManager(mapView, mapboxMap, style)
    private var symbolManager = SymbolManager(mapView, mapboxMap, style)

    private lateinit var mapLines: Line
    private lateinit var mapSymbols: List<Symbol>

    private var reset = true

    /**
     * Show a [mission] on the map by linking all points with lines.
     */
    fun showMission(mission: MappingMission) {
        showMission(mission.flightPath)
    }

    /**
     * Show a [flightPath] on the map by linking all points with lines.
     */
    fun showMission(flightPath: List<LatLng>) {

        if(flightPath.isEmpty()){
            lineManager.deleteAll()
            symbolManager.deleteAll()
            reset = true
        }
        else {
            symbolManager.deleteAll()
            mapSymbols = flightPath.mapIndexed() { index: Int, latlng: LatLng ->

                val symbolOption = SymbolOptions()
                    .withLatLng(LatLng(latlng))
                    .withTextField(index.toString())

                symbolManager.create(symbolOption)
            }

            if (!::mapLines.isInitialized || reset) {

                val lineOptions = LineOptions()
                    .withLatLngs(flightPath)

                mapLines = lineManager.create(lineOptions)

                reset = false

            } else {
                mapLines.latLngs = flightPath
                lineManager.update(mapLines)
            }
        }
    }

    override fun onDestroy() {
        lineManager.onDestroy()
        symbolManager.onDestroy()
    }
}