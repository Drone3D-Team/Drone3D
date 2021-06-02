/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.model.mission

import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * This class can convert latlng to x,y coordinates in meters in a local area. This class only works well for
 * areas that are not too big and has some errors when the distance increases (maximum near the poles:
 * ~1.2 meters (0.000013°) for ~100km in longitude (1°), almost no error for latitude when we project
 * onto a plane and then back on sphere) due to approximation, since no isometric projection from a sphere
 * to a 2D plane exists. This class should not be used for areas near the poles, where the distance
 * passing on top of the Earth is smaller than the distance around the earth.
 *
 * Longitude is projected along the x-axis and latitude along the y-axis.
 *
 * The [origin] should be one of the point that will be projected or close to them. It will be mapped
 * to the point (0,0) in the projected space.
 */
data class SphereToPlaneProjector(val origin: LatLng) {

    companion object {
        private const val CONVERSION_FACTOR_DEGREE = 0.0001 //~100m near the Equator
        private const val MAX_LONGITUDE = 180
        private const val MAX_LATITUDE = 90
    }

    private val meterToLong =
        CONVERSION_FACTOR_DEGREE / LatLng(origin.latitude, origin.longitude + CONVERSION_FACTOR_DEGREE).distanceTo(origin)
    private val meterToLat =
        CONVERSION_FACTOR_DEGREE / LatLng(origin.latitude + CONVERSION_FACTOR_DEGREE, origin.longitude).distanceTo(origin)


    /**
     * Transforms a [latlng] to a point in this local area.
     */
    fun toPoint(latlng: LatLng): Point {

        val xDistance = LatLng(origin.latitude, latlng.longitude).distanceTo(origin)
        val yDistance = LatLng(latlng.latitude, origin.longitude).distanceTo(origin)

        val x = if ((latlng.longitude >= origin.longitude && latlng.longitude - origin.longitude <= MAX_LONGITUDE)
            || (latlng.longitude < origin.longitude && origin.longitude - latlng.longitude > MAX_LONGITUDE)
        ) xDistance else -xDistance
        val y = if (latlng.latitude >= origin.latitude) yDistance else -yDistance

        return Point(x, y)
    }

    /**
     * Transform a [point] to a latlng in this local area.
     * This is an approximation which should work well for small areas like the ones we will be using.
     */
    fun toLatLng(point: Point): LatLng {

        var longitude = point.x * meterToLong + origin.longitude
        var latitude = point.y * meterToLat + origin.latitude

        if (latitude > MAX_LATITUDE) {
            latitude = 2 * MAX_LATITUDE - latitude
            longitude += MAX_LONGITUDE //We come on the other side of the earth in longitude
        } else if (latitude < -MAX_LATITUDE) {
            latitude = -2 * MAX_LATITUDE - latitude
            longitude += MAX_LONGITUDE //We come on the other side of the earth in longitude
        }

        if (longitude > MAX_LONGITUDE) {
            longitude -= 2 * MAX_LONGITUDE
        } else if (longitude < -MAX_LONGITUDE) {
            longitude += 2 * MAX_LONGITUDE
        }

        return LatLng(latitude, longitude)
    }

    /**
     * Transform the list [latlngs] to a list of points in this local area.
     */
    fun toPoints(latlngs: List<LatLng>): List<Point> {
        return latlngs.map { latlng -> toPoint(latlng) }
    }

    /**
     * Transform the list [points] to a list of latlngs in this local area.
     */
    fun toLatLngs(points: List<Point>): List<LatLng> {
        return points.map { point -> toLatLng(point) }
    }
}