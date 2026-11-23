package com.robnugen.tswa.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robnugen.tswa.places.SavedPlaces
import com.robnugen.tswa.weather.Place

private const val DASH = 8f
private const val GAP = 6f

// "+ Save current place (Name)" in amber with a dashed amber edge, or a
// muted "✓ Saved — Name" once it is in the list.
@Composable
fun SavePlaceButton(
    current: Place,
    saved: List<Place>,
    onSave: () -> Unit,
) {
    val already = SavedPlaces.isSaved(saved, current)
    val color = if (already) TswaColors.Muted else TswaColors.Amber
    Box(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .dashedBorder(if (already) TswaColors.Line else TswaColors.Amber, 12.dp)
            .clickable(enabled = !already, role = Role.Button, onClick = onSave)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (already) "✓ Saved — ${current.shortName}" else "+ Save current place (${current.shortName})",
            color = color,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    radius: Dp,
): Modifier =
    drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(radius.toPx()),
            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH, GAP))),
        )
    }

// Each saved place: its short name, and the full name beneath when it
// says more.
@Composable
fun SavedPlacesCard(saved: List<Place>) {
    TswaCard("Saved places") {
        if (saved.isEmpty()) {
            Text(
                "No saved places yet. View a place on the Weather page, then save it with the button above.",
                color = TswaColors.Muted,
                fontSize = 14.sp,
            )
        }
        Column {
            saved.forEachIndexed { i, place ->
                if (i > 0) HorizontalDivider(thickness = 1.dp, color = TswaColors.Line)
                Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    Text(place.shortName, color = TswaColors.Text, fontSize = 16.sp)
                    if (place.fullName != place.shortName) {
                        Text(place.fullName, color = TswaColors.Muted, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}
