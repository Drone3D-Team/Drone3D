/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.model.mission

/**
 * Utility class to represent a 2D point in meters
 */
data class Point(val x: Double, val y: Double) {

    companion object {
        /**
         * Computes the middle point between two points
         */
        fun middle(a: Point, b: Point) = Point((a.x + b.x) / 2, (a.y + b.y) / 2)

        /**
         * Computes the distance between two points
         */
        fun distance(a: Point, b: Point) = Vector(a, b).norm()
    }

    operator fun plus(vector: Vector) = Point(this.x + vector.x, this.y + vector.y)
    operator fun minus(vector: Vector) = Point(this.x - vector.x, this.y - vector.y)

    /**
     * Unit vector representing the direction from this point to [other]
     */
    fun direction(other: Point) = Vector(this, other).normalized()
}