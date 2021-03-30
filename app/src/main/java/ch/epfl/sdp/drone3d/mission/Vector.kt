package ch.epfl.sdp.drone3d.mission

import ch.epfl.sdp.drone3d.storage.data.LatLong
import kotlin.math.sqrt
import kotlin.math.abs

data class Vector (val x:Double,val y:Double) {

    /**
     * Represents the vector from a to b
     */
    constructor(a:Point,b:Point){
        Vector(b.x-a.x,b.y-a.y)
    }

    operator fun unaryMinus() = Vector(-x, -y)
    operator fun plus(other: Vector) = Vector(x+other.x,y+other.y)
    operator fun minus(other: Vector) = Vector(x-other.x,y-other.y)
    operator fun times(factor: Double) = Vector(x*factor,y*factor)
    operator fun div(factor: Double) = Vector(x/factor,y/factor)
    operator fun times(factor: Int) = Vector(x*factor,y*factor)
    operator fun div(factor: Int) = Vector(x/factor,y/factor)
    fun norm() = sqrt(x*x+y*y)
    fun normalized() = this/norm()
    fun reverse() = -this

}