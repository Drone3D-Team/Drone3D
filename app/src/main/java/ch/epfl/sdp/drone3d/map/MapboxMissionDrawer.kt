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
    fun showMission(mission: MappingMission, withIndex : Boolean) {
        showMission(mission.flightPath, withIndex)
    }

    /**
     * Show a [flightPath] on the map by linking all points with lines.
     */
    fun showMission(flightPath: List<LatLng>, withIndex : Boolean) {
        symbolManager.deleteAll()

        if(flightPath.isEmpty()){
            lineManager.deleteAll()
            reset = true
        }
        else {
            if(withIndex){
                mapSymbols = flightPath.mapIndexed() { index: Int, latlng: LatLng ->

                    val symbolOption = SymbolOptions()
                        .withLatLng(LatLng(latlng))
                        .withTextField(index.toString())

                    symbolManager.create(symbolOption)
                }
            }else{
                displayStartAndEnd(flightPath)
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

    // Draw tags for first and last point
    private fun displayStartAndEnd(flightPath: List<LatLng>){
        if(flightPath.isNotEmpty()) {
            val symbolOptionFirst = SymbolOptions()
                .withLatLng(LatLng(flightPath[0]))
                .withTextField("Start")
            val first = symbolManager.create(symbolOptionFirst)
            val symbolOptionLast = SymbolOptions()
                .withLatLng(LatLng(flightPath[flightPath.lastIndex]))
                .withTextField("End")
            val last = symbolManager.create(symbolOptionLast)
            mapSymbols = listOf(first, last)
        }
    }

    override fun onDestroy() {
        lineManager.onDestroy()
        symbolManager.onDestroy()
    }
}