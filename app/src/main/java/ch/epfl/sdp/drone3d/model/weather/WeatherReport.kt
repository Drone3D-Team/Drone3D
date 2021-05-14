/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.model.weather

import java.util.*

/**
 * Represent a weather report with attributes:
 * - [keywordDescription]: a short description of the weather in word
 * - [description]: a description of the weather in word
 * - [temperature]: temperature in Celsius
 * - [humidity]: humidity in %
 * - [windSpeed]: speed of the wind in meter per second
 * - [visibility]: visibility in meter
 */
data class WeatherReport(
    val keywordDescription: String,
    val description: String,
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val visibility: Int,
    val updatedAt: Date
)
