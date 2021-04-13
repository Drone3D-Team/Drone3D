package ch.epfl.sdp.drone3d.map.area

import com.mapbox.mapboxsdk.geometry.LatLng

class PolygonBuilder : AreaBuilder() {
    override val sizeLowerBound: Int? = 3
    override val sizeUpperBound: Int? = 4
    override val shapeName: String = "Polygon"
    override fun buildGivenIsComplete(): PolygonArea = PolygonArea(vertices)
    override fun getShapeVerticesGivenComplete(): List<LatLng> = vertices
}