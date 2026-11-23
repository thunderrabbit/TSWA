package com.robnugen.tswa.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robnugen.tswa.R
import com.robnugen.tswa.temperature.Scale
import com.robnugen.tswa.weather.DaySlot
import com.robnugen.tswa.weather.Forecast
import com.robnugen.tswa.weather.Glyph
import com.robnugen.tswa.weather.Tone
import com.robnugen.tswa.weather.WeatherView

private val dayLowStyle = TextStyle(color = TswaColors.Muted, fontSize = 15.sp, textAlign = TextAlign.End)
private val dayHighStyle =
    TextStyle(color = TswaColors.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
private val hourTempStyle = TextStyle(color = TswaColors.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

// Twelve hours from now, scrolling sideways; "Now" sits on a card.
@Composable
fun Hourly(
    forecast: Forecast,
    control: ScaleControl,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 14.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WeatherView.hours(forecast).forEachIndexed { i, hour ->
            Column(
                Modifier
                    .widthIn(min = 62.dp)
                    .background(if (i == 0) TswaColors.Card else Color.Transparent, RoundedCornerShape(12.dp))
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(hour.label, color = TswaColors.Muted, fontSize = 12.sp, maxLines = 1, softWrap = false)
                GlyphIcon(hour.glyph, size = 22, modifier = Modifier.padding(top = 3.dp))
                Temperature(hour.temperatureC, control, Modifier.padding(top = 3.dp), style = hourTempStyle)
                Text(
                    rainLabel(hour.rainPercent),
                    color = TswaColors.Cyan,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

// The web left a 0% chance blank.
private fun rainLabel(percent: Int?): String = if (percent == null || percent == 0) "" else "$percent%"

// Today, then weekdays: icon, rain chance, low, range bar, high. Columns
// are as wide as their widest text, so no temperature is ever clipped (a
// clipped °R reads as °F, mg #905). When one line cannot hold them all
// (°R or K at a large font size), low, bar and high move to a second line.
@Composable
fun TenDays(
    forecast: Forecast,
    control: ScaleControl,
) {
    val days = WeatherView.days(forecast)
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
        shape = RoundedCornerShape(16.dp),
        color = TswaColors.Card,
        border = BorderStroke(1.dp, TswaColors.Line),
    ) {
        BoxWithConstraints(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            val widths = tenDayWidths(days, control.scale)
            val oneLine = widths.oneLine() <= maxWidth
            Column {
                days.forEachIndexed { i, day ->
                    if (i > 0) HorizontalDivider(thickness = 1.dp, color = TswaColors.Line)
                    if (oneLine) DayLine(day, control, widths) else DayTwoLines(day, control, widths)
                }
            }
        }
    }
}

private val dayGap = 8.dp
private val dayIcon = 20.dp
private val minBar = 24.dp
private val minName = 80.dp
private val minRain = 36.dp
private val minDayTemp = 64.dp
private val dayNameStyle = TextStyle(color = TswaColors.Text, fontSize = 15.sp)
private val dayRainStyle = TextStyle(color = TswaColors.Cyan, fontSize = 12.sp)

private data class TenDayWidths(
    val name: Dp,
    val rain: Dp,
    val low: Dp,
    val high: Dp,
) {
    // Six columns, five gaps.
    fun oneLine(): Dp = name + dayIcon + rain + low + minBar + high + dayGap + dayGap + dayGap + dayGap + dayGap
}

// Each column's width: its widest text as drawn now (scale, font size),
// and never narrower than the column was before mg #905.
@Composable
private fun tenDayWidths(
    days: List<DaySlot>,
    scale: Scale,
): TenDayWidths {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current

    fun widest(
        texts: List<String>,
        style: TextStyle,
        atLeast: Dp,
    ): Dp {
        val px = texts.maxOfOrNull { measurer.measure(it, style, maxLines = 1, softWrap = false).size.width } ?: 0
        return maxOf(atLeast, with(density) { px.toDp() })
    }
    return TenDayWidths(
        name = widest(days.map { it.name }, dayNameStyle, minName),
        rain = widest(days.map { rainLabel(it.rainPercent) }, dayRainStyle, minRain),
        low = widest(days.mapNotNull { it.lowC }.map(scale::format), dayLowStyle, minDayTemp),
        high = widest(days.mapNotNull { it.highC }.map(scale::format), dayHighStyle, minDayTemp),
    )
}

@Composable
private fun DayLine(
    day: DaySlot,
    control: ScaleControl,
    widths: TenDayWidths,
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dayGap),
    ) {
        NameIconRain(day, widths)
        Temperature(day.lowC, control, Modifier.width(widths.low), style = dayLowStyle)
        RangeBar(day.barStart, day.barWidth)
        Temperature(day.highC, control, Modifier.width(widths.high), style = dayHighStyle)
    }
}

@Composable
private fun DayTwoLines(
    day: DaySlot,
    control: ScaleControl,
    widths: TenDayWidths,
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dayGap)) {
            NameIconRain(day, widths)
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dayGap),
        ) {
            Temperature(day.lowC, control, Modifier.width(widths.low), style = dayLowStyle)
            RangeBar(day.barStart, day.barWidth)
            Temperature(day.highC, control, Modifier.width(widths.high), style = dayHighStyle)
        }
    }
}

@Composable
private fun NameIconRain(
    day: DaySlot,
    widths: TenDayWidths,
) {
    Text(day.name, style = dayNameStyle, maxLines = 1, softWrap = false, modifier = Modifier.width(widths.name))
    GlyphIcon(day.glyph, size = dayIcon.value.toInt())
    Text(
        rainLabel(day.rainPercent),
        style = dayRainStyle,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier.width(widths.rain),
    )
}

// A cyan-to-amber bar on a navy track, placed on the ten-day range.
@Composable
private fun RowScope.RangeBar(
    start: Float,
    width: Float,
) {
    val gradient = Brush.horizontalGradient(listOf(TswaColors.Cyan, TswaColors.Amber))
    Row(
        Modifier
            .weight(1f)
            .height(4.dp)
            .background(TswaColors.Navy, RoundedCornerShape(2.dp)),
    ) {
        if (start > 0f) Spacer(Modifier.weight(start))
        Box(Modifier.weight(width).fillMaxHeight().background(gradient, RoundedCornerShape(2.dp)))
        val rest = 1f - start - width
        if (rest > 0f) Spacer(Modifier.weight(rest))
    }
}

@Composable
private fun GlyphIcon(
    glyph: Glyph,
    size: Int,
    modifier: Modifier = Modifier,
) {
    Icon(
        painterResource(glyph.drawable()),
        contentDescription = null,
        tint =
            when (glyph.tone) {
                Tone.Sun -> TswaColors.Amber
                Tone.Rain -> TswaColors.Cyan
                Tone.Cloud -> TswaColors.Muted
            },
        modifier = modifier.size(size.dp),
    )
}

private fun Glyph.drawable(): Int =
    when (this) {
        Glyph.Sun -> R.drawable.wx_sun
        Glyph.Moon -> R.drawable.wx_moon
        Glyph.Partly -> R.drawable.wx_partly
        Glyph.PartlyNight -> R.drawable.wx_partly_night
        Glyph.Cloud -> R.drawable.wx_cloud
        Glyph.Fog -> R.drawable.wx_fog
        Glyph.Drizzle -> R.drawable.wx_drizzle
        Glyph.Rain -> R.drawable.wx_rain
        Glyph.Snow -> R.drawable.wx_snow
        Glyph.Thunder -> R.drawable.wx_thunder
    }
