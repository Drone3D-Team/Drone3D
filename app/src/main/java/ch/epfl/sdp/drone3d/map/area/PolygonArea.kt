package ch.epfl.sdp.drone3d.map.area
import com.mapbox.mapboxsdk.geometry.LatLng

class PolygonArea(override val vertices: List<LatLng>) : Area {
    init {
        require(vertices.size >= 3)
    }
}