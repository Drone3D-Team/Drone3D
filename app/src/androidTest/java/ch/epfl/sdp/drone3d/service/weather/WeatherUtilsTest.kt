/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.service.weather

import ch.epfl.sdp.drone3d.model.weather.WeatherReport
import ch.epfl.sdp.drone3d.service.impl.weather.WeatherUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class WeatherUtilsTest {

    companion object {
        private val BAD_WEATHER_REPORT = WeatherReport("RAIN", "description",
            -1.0, 20, 10.0, 500, Date(12903))

        private val GOOD_WEATHER_REPORT = WeatherReport("Clear", "description",
            20.0, 20, 5.0, 500, Date(12903))
    }

    @Test
    fun isWeatherGoodEnoughWorks() {
        assertTrue(WeatherUtils.isWeatherGoodEnough(GOOD_WEATHER_REPORT))
        assertFalse(WeatherUtils.isWeatherGoodEnough(BAD_WEATHER_REPORT))
    }
}