package ch.epfl.sdp.drone3d.mission
import kotlin.math.ceil

class ParallelogramMission {

    val FRONTAL_OVERLAP = 0.8
    val SIDE_OVERLAP = 0.7

    //Will have to verify from what corner the start point is the closest=> start mapping from that corner
    //Test Point and vector
    //


    /**
     * Assumes the mission starts at the origin of the parallelogram
     */
    private fun buildMappingMission(area:Parallelogram, projectedImageWidth:Double,projectedImageHeight:Double):List<Point>{
        val direction1Increment = area.ySpan.normalized()*projectedImageWidth*(1-SIDE_OVERLAP)
        val direction2Increment = area.xSpan.normalized()*projectedImageHeight*(1-FRONTAL_OVERLAP)
        val direction1IncrementCount = ceil(area.ySpan.norm()/direction1Increment.norm()).toInt()
        val direction2IncrementCount = ceil(area.xSpan.norm()/direction2Increment.norm()).toInt()

        return buildMappingMission(area.origin,direction1Increment,direction1IncrementCount,direction2Increment,direction2IncrementCount)
    }

    /**
     * Builds a single pass mapping mission on a quadrilateral
     * [direction2Increment] corresponds to the initial frontal direction of the drone and the distance it will travel
     */
    private fun buildMappingMission(startingPoint:Point, direction1Increment:Vector,direction1IncrementCount:Int, direction2Increment: Vector,direction2IncrementCount: Int):List<Point> {
        val resultList = mutableListOf(startingPoint)
        var previousPoint = startingPoint
        var currentDirection2Increment = direction2Increment
        for (i in 1..direction1IncrementCount){
            for (j in 1..direction2IncrementCount){
                previousPoint += currentDirection2Increment
                resultList.add(previousPoint)
            }
            previousPoint += direction1Increment
            resultList.add(previousPoint)
            currentDirection2Increment = currentDirection2Increment.reverse()
        }
        return resultList.toList()
    }
}