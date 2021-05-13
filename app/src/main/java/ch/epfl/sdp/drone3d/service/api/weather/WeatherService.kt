package ch.epfl.sdp.drone3d.service.api.weather

import androidx.lifecycle.LiveData
import ch.epfl.sdp.drone3d.model.weather.WeatherReport
import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * This interface represents a service that enables you to get weather report for a specific
 * location.
 */
interface WeatherService {

    /**
     * Return the WeatherReport of the given [location]
     */
    fun getWeatherReport(location: LatLng): LiveData<WeatherReport>
}