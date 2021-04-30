/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.model.mission

import ch.epfl.sdp.drone3d.model.mission.Point.Companion.distance

/**
 * Utility class to represent a parallelogram in meters
 */
data class Parallelogram(val origin: Point, val dir1Span: Vector, val dir2Span: Vector){
    companion object{
        fun getFourthPoint(origin: Point, adjacentPoint1: Point, adjacentPoint2: Point):Point{
            val parallelogram = Parallelogram(origin,adjacentPoint1,adjacentPoint2)
            return parallelogram.origin+parallelogram.dir1Span+parallelogram.dir2Span
        }
    }
    constructor(origin: Point, adjacentPoint1: Point, adjacentPoint2: Point): this(origin, Vector(origin,adjacentPoint1), Vector(origin,adjacentPoint2))

    /**
     * Translate the parallelogram by [translationVector]
     */
    fun translate(translationVector: Vector): Parallelogram {
        return Parallelogram(origin+translationVector,dir1Span,dir2Span)
    }

    /**
     * Returns the equivalent parallelogram with base point diagonally opposite
     * and flipped direction vectors
     */
    fun diagonalEquivalent(): Parallelogram {
        return Parallelogram(origin+dir1Span+dir2Span, dir2Span.reverse(),dir1Span.reverse())
    }

    /**
     * Returns the 4 vertices of the parallelogram
     */
    fun getVertices():List<Point>{
        val vertices = mutableListOf<Point>()

        vertices.add(origin)
        vertices.add(origin+dir1Span)
        vertices.add(origin+dir1Span+dir2Span)
        vertices.add(origin+dir2Span)
        return vertices
    }

    /**
     * Returns the equivalent parallelogram with base point closest to [point]
     */
    fun getClosestEquivalentParallelogram(point: Point): Parallelogram {

        val vertices = getVertices()
        val closestOriginIndex = vertices.withIndex().minByOrNull{ (_, vertex) -> distance(point, vertex) }?.index ?: 0

        return Parallelogram(vertices[closestOriginIndex],vertices[(closestOriginIndex-1+vertices.size)%vertices.size], vertices[(closestOriginIndex+1)%vertices.size])
    }

}