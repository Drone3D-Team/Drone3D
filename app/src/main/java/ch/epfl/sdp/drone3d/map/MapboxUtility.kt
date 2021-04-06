package ch.epfl.sdp.drone3d.map

import ch.epfl.sdp.drone3d.service.storage.data.MappingMission
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap

class MapboxUtility {
    companion object {

        private val ZOOM_VALUE = 14.0

        /**
         * Zoom on the first step of a mission [mission] on the map [mapboxMap].
         */
        fun zoomOnMission(mission: MappingMission, mapboxMap: MapboxMap){

            if(mission.flightPath.isNotEmpty()){

                val firstCoordinates = LatLng(mission.flightPath[0].latitude, mission.flightPath[0].longitude)

                mapboxMap.cameraPosition =  CameraPosition.Builder()
                    .target(firstCoordinates)
                    .zoom(ZOOM_VALUE)
                    .build()
            }
        }
    }
}