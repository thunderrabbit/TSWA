package com.robnugen.tswa.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Open-Meteo's forecast response, holding only the fields TSWA asks for.
// Temperatures are °C, wind km/h, pressure hPa, visibility metres. Any
// array value can be null when Open-Meteo has no data for that slot.
@Serializable
data class Forecast(
    @SerialName("utc_offset_seconds") val utcOffsetSeconds: Int = 0,
    val timezone: String = "",
    val current: Current,
    val hourly: Hourly,
    val daily: Daily,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun parse(text: String): Forecast = json.decodeFromString(serializer(), text)
    }
}

@Serializable
data class Current(
    val time: String,
    @SerialName("temperature_2m") val temperatureC: Double,
    @SerialName("apparent_temperature") val feelsLikeC: Double? = null,
    @SerialName("relative_humidity_2m") val humidityPercent: Double? = null,
    @SerialName("dew_point_2m") val dewPointC: Double? = null,
    @SerialName("pressure_msl") val pressureHpa: Double? = null,
    @SerialName("wind_speed_10m") val windKmh: Double? = null,
    @SerialName("wind_direction_10m") val windDegrees: Double? = null,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("is_day") val isDay: Int = 1,
)

@Serializable
data class Hourly(
    val time: List<String>,
    @SerialName("temperature_2m") val temperatureC: List<Double?>,
    @SerialName("weather_code") val weatherCode: List<Int?>,
    @SerialName("precipitation_probability") val rainPercent: List<Int?>,
    val visibility: List<Double?>,
)

@Serializable
data class Daily(
    val time: List<String>,
    @SerialName("weather_code") val weatherCode: List<Int?>,
    @SerialName("temperature_2m_max") val highC: List<Double?>,
    @SerialName("temperature_2m_min") val lowC: List<Double?>,
    val sunrise: List<String?>,
    val sunset: List<String?>,
    @SerialName("uv_index_max") val uvIndexMax: List<Double?>,
    @SerialName("precipitation_probability_max") val rainPercentMax: List<Int?>,
)
