/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.impl.weather

import ch.epfl.sdp.drone3d.model.weather.WeatherReport

/**
 * Contains useful functions linked with the weather used in the rest of our app
 */
object WeatherUtils {

    // limit values for the weather
    // temperature min 0 degree Celsius
    const val MIN_TEMPERATURE = 0
    // wind speed max = 8.8 m/s
    const val MAX_WIND_SPEED = 8.8
    // visibility min = 300 m
    const val MIN_VISIBILITY = 300.0
    // set containing the keyword where it is dangerous for the drone to be launched
    val SAFE_CONDITIONS = setOf("Clear", "Clouds")

    /**
     * Check if the weather is good enough for the drone to fly or not
     */
    fun isWeatherGoodEnough(report: WeatherReport): Boolean {
        return report.temperature > MIN_TEMPERATURE
                && report.windSpeed < MAX_WIND_SPEED
                && report.visibility > MIN_VISIBILITY
                && SAFE_CONDITIONS.contains(report.keywordDescription)
    }
}