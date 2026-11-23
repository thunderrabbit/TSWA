package com.robnugen.tswa.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robnugen.tswa.temperature.Scale
import com.robnugen.tswa.weather.Current
import com.robnugen.tswa.weather.Forecast
import com.robnugen.tswa.weather.Place
import com.robnugen.tswa.weather.WeatherController
import com.robnugen.tswa.weather.WeatherStatus
import com.robnugen.tswa.weather.WeatherView
import com.robnugen.tswa.weather.Wmo
import java.util.Date
import android.text.format.DateFormat as AndroidDateFormat

// The hero number scales with the screen, like the web's
// clamp(84px, 26vw, 120px); its unit is 45% of its size, raised.
private const val HERO_WIDTH_SHARE = 0.26f
private const val HERO_MIN_SP = 84f
private const val HERO_MAX_SP = 120f
private const val HERO_UNIT_SHARE = 0.45f
private const val HERO_UNIT_RAISE = 0.42f

private val feelsStyle = TextStyle(color = TswaColors.Muted, fontSize = 15.sp)
private val highStyle = TextStyle(color = TswaColors.Text, fontSize = 16.sp)
private val lowStyle = TextStyle(color = TswaColors.Muted, fontSize = 16.sp)

// Everything about the weather on screen. Every temperature is tappable
// and steps the one app-wide scale.
@Composable
fun WeatherPage(
    weather: WeatherController,
    control: ScaleControl,
    enabled: Set<Scale>,
) {
    val shown = weather.weather
    val forecast = shown?.forecast
    Hero(shown?.place ?: weather.currentPlace, forecast?.current, control)
    if (forecast != null) {
        HighLow(forecast, control)
        Text(
            switchHint(enabled),
            color = TswaColors.Muted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        )
        Hourly(forecast, control)
        TenDays(forecast, control)
        Details(forecast, control)
    }
    StatusLine(weather)
}

// Place, conditions, the big number and "Feels like".
@Composable
private fun Hero(
    place: Place,
    current: Current?,
    control: ScaleControl,
) {
    Column(
        Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            place.fullName,
            color = TswaColors.Text,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            if (current == null) "Loading…" else Wmo.describe(current.weatherCode),
            color = TswaColors.Muted,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 3.dp),
        )
        HeroNumber(current?.temperatureC, control)
        if (current != null) {
            Temperature(current.feelsLikeC, control, Modifier.padding(top = 4.dp), "Feels like ", feelsStyle)
        }
    }
}

// The big light number with its unit small and raised, as on the web.
@Composable
private fun HeroNumber(
    celsius: Double?,
    control: ScaleControl,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val size = (LocalConfiguration.current.screenWidthDp * HERO_WIDTH_SHARE).coerceIn(HERO_MIN_SP, HERO_MAX_SP)
    val unitStyle =
        SpanStyle(
            fontSize = (size * HERO_UNIT_SHARE).sp,
            color = TswaColors.Muted,
            fontWeight = FontWeight.Normal,
            baselineShift = BaselineShift(HERO_UNIT_RAISE),
        )
    Text(
        buildAnnotatedString {
            append(celsius?.let { control.scale.format(it, withSymbol = false) } ?: "–")
            withStyle(unitStyle) { append(control.scale.symbol) }
        },
        color = if (pressed) TswaColors.Amber else TswaColors.Text,
        fontSize = size.sp,
        fontWeight = FontWeight.Light,
        maxLines = 1,
        softWrap = false,
        modifier =
            Modifier.clickable(
                interactionSource = interaction,
                indication = null,
                enabled = celsius != null,
                role = Role.Button,
                onClickLabel = "Step to the next temperature scale",
                onClick = control.onStep,
            ),
    )
}

// ↑ high  ↓ low  · N% rain (only when the chance is worth a mention).
@Composable
private fun HighLow(
    forecast: Forecast,
    control: ScaleControl,
) {
    val rain = WeatherView.rainToday(forecast)
    Row(
        Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
    ) {
        Temperature(forecast.daily.highC.firstOrNull(), control, prefix = "↑ ", style = highStyle)
        Temperature(forecast.daily.lowC.firstOrNull(), control, prefix = "↓ ", style = lowStyle)
        if (WeatherView.rainWorthMentioning(rain)) {
            Text("· $rain% rain", color = TswaColors.Cyan, fontSize = 16.sp)
        }
    }
}

// Attribution, and how current the weather is; a search's message
// takes the line over, as on the web version.
@Composable
private fun StatusLine(weather: WeatherController) {
    val context = LocalContext.current
    val updated =
        weather.weather?.let { AndroidDateFormat.getTimeFormat(context).format(Date(it.fetchedAtMillis)) }
    val tail =
        when {
            weather.status == WeatherStatus.Offline -> " · Last good data — offline"
            weather.status == WeatherStatus.Loading -> " · Updating…"
            updated != null -> " · Updated $updated"
            else -> ""
        }
    Text(
        weather.searchNote ?: "Weather: Open-Meteo (CC BY 4.0)$tail",
        color = TswaColors.Muted,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 6.dp),
    )
}
