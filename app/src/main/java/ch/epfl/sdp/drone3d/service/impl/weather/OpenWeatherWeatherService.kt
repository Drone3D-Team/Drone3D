package ch.epfl.sdp.drone3d.service.impl.weather

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.model.weather.WeatherReport
import ch.epfl.sdp.drone3d.service.api.weather.WeatherService
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import timber.log.Timber
import java.net.URL
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.thread

/**
 * WeatherService implementation using OpenWeather API
 */
class OpenWeatherWeatherService @Inject constructor(@ApplicationContext val context: Context) :
    WeatherService {

    companion object {
        private const val API_ENDPOINT = "https://api.openweathermap.org/data/2.5/weather?"
        private const val TEMP_UNIT = "metric"
        private val NO_DATA = WeatherReport("N/A", "N/A", null, 0.0, 0, 0.0, 0, Date())
    }

    override fun getWeatherReport(location: LatLng): LiveData<WeatherReport> {
        val liveData = MutableLiveData<WeatherReport>()
        thread {
            try {
                val data = fetchAPI(location)
                val report = parseJsonToWeatherReport(data)
                liveData.postValue(report)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return liveData
    }

    /**
     * Parse a json string into a WeatherReport
     */
    private fun parseJsonToWeatherReport(jsonReport: String): WeatherReport {
        var report = NO_DATA
        try {
            /* Extracting JSON returns from the API */
            val jsonObj = JSONObject(jsonReport)
            val main = jsonObj.getJSONObject("main")
            val wind = jsonObj.getJSONObject("wind")
            val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

            val iconId = weather.getString("icon")
            val updatedAt = Date(jsonObj.getLong("dt") * 1000)
            val temperature = main.getDouble("temp")
            val humidity = main.getInt("humidity")
            val windSpeed = wind.getDouble("speed")
            val description = weather.getString("description")
            val keywordDescription = weather.getString("main")
            val visibility = jsonObj.getInt("visibility")


            report = WeatherReport(
                keywordDescription,
                description,
                iconId,
                temperature,
                humidity,
                windSpeed,
                visibility,
                updatedAt
            )
        } catch (e: Exception) {
            Timber.e(e, "Error when getting the weather : $e")
        }
        return report
    }

    /**
     * Fetch OpenWeather API
     */
    private fun fetchAPI(location: LatLng): String {
        val queryUrl = API_ENDPOINT +
                "lat=" + location.latitude +
                "&lon=" + location.longitude +
                "&units=" + TEMP_UNIT +
                "&appid=" + context.getString(R.string.open_weather_api_key)
        return URL(queryUrl).readText()
    }

}