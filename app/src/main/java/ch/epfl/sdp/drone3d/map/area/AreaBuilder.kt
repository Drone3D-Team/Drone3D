/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * This class was taken from the project Fly2Find and adapted for our project
 */

package ch.epfl.sdp.drone3d.map.area

import androidx.annotation.VisibleForTesting
import ch.epfl.sdp.drone3d.map.DrawableArea
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlin.properties.Delegates

/**
 * Builder for an area that can be displayed on MapBox as it is constructed
 */

abstract class AreaBuilder : DrawableArea {

    abstract val sizeLowerBound: Int?
    abstract val sizeUpperBound: Int?
    abstract val shapeName: String

    val onAreaChanged = mutableListOf<() -> Unit>()
    val onVerticesChanged = mutableListOf<(MutableList<LatLng>) -> Unit>()

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    var vertices: MutableList<LatLng> by Delegates.observable(mutableListOf()) { _, _, _ ->
        val area = try {
            this.build()
        } catch (ex: Exception) {
            when (ex) {
                is AreaNotCompleteException -> null
                is IllegalArgumentException -> null
                else -> throw ex
            }
        }

        onVerticesChanged.forEach { it(vertices) }
        if (area != null) {
            onAreaChanged.forEach { it() }
        }
    }


    fun onDestroy() {
        reset()
        onAreaChanged.clear()
        onVerticesChanged.clear()
    }

    fun reset() {
        vertices.clear()
        this.vertices = this.vertices
    }

    fun addVertex(vertex: LatLng): AreaBuilder {
        require(isStrictlyUnderUpperBound()) { "Already enough points" }
        vertices.add(vertex)
        orderVertices()
        this.vertices = this.vertices
        return this
    }

    fun moveVertex(old: LatLng, new: LatLng): AreaBuilder {
        val oldIndex = vertices.withIndex().minByOrNull { it.value.distanceTo(old) }?.index!!
        this.vertices[oldIndex] = new
        orderVertices()
        this.vertices = this.vertices
        return this
    }

    protected open fun orderVertices() {}

    private fun isStrictlyUnderUpperBound() = sizeUpperBound?.let { vertices.size < it } ?: true
    private fun isUnderUpperBound() = sizeUpperBound?.let { vertices.size <= it } ?: true
    private fun isAboveLowerBound() = sizeLowerBound?.let { it <= vertices.size } ?: true

    fun isComplete(): Boolean {
        return isAboveLowerBound() && isUnderUpperBound()
    }

    abstract fun buildGivenIsComplete(): Area

    fun build(): Area {
        if (!isComplete()) {
            throw AreaNotCompleteException("$shapeName not complete")
        }
        return buildGivenIsComplete()
    }

    override fun getControlVertices() = vertices

    override fun getShapeVertices(): List<LatLng>? {
        return if (isComplete()) {
            getShapeVerticesGivenComplete()
        } else {
            null
        }
    }

    protected abstract fun getShapeVerticesGivenComplete(): List<LatLng>

    fun getAreaSize(): Float {
        if (!isComplete()) {
            throw AreaNotCompleteException("$shapeName not complete")
        }
        return getAreaSizeGivenComplete()
    }

    protected abstract fun getAreaSizeGivenComplete(): Float
}