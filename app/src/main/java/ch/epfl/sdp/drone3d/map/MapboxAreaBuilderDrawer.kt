/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * This class was taken from the project Fly2Find and adapted for our project
 */
package ch.epfl.sdp.drone3d.map

import android.graphics.Color
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils

/**
 * Drawer on MapBox for an AreaBuilder
 */
class MapboxAreaBuilderDrawer(
    mapView: MapView,
    mapboxMap: MapboxMap,
    style: Style,
) : MapboxDrawer {

    companion object {
        private const val REGION_FILL_OPACITY: Float = 0.5F
        private const val WAYPOINT_RADIUS: Float = 8f
    }

    val onVertexMoved = mutableListOf<(old: LatLng, new: LatLng) -> Unit>()

    private var fillManager: FillManager = FillManager(mapView, mapboxMap, style)
    private var circleManager: CircleManager = CircleManager(mapView, mapboxMap, style)

    private lateinit var fillArea: Fill

    private var nbVertices = 0

    private val dragListener = object : OnCircleDragListener {
        lateinit var previousLocation: LatLng
        override fun onAnnotationDragStarted(annotation: Circle) {
            previousLocation = annotation.latLng
        }

        override fun onAnnotationDrag(annotation: Circle) {
            onVertexMoved.forEach { it(previousLocation, annotation.latLng) }
            previousLocation = annotation.latLng
        }

        override fun onAnnotationDragFinished(annotation: Circle?) {
        }
    }

    init {
        circleManager.addDragListener(dragListener)
    }

    override fun onDestroy() {
        onVertexMoved.clear()
        nbVertices = 0
        fillManager.deleteAll()
        circleManager.deleteAll()
        fillManager.onDestroy()
        circleManager.onDestroy()
    }

    /**
     * Draw the control vertices and the area formed by the shape vertices of the drawable area
     */
    fun draw(drawableArea: DrawableArea) {
        val controlVertices = drawableArea.getControlVertices()
        val shapeVertices = drawableArea.getShapeVertices()

        if (controlVertices.size != nbVertices) {
            drawControlVertices(controlVertices)
            nbVertices = controlVertices.size
        }
        drawShape(shapeVertices ?: listOf())
    }

    /**
     * Draws a filled polygon described by the list of vertices
     * Those are the vertices that the user can not drag, and do not show up on the map,
     * Only the edges connecting them do
     */
    private fun drawShape(shapeOutline: List<LatLng>) {

        if (!::fillArea.isInitialized) {
            fillManager.deleteAll()
            val fillOption = FillOptions()
                .withLatLngs(listOf(shapeOutline))
                .withFillColor(ColorUtils.colorToRgbaString(Color.WHITE))
                .withFillOpacity(REGION_FILL_OPACITY)
            fillArea = fillManager.create(fillOption)
        } else {

            fillArea.latLngs = listOf(shapeOutline)
            fillManager.update(fillArea)
        }

    }

    /**
     * Draws the control vertices
     * Those are the vertices that the user can drag to modify the shape
     */
    private fun drawControlVertices(vertices: List<LatLng>) {
        circleManager.deleteAll()

        vertices.forEach {
            val circleOptions = CircleOptions()
                .withCircleRadius(WAYPOINT_RADIUS)
                .withLatLng(it)
                .withDraggable(true)
            circleManager.create(circleOptions)
        }
    }
}