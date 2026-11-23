package com.robnugen.tswa.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import com.robnugen.tswa.temperature.Scale

// The scale every temperature is shown in, and the tap that steps it.
class ScaleControl(
    val scale: Scale,
    val onStep: () -> Unit,
)

// A temperature in the current scale that steps the scale when tapped,
// amber while pressed (the web's [data-temp]). "–" while unknown.
@Composable
fun Temperature(
    celsius: Double?,
    control: ScaleControl,
    modifier: Modifier = Modifier,
    prefix: String = "",
    style: TextStyle = TextStyle(color = TswaColors.Text),
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Text(
        prefix + (celsius?.let { control.scale.format(it) } ?: "–"),
        style = if (pressed) style.copy(color = TswaColors.Amber) else style,
        maxLines = 1,
        softWrap = false,
        modifier =
            modifier.clickable(
                interactionSource = interaction,
                indication = null,
                enabled = celsius != null,
                role = Role.Button,
                onClickLabel = "Step to the next temperature scale",
                onClick = control.onStep,
            ),
    )
}
