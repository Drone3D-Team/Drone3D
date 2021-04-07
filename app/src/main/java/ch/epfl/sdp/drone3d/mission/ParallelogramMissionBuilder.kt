/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.mission
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.tan


/**
 * Utility class that allows the creation of a parallelogram mapping mission
 */
class ParallelogramMissionBuilder {
    companion object{
        private val FRONTAL_OVERLAP = 0.8
        private val SIDE_OVERLAP = 0.7

        /**
         * Returns the coordinates where the drone should take pictures on a single pass mapping mission.
         * A single pass mapping mission is sufficient when the area to map has low terrain features such as a landscape or a field.
         * For more vertical 3D mappings such a cities, see "buildDoublePassMappingMission"
         * All distances are in meters and angles are in radians
         */
        fun buildSinglePassMappingMission(startingPoint:Point,area:Parallelogram, cameraAngle:Double,droneHeight:Double, projectedImageWidth:Double,projectedImageHeight: Double):List<Point>{
            val newArea = area.getClosestEquivalentParallelogram(startingPoint)
            return singlePassMappingMission(newArea,cameraAngle,droneHeight,projectedImageWidth,projectedImageHeight)
        }

        /**
         * Returns the coordinates where the drone should take pictures on a double pass mapping mission.
         * Use this function for high resolution vertical 3D mappings such a cities.
         * All distances are in meters and angles are in radians
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
            val direction1IncrementCount:Double = area.dir1Span.norm()/direction1Increment.norm()
            val direction2IncrementCount:Double = area.dir2Span.norm()/direction2Increment.norm()

            var currentPoint = area.origin
            val resultList = mutableListOf(currentPoint)

            var currentDirection1Increment = direction1Increment
            var remainingDir2IncrementCount = direction2IncrementCount

            while (remainingDir2IncrementCount>0){
                var remainingDir1IncrementCount = direction1IncrementCount
                while (remainingDir1IncrementCount>0){
                    currentPoint += currentDirection1Increment*min(1.0,remainingDir1IncrementCount)
                    remainingDir1IncrementCount-=1
                    resultList.add(currentPoint)
                }

                currentPoint += direction2Increment*min(1.0,remainingDir2IncrementCount)
                remainingDir2IncrementCount-=1
                resultList.add(currentPoint)
                currentDirection1Increment = currentDirection1Increment.reverse()
            }
            //Last round
            var remainingDir1IncrementCount = direction1IncrementCount
            while (remainingDir1IncrementCount>0){
                currentPoint += currentDirection1Increment*min(1.0,remainingDir1IncrementCount)
                remainingDir1IncrementCount-=1
                resultList.add(currentPoint)
            }
            return resultList.toList()
        }
    }
}