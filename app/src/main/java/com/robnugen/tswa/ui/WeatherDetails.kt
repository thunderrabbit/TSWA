package com.robnugen.tswa.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robnugen.tswa.temperature.Scale
import com.robnugen.tswa.weather.Forecast
import com.robnugen.tswa.weather.Measures
import com.robnugen.tswa.weather.WeatherView
import kotlin.math.roundToInt

private val valueStyle = TextStyle(color = TswaColors.Text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)

// The details grid: metric first, the US equivalent quietly underneath.
@Composable
fun Details(
    forecast: Forecast,
    control: ScaleControl,
) {
    val now = forecast.current
    val wind = now.windKmh ?: 0.0
    val uv = forecast.daily.uvIndexMax.firstOrNull() ?: 0.0
    val visKm = WeatherView.visibilityKm(forecast)
    val pressure = now.pressureHpa ?: 0.0
    Column(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val compass = Measures.compassPoint(now.windDegrees)
            Cell("Wind", "${wind.roundToInt()} km/h $compass", "${Measures.mph(wind)} mph")
            Cell("Precipitation", "${WeatherView.rainToday(forecast)}%", "chance today")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Cell("Humidity", "${(now.humidityPercent ?: 0.0).roundToInt()}%")
            Cell("Dew point") { Temperature(now.dewPointC, control, style = valueStyle) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Cell("UV index", "${uv.roundToInt()}", Measures.uvWord(uv))
            Cell("Visibility", "${visKm.roundToInt()} km", "${Measures.miles(visKm)} miles")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Cell("Pressure", "${pressure.roundToInt()} hPa", "${Measures.inHg(pressure)} inHg")
            Spacer(Modifier.weight(1f))
        }
        // Sunrise and sunset share one wide card, as on the web.
        Row(Modifier.fillMaxWidth()) {
            Cell(label = null) {
                Row {
                    SunTime("Sunrise", WeatherView.clockTime(forecast.daily.sunrise.firstOrNull()))
                    SunTime("Sunset", WeatherView.clockTime(forecast.daily.sunset.firstOrNull()))
                }
            }
        }
    }
}

@Composable
private fun RowScope.Cell(
    label: String,
    value: String,
    sub: String? = null,
) {
    Cell(label, sub) { Text(value, style = valueStyle) }
}

@Composable
private fun RowScope.Cell(
    label: String?,
    sub: String? = null,
    value: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        color = TswaColors.Card,
        border = BorderStroke(1.dp, TswaColors.Line),
    ) {
        Column(Modifier.padding(12.dp)) {
            if (label != null) CellLabel(label)
            Box(Modifier.padding(top = 3.dp)) { value() }
            if (sub != null) {
                Text(sub, color = TswaColors.Muted, fontSize = 12.5.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

@Composable
private fun CellLabel(text: String) {
    Text(text.uppercase(), color = TswaColors.Muted, fontSize = 11.sp, letterSpacing = 1.sp)
}

@Composable
private fun RowScope.SunTime(
    label: String,
    time: String,
) {
    Column(Modifier.weight(1f)) {
        CellLabel(label)
        Text(time, style = valueStyle, modifier = Modifier.padding(top = 3.dp))
    }
}
