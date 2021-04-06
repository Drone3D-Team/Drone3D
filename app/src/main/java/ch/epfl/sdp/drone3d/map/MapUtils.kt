/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.map

import ch.epfl.sdp.drone3d.service.storage.data.LatLong
import com.mapbox.mapboxsdk.geometry.LatLng

object MapUtils {

    fun toLatLng(latLong: LatLong?) : LatLng? {
        return if (latLong?.latitude == null || latLong.longitude == null) {
            null
        } else {
            LatLng(latLong.latitude, latLong.longitude)
        }
    }
}