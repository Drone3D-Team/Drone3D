/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.mission

import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * This class can convert latlng to x,y coordinates in meters in a local area. This class only works well for
 * pretty small area (~10 km^2) due to approximation, since no isometric projection from a sphere
 * to a 2D plane exists. This class should not be used for areas near the poles (10km near the poles).
 * The [origin] should be one of the point that will be projected
 */
data class SphereToPlaneProjector(val origin: LatLng) {
    //TODO:
    //Deal with exceptions for points to close to poles (and points to far from origin)

    //The origin will have coordinates (0,0) in the projected space.
    //longitude will be projected along the x-axis and latitude along the y-axis

    private val CONVERSION_FACTOR = 0.0001 //~100m near the Equator
    private val MAX_LONGITUDE = 180
    private val MAX_LATITUDE = 90
    private val meterToLong = CONVERSION_FACTOR/LatLng(origin.latitude, origin.longitude + CONVERSION_FACTOR).distanceTo(origin)
    private val meterToLat = CONVERSION_FACTOR/LatLng(origin.latitude + CONVERSION_FACTOR, origin.longitude).distanceTo(origin)


    /**
     * Transforms a [latlng] to a point in this local area.
     */
    fun toPoint(latlng: LatLng): Point{

        val xDistance = LatLng(origin.latitude, latlng.longitude).distanceTo(origin)
        val yDistance = LatLng(latlng.latitude, origin.longitude).distanceTo(origin)

        val x = if (latlng.longitude >= origin.longitude) xDistance else -xDistance
        val y = if (latlng.latitude >= origin.latitude) yDistance else -yDistance

        return Point(x,y)
    }

    /**
     * Transform a [point] to a latlng in this local area.
     * This is an approximation which should work well for small areas like the ones we will be using.
     */
    fun toLatLng(point: Point): LatLng {

        var longitude = point.x * meterToLong + origin.longitude
        var latitude = point.y * meterToLat + origin.latitude

        if(longitude > MAX_LONGITUDE){
            longitude -= 2 * MAX_LONGITUDE
        }
        else if(longitude < -MAX_LONGITUDE){
            longitude += 2 * MAX_LONGITUDE
        }

        if(latitude > MAX_LATITUDE){
            latitude = 2 * MAX_LATITUDE - latitude
        }
        else if(latitude < -MAX_LATITUDE){
            latitude = -2 * MAX_LATITUDE - latitude
        }

        return LatLng(latitude, longitude)
    }

    /**
     * Transform the list [latlngs] to a list of points in this local area.
     */
    fun toPoints(latlngs: List<LatLng>): List<Point>{
        return latlngs.map{latlng -> toPoint(latlng)}
    }

    /**
     * Transform the list [points] to a list of latlngs in this local area.
     */
    fun toLatLngs(points: List<Point>): List<LatLng>{
        return points.map{point -> toLatLng(point)}
    }
}