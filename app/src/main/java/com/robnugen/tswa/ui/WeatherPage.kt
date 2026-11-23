package com.robnugen.tswa.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robnugen.tswa.temperature.Scale
import com.robnugen.tswa.weather.CachedWeather
import com.robnugen.tswa.weather.WeatherStatus
import com.robnugen.tswa.weather.Wmo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TEMPORARY (mg #894): one line proving the weather layer works on the
// phone. The real Weather page replaces this in mg #895.
@Composable
fun WeatherPage(
    weather: CachedWeather?,
    status: WeatherStatus,
    scale: Scale,
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Weather", color = TswaColors.Text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Text(
            debugLine(weather, status, scale),
            color = TswaColors.Cyan,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

private fun debugLine(
    weather: CachedWeather?,
    status: WeatherStatus,
    scale: Scale,
): String {
    if (weather == null) return "Debug: no weather yet ($status)"
    val now = weather.forecast.current
    val at = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(weather.fetchedAtMillis))
    val how =
        when (status) {
            WeatherStatus.Fresh -> "fresh, fetched $at"
            WeatherStatus.Loading -> "loading; showing data from $at"
            WeatherStatus.Offline -> "offline; last good data from $at"
        }
    return "Debug: ${weather.place.fullName} ${scale.format(now.temperatureC)}, " +
        "${Wmo.describe(now.weatherCode)} — $how"
}
