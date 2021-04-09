package ch.epfl.sdp.drone3d.mission

import kotlin.math.sqrt

/**
 * Utility class to represent a 2D vector
 */
data class Vector (val x:Double,val y:Double) {

    /**
     * Represents the vector from a to b
     */
    constructor(a:Point,b:Point):this(b.x-a.x,b.y-a.y)

    operator fun unaryMinus() = Vector(-x, -y)
    operator fun plus(other: Vector) = Vector(x+other.x,y+other.y)
    operator fun minus(other: Vector) = Vector(x-other.x,y-other.y)
    operator fun times(factor: Double) = Vector(x*factor,y*factor)
    operator fun div(factor: Double) = Vector(x/factor,y/factor)

    /**
     * Returns the euclidean norm of the vector
     */
    fun norm() = sqrt(x*x+y*y)

    /**
     * Returns a normalized version of this vector
     */
    fun normalized() = this/norm()

    /**
     * Reverse the vectors orientation
     */
    fun reverse() = -this

}