/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map

import ch.epfl.sdp.drone3d.model.mission.MappingMission
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap

/**
 * Utility class that provides static methods used with mapbox.
 */
class MapboxUtility {

    companion object {

        private const val ZOOM_VALUE = 14.0

        /**
         * Zoom on the first step of a mission [mission] on the map [mapboxMap].
         */
        fun zoomOnMission(mission: MappingMission, mapboxMap: MapboxMap) {
            zoomOnMission(mission.area, mapboxMap)
        }

        /**
         * Zoom on the first step of a [missionPath] on the map [mapboxMap].
         */
        fun zoomOnMission(missionPath: List<LatLng>, mapboxMap: MapboxMap) {

            if (missionPath.isNotEmpty()) {

                val firstCoordinates = missionPath[0]

                zoomOnCoordinate(firstCoordinates, mapboxMap)
            }
        }

        /**
         * Zoom on [coordinate] on the map [mapboxMap]
         */
        fun zoomOnCoordinate(coordinate: LatLng, mapboxMap: MapboxMap, zoomValue: Double = ZOOM_VALUE) {
            mapboxMap.cameraPosition = CameraPosition.Builder()
                .target(coordinate)
                .zoom(zoomValue)
                .build()
        }
    }
}