package ch.epfl.sdp.drone3d

import ch.epfl.sdp.drone3d.storage.data.LatLong
import ch.epfl.sdp.drone3d.storage.data.MappingMission
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

        val flightPathNotNull = mission.flightPath.filter { latLong -> latLong.latitude!=null && latLong.longitude!=null }

        if(flightPathNotNull.isEmpty()){
            lineManager.deleteAll()
            symbolManager.deleteAll()
            reset = true
        }
        else {

            val coordinates: List<LatLng> = flightPathNotNull.map { pos: LatLong ->
                LatLng(pos.latitude!!, pos.longitude!!)
            }

            symbolManager.deleteAll()
            mapSymbols = coordinates.mapIndexed() { index: Int, latlng: LatLng ->

                val symbolOption = SymbolOptions()
                    .withLatLng(LatLng(latlng))
                    .withTextField(index.toString())

                symbolManager.create(symbolOption)
            }

            if (!::mapLines.isInitialized || reset) {

                val lineOptions = LineOptions()
                    .withLatLngs(coordinates)

                mapLines = lineManager.create(lineOptions)

                reset = false

            } else {
                mapLines.latLngs = coordinates
                lineManager.update(mapLines)
            }
        }
    }


}