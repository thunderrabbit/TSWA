package com.robnugen.tswa.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robnugen.tswa.R
import com.robnugen.tswa.temperature.Scale
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
                    .width(62.dp)
                    .background(if (i == 0) TswaColors.Card else Color.Transparent, RoundedCornerShape(12.dp))
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(hour.label, color = TswaColors.Muted, fontSize = 12.sp)
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

// Today, then weekdays: icon, rain chance, low, range bar, high.
@Composable
fun TenDays(
    forecast: Forecast,
    control: ScaleControl,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
        shape = RoundedCornerShape(16.dp),
        color = TswaColors.Card,
        border = BorderStroke(1.dp, TswaColors.Line),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            WeatherView.days(forecast).forEachIndexed { i, day ->
                if (i > 0) HorizontalDivider(thickness = 1.dp, color = TswaColors.Line)
                Row(
                    Modifier.fillMaxWidth().height(52.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(day.name, color = TswaColors.Text, fontSize = 15.sp, modifier = Modifier.width(80.dp))
                    GlyphIcon(day.glyph, size = 20)
                    Text(
                        rainLabel(day.rainPercent),
                        color = TswaColors.Cyan,
                        fontSize = 12.sp,
                        modifier = Modifier.width(36.dp),
                    )
                    Temperature(day.lowC, control, Modifier.width(64.dp), style = dayLowStyle)
                    RangeBar(day.barStart, day.barWidth)
                    Temperature(day.highC, control, Modifier.width(64.dp), style = dayHighStyle)
                }
            }
        }
    }
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
        Glyph.Cloud -> R.drawable.wx_cloud
        Glyph.Fog -> R.drawable.wx_fog
        Glyph.Drizzle -> R.drawable.wx_drizzle
        Glyph.Rain -> R.drawable.wx_rain
        Glyph.Snow -> R.drawable.wx_snow
        Glyph.Thunder -> R.drawable.wx_thunder
    }
