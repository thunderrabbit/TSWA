package com.robnugen.tswa.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robnugen.tswa.temperature.Scale
import com.robnugen.tswa.temperature.ScaleChoice
import com.robnugen.tswa.weather.Place

@Composable
fun SettingsPage(
    enabled: Set<Scale>,
    onToggle: (Scale) -> Unit,
    current: Place,
    saved: List<Place>,
    onSave: () -> Unit,
) {
    ScalesCard(enabled, onToggle)
    SavePlaceButton(current, saved, onSave)
    SavedPlacesCard(saved)
}

// One row per scale, in C, F, K, R order. The last scale that is on
// cannot be switched off, so its box is greyed out.
@Composable
private fun ScalesCard(
    enabled: Set<Scale>,
    onToggle: (Scale) -> Unit,
) {
    TswaCard("Scales") {
        Column {
            Scale.entries.forEachIndexed { index, scale ->
                if (index > 0) HorizontalDivider(thickness = 1.dp, color = TswaColors.Line)
                ScaleRow(
                    scale = scale,
                    on = scale in enabled,
                    canToggle = ScaleChoice.canToggle(enabled, scale),
                    onToggle = { onToggle(scale) },
                )
            }
        }
        Text(
            "Tap the big number to step through the scales that are on. At least one scale must stay on.",
            color = TswaColors.Muted,
            fontSize = 12.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        )
    }
}

@Composable
private fun ScaleRow(
    scale: Scale,
    on: Boolean,
    canToggle: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = on, enabled = canToggle, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            buildAnnotatedString {
                append(scale.fullName + " ")
                withStyle(SpanStyle(color = TswaColors.Muted)) { append(scale.symbol) }
            },
            color = TswaColors.Text,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        Checkbox(
            checked = on,
            onCheckedChange = null,
            enabled = canToggle,
            colors =
                CheckboxDefaults.colors(
                    checkedColor = TswaColors.Amber,
                    checkmarkColor = TswaColors.OnAmber,
                    uncheckedColor = TswaColors.Muted,
                    disabledCheckedColor = TswaColors.Amber.copy(alpha = DISABLED_ALPHA),
                ),
        )
    }
}

// How faint the last scale's box is: the web's disabled opacity.
private const val DISABLED_ALPHA = 0.35f
