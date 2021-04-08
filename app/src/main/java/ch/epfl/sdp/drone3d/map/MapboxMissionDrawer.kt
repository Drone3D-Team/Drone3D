/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map

import ch.epfl.sdp.drone3d.service.storage.data.MappingMission
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*

class MapboxMissionDrawer(mapView: MapView, mapboxMap: MapboxMap, style: Style) {


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
     * Show a [missionPath] on the map by linking all points with lines.
     */
    fun showMission(missionPath: List<LatLng>) {

        if(missionPath.isEmpty()){
            lineManager.deleteAll()
            symbolManager.deleteAll()
            reset = true
        }
        else {
            symbolManager.deleteAll()
            mapSymbols = missionPath.mapIndexed() { index: Int, latlng: LatLng ->

                val symbolOption = SymbolOptions()
                    .withLatLng(LatLng(latlng))
                    .withTextField(index.toString())

                symbolManager.create(symbolOption)
            }

            if (!::mapLines.isInitialized || reset) {

                val lineOptions = LineOptions()
                    .withLatLngs(missionPath)

                mapLines = lineManager.create(lineOptions)

                reset = false

            } else {
                mapLines.latLngs = missionPath
                lineManager.update(mapLines)
            }
        }
    }


}