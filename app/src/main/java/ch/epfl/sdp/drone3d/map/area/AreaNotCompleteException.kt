/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

/*
 * This class was taken from the project Fly2Find and adapted for our project
 */
package ch.epfl.sdp.drone3d.map.area

/**
 * An exception thrown when the shape of an area is incomplete
 */
class AreaNotCompleteException(message: String) : Exception(message)