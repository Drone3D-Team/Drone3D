package ch.epfl.sdp.drone3d.mission
import kotlin.math.ceil
import kotlin.math.tan


/**
 * Utility class that allows the create of a parallelogram mapping mission
 */
class ParallelogramMissionBuilder {
    companion object{
        private val FRONTAL_OVERLAP = 0.8
        private val SIDE_OVERLAP = 0.7

        /**
         * Returns the coordinates where the drone should take pictures on a single pass mapping mission.
         * A single pass mapping mission is sufficient when the area to map has low terrain features such as a landscape or a field.
         * For more vertical 3D mappings such a cities, see "buildDoublePassMappingMission"
         *
         */
        fun buildSinglePassMappingMission(startingPoint:Point,area:Parallelogram, cameraAngle:Double,droneHeight:Double, projectedImageWidth:Double,projectedImageHeight: Double):List<Point>{
            val newArea = area.getClosestEquivalentParallelogram(startingPoint)
            return singlePassMappingMission(newArea,cameraAngle,droneHeight,projectedImageWidth,projectedImageHeight)
        }

        /**
         * Returns the coordinates where the drone should take pictures on a double pass mapping mission.
         * Use this function for high resolution vertical 3D mappings such a cities.
         */
        fun buildDoublePassMappingMission(startingPoint:Point,area:Parallelogram, cameraAngle:Double,droneHeight:Double, projectedImageWidth:Double,projectedImageHeight: Double):List<Point>{
            val firstPassArea = area.getClosestEquivalentParallelogram(startingPoint)
            val mappingMissionFirst = singlePassMappingMission(firstPassArea,cameraAngle,droneHeight,projectedImageWidth,projectedImageHeight)
            val secondPassArea = firstPassArea.diagonalEquivalent()
            val mappingMissionSecond = singlePassMappingMission(secondPassArea,cameraAngle,droneHeight,projectedImageWidth,projectedImageHeight)
            return mappingMissionFirst + mappingMissionSecond
        }

        /**
         * Builds a single pass mapping mission on a parallelogram starting at the origin of the [area]
         * and compensating for the camera angle and drone height
         */
        private fun singlePassMappingMission(area:Parallelogram, cameraAngle:Double,droneHeight:Double, projectedImageWidth:Double,projectedImageHeight: Double):List<Point>{
            val distanceToPictureCenterStart = droneHeight*tan(cameraAngle)
            val direction1CameraCompensation = (area.dir1Span.normalized()*distanceToPictureCenterStart).reverse()
            val direction2CameraCompensation = (area.dir2Span.normalized()*distanceToPictureCenterStart).reverse()

            //Shift it in order to compensate the fact that the image is not taken perpendicular
            val newArea = area.translate(direction1CameraCompensation).translate(direction2CameraCompensation)

            return singlePassMappingMission(newArea,projectedImageWidth,projectedImageHeight)
        }

        /**
         * Builds a single pass mapping mission on a parallelogram starting at the origin of the [area]
         */
        private fun singlePassMappingMission(area:Parallelogram, projectedImageWidth:Double,projectedImageHeight:Double):List<Point>{

            val direction1Increment = area.dir1Span.normalized()*projectedImageWidth*(1-FRONTAL_OVERLAP)
            val direction2Increment = area.dir2Span.normalized()*projectedImageHeight*(1-SIDE_OVERLAP)
            val direction1IncrementCount = ceil(area.dir1Span.norm()/direction1Increment.norm()).toInt()
            val direction2IncrementCount = ceil(area.dir2Span.norm()/direction2Increment.norm()).toInt() +1

            var currentPoint = area.origin

            val resultList = mutableListOf(currentPoint)
            var currentDirection1Increment = direction1Increment
            for (i in 0 until direction2IncrementCount){
                for (j in 0 until direction1IncrementCount){
                    currentPoint += currentDirection1Increment
                    resultList.add(currentPoint)
                }
                //Not needed for last iteration
                if(i!=direction2IncrementCount-1){
                    currentPoint += direction2Increment
                    resultList.add(currentPoint)
                    currentDirection1Increment = currentDirection1Increment.reverse()
                }
            }
            return resultList.toList()
        }
    }
}