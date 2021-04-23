/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map

/**
 * This interface represents a class who draws something on the map.
 *
 * This interface is taken from the Fly2Find project.
 */
interface MapboxDrawer {

    /**
     * Destroy the instance of the drawer
     */
    fun onDestroy()
}