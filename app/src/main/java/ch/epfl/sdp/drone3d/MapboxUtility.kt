package ch.epfl.sdp.drone3d

import ch.epfl.sdp.drone3d.storage.data.MappingMission
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

            val flightPathNotNull = mission.flightPath.filter { latLong -> latLong.latitude!=null && latLong.longitude!=null }

            if(flightPathNotNull.isNotEmpty()){

                val firstCoordinates = LatLng(flightPathNotNull[0].latitude!!, flightPathNotNull[0].longitude!!)

                mapboxMap.cameraPosition =  CameraPosition.Builder()
                    .target(firstCoordinates)
                    .zoom(ZOOM_VALUE)
                    .build()
            }
        }
    }
}