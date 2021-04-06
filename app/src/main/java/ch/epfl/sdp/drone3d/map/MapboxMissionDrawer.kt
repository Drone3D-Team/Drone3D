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

    fun showMission(mission: MappingMission) {

        if(mission.flightPath.isEmpty()){
            lineManager.deleteAll()
            symbolManager.deleteAll()
            reset = true
        }
        else {
            symbolManager.deleteAll()
            mapSymbols = mission.flightPath.mapIndexed() { index: Int, latlng: LatLng ->

                val symbolOption = SymbolOptions()
                    .withLatLng(LatLng(latlng))
                    .withTextField(index.toString())

                symbolManager.create(symbolOption)
            }

            if (!::mapLines.isInitialized || reset) {

                val lineOptions = LineOptions()
                    .withLatLngs(mission.flightPath)

                mapLines = lineManager.create(lineOptions)

                reset = false

            } else {
                mapLines.latLngs = mission.flightPath
                lineManager.update(mapLines)
            }
        }
    }


}