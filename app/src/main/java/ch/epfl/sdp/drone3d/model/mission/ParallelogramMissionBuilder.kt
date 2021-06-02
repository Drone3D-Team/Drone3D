/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.model.mission

import kotlin.math.PI
import kotlin.math.min
import kotlin.math.tan

/**
 * Utility class that allows the creation of a parallelogram mapping mission
 */
class ParallelogramMissionBuilder {
    companion object {
        private const val FRONTAL_OVERLAP = 0.8
        private const val SIDE_OVERLAP = 0.7

        // Overshoot distance the drone makes before making a turn to make sure the camera
        // is oriented in the right direction before taking the next pictures
        private const val U_TURN_DISTANCE = 5.0 //meters

        /**
         * Returns the list of coordinates indicating where the drone should go and
         * take pictures on a single pass mapping mission.
         * All distances are in meters and angles are in radians
         * The camera pitch is at 0 when the drone looks forward and at PI/2 when the drone looks downwards
         */
        fun buildSinglePassMappingMission(
            startingPoint: Point,
            area: Parallelogram,
            cameraPitch: Float,
            flightHeight: Double,
            groundImageDimension: GroundImageDim
        ): List<Point> {
            val newArea = area.getClosestEquivalentParallelogram(startingPoint)
            return singlePassMappingMission(newArea, cameraPitch, flightHeight, groundImageDimension)
        }

        /**
         * Returns the list of coordinates indicating where the drone should go and
         * take pictures on a double pass mapping mission.
         * All distances are in meters and angles are in radians
         * The camera pitch is at 0 when the drone looks forward and at PI/2 when the drone looks downwards
         */
        fun buildDoublePassMappingMission(
            startingPoint: Point,
            area: Parallelogram,
            cameraPitch: Float,
            flightHeight: Double,
            groundImageDimension: GroundImageDim
        ): List<Point> {
            val firstPassArea = area.getClosestEquivalentParallelogram(startingPoint)
            val mappingMissionFirst = singlePassMappingMission(firstPassArea, cameraPitch, flightHeight, groundImageDimension)
            val secondPassArea = firstPassArea.diagonalEquivalent()
            val mappingMissionSecond = singlePassMappingMission(secondPassArea, cameraPitch, flightHeight, groundImageDimension)
            return mappingMissionFirst + mappingMissionSecond
        }

        /**
         * Builds a single pass mapping mission on a parallelogram starting at the origin of the [area]
         * and compensating for the camera angle and drone height
         * The camera pitch is at 0 when the drone looks forward and at PI/2 when the drone looks downwards
         */
        private fun singlePassMappingMission(
            area: Parallelogram,
            cameraPitch: Float,
            flightHeight: Double,
            groundImageDimension: GroundImageDim
        ): List<Point> {
            val distanceToPictureCenterStart = flightHeight * tan(PI / 2 - cameraPitch)//0 when the drone looks down
            val mainDirectionCompensation = distanceToPictureCenterStart + U_TURN_DISTANCE

            return singlePassMappingMission(area, groundImageDimension, mainDirectionCompensation)
        }

        /**
         * Builds a single pass mapping mission on a parallelogram starting at the origin of the [area]
         * [mainDirectionCompensation] compensates for the tilted camera.
         */
        private fun singlePassMappingMission(
            area: Parallelogram,
            groundImageDimension: GroundImageDim,
            mainDirectionCompensation: Double
        ): List<Point> {

            val direction1Increment = area.dir1Span.normalized() * groundImageDimension.width * (1 - FRONTAL_OVERLAP)
            val direction2Increment = area.dir2Span.normalized() * groundImageDimension.height * (1 - SIDE_OVERLAP)
            val direction1IncrementCount: Double = area.dir1Span.norm() / direction1Increment.norm()
            val direction2IncrementCount: Double = area.dir2Span.norm() / direction2Increment.norm()

            var currentDirection1Increment = direction1Increment
            var remainingDir2IncrementCount = direction2IncrementCount

            val initialCompensatedOrigin = area.origin - currentDirection1Increment.normalized() * mainDirectionCompensation
            var currentPoint = initialCompensatedOrigin
            val resultList: MutableList<Point> = mutableListOf(currentPoint)


            while (remainingDir2IncrementCount > 0) {
                var remainingDir1IncrementCount = direction1IncrementCount
                while (remainingDir1IncrementCount > 0) {
                    currentPoint += currentDirection1Increment * min(1.0, remainingDir1IncrementCount)
                    remainingDir1IncrementCount -= 1
                    resultList.add(currentPoint)
                }
                //To do the U-turn and compensate for the camera downwards angle
                currentPoint += currentDirection1Increment.normalized() * 2.0 * mainDirectionCompensation
                resultList.add(currentPoint)

                currentPoint += direction2Increment * min(1.0, remainingDir2IncrementCount)
                resultList.add(currentPoint)

                remainingDir2IncrementCount -= 1
                currentDirection1Increment = currentDirection1Increment.reverse()
            }
            //Last round
            var remainingDir1IncrementCount = direction1IncrementCount
            while (remainingDir1IncrementCount > 0) {
                currentPoint += currentDirection1Increment * min(1.0, remainingDir1IncrementCount)
                remainingDir1IncrementCount -= 1
                resultList.add(currentPoint)
            }
            return resultList.toList()
        }
    }
}