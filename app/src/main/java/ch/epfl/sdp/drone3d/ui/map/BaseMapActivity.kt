/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.maps.MapView

/**
 * This class defines the basic comportment of a map activity
 *
 * This class is taken from the Fly2Find project, but has been renamed from MapViewBaseActivity.kt.
 */
open class BaseMapActivity : AppCompatActivity() {
    lateinit var mapView: MapView

    protected fun initMapView(savedInstanceState: Bundle?, contentViewId: Int, mapViewId: Int) {
        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(contentViewId)
        // Set up the MapView
        mapView = findViewById(mapViewId)
        mapView.onCreate(savedInstanceState)
    }

    // Override Activity lifecycle methods
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}