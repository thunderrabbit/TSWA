package com.robnugen.tswa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robnugen.tswa.places.SavedPlaces
import com.robnugen.tswa.weather.Place
import sh.calvin.reorderable.ReorderableColumn

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

// The saved places: drag the handle to sort, swipe a row right to
// delete it. Rows only ever move under a finger on the handle, so
// scrolling the page cannot lift or shift one.
@Composable
fun SavedPlacesCard(
    saved: List<Place>,
    onMove: (from: Int, to: Int) -> Unit,
    onDelete: (Place) -> Unit,
) {
    TswaCard("Saved places") {
        if (saved.isEmpty()) {
            Text(
                "No saved places yet. View a place on the Weather page, then save it with the button above.",
                color = TswaColors.Muted,
                fontSize = 14.sp,
            )
        }
        ReorderableColumn(list = saved, onSettle = onMove) { index, place, dragging ->
            key(place) {
                Column {
                    if (index > 0 && !dragging) HorizontalDivider(thickness = 1.dp, color = TswaColors.Line)
                    PlaceRow(place, dragging, Modifier.draggableHandle(), onDelete)
                }
            }
        }
        if (saved.isNotEmpty()) {
            Text(
                "Drag the handle to sort · swipe a place right to delete it. " +
                    "Deleting every place starts onboarding again.",
                color = TswaColors.Muted,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceRow(
    place: Place,
    dragging: Boolean,
    handle: Modifier,
    onDelete: (Place) -> Unit,
) {
    val swipe =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.StartToEnd) onDelete(place)
                value == SwipeToDismissBoxValue.StartToEnd
            },
        )
    SwipeToDismissBox(
        state = swipe,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().background(DELETE_RED).padding(start = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text("Delete", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        },
    ) {
        Surface(
            color = TswaColors.Card,
            shadowElevation = if (dragging) 8.dp else 0.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "⠿",
                    color = if (dragging) TswaColors.Amber else TswaColors.Muted,
                    fontSize = 20.sp,
                    modifier =
                        handle
                            .semantics { contentDescription = "Drag to sort ${place.shortName}" }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                )
                Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
                    Text(place.shortName, color = TswaColors.Text, fontSize = 16.sp)
                    if (place.fullName != place.shortName) {
                        Text(place.fullName, color = TswaColors.Muted, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}

// The web version's delete red.
private val DELETE_RED = Color(android.graphics.Color.parseColor("#A33327"))
