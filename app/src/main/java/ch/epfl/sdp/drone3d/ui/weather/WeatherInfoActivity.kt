/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.weather

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.model.weather.WeatherReport
import ch.epfl.sdp.drone3d.service.api.weather.WeatherService
import ch.epfl.sdp.drone3d.service.impl.weather.WeatherUtils
import ch.epfl.sdp.drone3d.ui.mission.ItineraryShowActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * An activity showing information about the weather for a mapping mission
 */
@AndroidEntryPoint
class WeatherInfoActivity : AppCompatActivity() {

    @Inject
    lateinit var weatherService: WeatherService

    private lateinit var dateInfo: TextView
    private lateinit var weatherDescription: TextView
    private lateinit var temperatureInfo: TextView
    private lateinit var humidityInfo: TextView
    private lateinit var windSpeedInfo: TextView
    private lateinit var visibilityInfo: TextView

    private lateinit var nonExistentMission: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_info)

        dateInfo = findViewById(R.id.lastUpdated)
        weatherDescription = findViewById(R.id.infoWeatherDescription)
        temperatureInfo = findViewById(R.id.infoTemperature)
        humidityInfo = findViewById(R.id.infoHumidity)
        windSpeedInfo = findViewById(R.id.infoWindSpeed)
        visibilityInfo = findViewById(R.id.infoVisibility)

        nonExistentMission = findViewById(R.id.nonExistentMission)
        nonExistentMission.visibility = View.GONE

        val location = intent.extras?.get(ItineraryShowActivity.LOCATION_INTENT_PATH) as LatLng?

        if (location != null) {
            weatherService.getWeatherReport(location).observe(this) { report ->
                setupWeatherInfo(report)
            }
        } else {
            setupInvalidMission()
        }
    }

    /**
     * Setup the weather information
     */
    private fun setupWeatherInfo(weatherReport: WeatherReport) {
        nonExistentMission.visibility = View.GONE

        dateInfo.text = getString(R.string.updated_on, weatherReport.updatedAt.toString())

        weatherDescription.apply {
            text = getString(R.string.info_weather_description, weatherReport.description)
            if (!WeatherUtils.SAFE_CONDITIONS.contains(weatherReport.keywordDescription)) {
                setTextColor(Color.RED)
            }
        }

        temperatureInfo.apply {
            val temperature = weatherReport.temperature
            text = getString(R.string.info_temperature, temperature.toString())
            if (temperature <= WeatherUtils.MIN_TEMPERATURE) {
                setTextColor(Color.RED)
            }
        }

        humidityInfo.text = getString(R.string.info_humidity, weatherReport.humidity.toString())

        windSpeedInfo.apply {
            val windSpeed = weatherReport.windSpeed
            text = getString(R.string.info_wind_speed, windSpeed.toString())
            if (windSpeed >= WeatherUtils.MAX_WIND_SPEED) {
                setTextColor(Color.RED)
            }
        }

        visibilityInfo.apply {
            val visibility = weatherReport.visibility
            text = if (visibility >= 1000) {
                getString(R.string.info_visibility_km, (visibility/1000).toString())
            } else {
                getString(R.string.info_visibility, visibility.toString())
            }
            if (visibility <= WeatherUtils.MIN_VISIBILITY) {
                setTextColor(Color.RED)
            }
        }
    }

    /**
     * Setup the view when weather checked for an invalid mission
     */
    private fun setupInvalidMission() {
        dateInfo.visibility = View.GONE
        weatherDescription.visibility = View.GONE
        temperatureInfo.visibility = View.GONE
        humidityInfo.visibility = View.GONE
        windSpeedInfo.visibility = View.GONE
        visibilityInfo.visibility = View.GONE
        nonExistentMission.visibility = View.VISIBLE
    }
}