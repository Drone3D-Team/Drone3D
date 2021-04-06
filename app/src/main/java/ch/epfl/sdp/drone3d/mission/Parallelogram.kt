package ch.epfl.sdp.drone3d.mission
import ch.epfl.sdp.drone3d.mission.Point.Companion.distance
import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * Utility class to represent a parallelogram
 */
data class Parallelogram(val origin:Point, val dir1Span:Vector, val dir2Span:Vector){

    constructor(origin: Point,dir1Point:Point,dir2Point:Point): this(origin,Vector(origin,dir1Point),Vector(origin,dir2Point))

    constructor(origin: LatLng, dir1Point:LatLng, dir2Point:LatLng): this(Point(origin),Point(dir1Point),Point(dir2Point))

    /**
     * Translate the parallelogram by [translationVector]
     */
    fun translate(translationVector:Vector):Parallelogram{
        return Parallelogram(origin+translationVector,dir1Span,dir2Span)
    }

    /**
     * Returns the equivalent parallelogram with base point diagonally opposite
     * and flipped direction vectors
     */
    fun diagonalEquivalent():Parallelogram{
        return Parallelogram(origin+dir1Span+dir2Span, dir2Span.reverse(),dir1Span.reverse())
    }

    /**
     * Returns the 4 vertices of the parallelogram
     */
    fun getVertices():List<Point>{
        var vertices = mutableListOf<Point>()

        vertices.add(origin)
        vertices.add(origin+dir1Span)
        vertices.add(origin+dir1Span+dir2Span)
        vertices.add(origin+dir2Span)
        return vertices
    }

    /**
     * Returns the equivalent parallelogram with base point closest to [point]
     */
    fun getClosestEquivalentParallelogram(point:Point):Parallelogram {
        val vertices = getVertices()
        var closestOriginIndex = 0
        var smallestDistance =  distance(point,vertices[0])
        for (i in 1 until vertices.size){
            val distance = distance(point,vertices[i])
            if(distance<smallestDistance){
                smallestDistance = distance
                closestOriginIndex = i
            }
        }
        return Parallelogram(vertices[closestOriginIndex],vertices[(closestOriginIndex-1+vertices.size)%vertices.size], vertices[(closestOriginIndex+1)%vertices.size])
    }

}