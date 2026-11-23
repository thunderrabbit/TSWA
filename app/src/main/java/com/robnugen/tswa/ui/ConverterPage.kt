package com.robnugen.tswa.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robnugen.tswa.temperature.Scale
import com.robnugen.tswa.temperature.Verdicts
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

// How far the slider reaches from where a drag began, and what one
// step of it is worth, as on the web version.
private const val SLIDER_STEPS = 50
private const val C_PER_STEP = 0.2

// The big number scales with the screen, like the web's
// clamp(56px, 17vw, 84px).
private const val BIG_NUMBER_WIDTH_SHARE = 0.17f
private const val BIG_NUMBER_MIN_SP = 56f
private const val BIG_NUMBER_MAX_SP = 84f

// Non-breaking spaces inside each formula, so a line only breaks
// between formulas.
private const val FORMULAS =
    "F\u00A0=\u00A0C\u00A0×\u00A09/5\u00A0+\u00A032  ·  " +
        "K\u00A0=\u00A0C\u00A0+\u00A0273.15  ·  " +
        "°R\u00A0=\u00A0(C\u00A0+\u00A0273.15)\u00A0×\u00A09/5"

@Composable
fun ConverterPage(
    celsius: Double,
    scale: Scale,
    enabled: Set<Scale>,
    onStep: () -> Unit,
    onSet: (Double) -> Unit,
) {
    TswaCard("Temperature converter") {
        Pills(scale, enabled)
        BigNumber(scale.format(celsius), onStep)
        Hint(stepHint(enabled))
        VerdictLines(celsius)
        CentreSlider(celsius, onSet)
        Presets(onSet)
    }
    LandmarksCard(scale)
}

// One pill per enabled scale; the one shown is lit. Indicators only.
@Composable
private fun Pills(
    scale: Scale,
    enabled: Set<Scale>,
) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Scale.entries.filter { it in enabled }.forEach { s ->
            val on = s == scale
            Surface(
                shape = CircleShape,
                color = if (on) TswaColors.Amber else TswaColors.Navy,
                border = BorderStroke(1.dp, if (on) TswaColors.Amber else TswaColors.Line),
            ) {
                Text(
                    s.symbol,
                    color = if (on) TswaColors.OnAmber else TswaColors.Muted,
                    fontSize = 14.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                )
            }
        }
    }
}

// Tap to step to the next enabled scale; amber while pressed.
@Composable
private fun BigNumber(
    text: String,
    onStep: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // Sized from the screen's width in dp, so a large font setting cannot
    // push the number past the screen edges.
    val share = LocalConfiguration.current.screenWidthDp * BIG_NUMBER_WIDTH_SHARE
    val width = share.coerceIn(BIG_NUMBER_MIN_SP, BIG_NUMBER_MAX_SP)
    val size = with(LocalDensity.current) { width.dp.toSp() }
    Text(
        text,
        color = if (pressed) TswaColors.Amber else TswaColors.Text,
        fontSize = size,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClickLabel = "Step to the next temperature scale",
                    onClick = onStep,
                ),
    )
}

@Composable
private fun Hint(text: String) {
    Text(
        text,
        color = TswaColors.Muted,
        fontSize = 12.5.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
    )
}

@Composable
private fun VerdictLines(celsius: Double) {
    val verdict = Verdicts.forCelsius(celsius)
    Column(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            verdict.title,
            color = TswaColors.Amber,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            verdict.detail,
            color = TswaColors.Muted,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

// Pinned to the centre: drag right for warmer, left for cooler, up to
// SLIDER_STEPS steps from where the drag began. On release it springs
// back to the centre and the value stays where the drag left it.
@Composable
private fun CentreSlider(
    celsius: Double,
    onSet: (Double) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val position = remember { Animatable(0f) }
    var dragBase by remember { mutableStateOf<Double?>(null) }
    var lastStep by remember { mutableIntStateOf(0) }
    Box(Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .width(2.dp)
                .height(12.dp)
                .background(TswaColors.Amber, RoundedCornerShape(2.dp)),
        )
        Slider(
            value = position.value,
            onValueChange = { value ->
                scope.launch { position.snapTo(value) }
                val base = dragBase ?: celsius.also { dragBase = it }
                val step = value.roundToInt()
                if (step != lastStep) {
                    lastStep = step
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSet(max(Scale.ABSOLUTE_ZERO_C, base + step * C_PER_STEP))
                }
            },
            onValueChangeFinished = {
                dragBase = null
                lastStep = 0
                scope.launch { position.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
            },
            valueRange = -SLIDER_STEPS.toFloat()..SLIDER_STEPS.toFloat(),
            colors =
                SliderDefaults.colors(
                    thumbColor = TswaColors.Amber,
                    activeTrackColor = TswaColors.Amber,
                    inactiveTrackColor = TswaColors.Line,
                ),
            modifier =
                Modifier.semantics {
                    contentDescription = "Slide to adjust the temperature; it springs back to the centre"
                },
        )
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("◀ slide: cooler", color = TswaColors.Muted, fontSize = 12.sp)
        Text("warmer ▶", color = TswaColors.Muted, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Presets(onSet: (Double) -> Unit) {
    FlowRow(
        Modifier.fillMaxWidth().padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Verdicts.presets.forEach { preset ->
            Surface(
                onClick = { onSet(preset.celsius) },
                shape = CircleShape,
                color = TswaColors.Navy,
                border = BorderStroke(1.dp, TswaColors.Line),
            ) {
                Text(
                    preset.label,
                    color = TswaColors.Text,
                    fontSize = 13.5.sp,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                )
            }
        }
    }
}

// Collapsed by default, as the web's <details> was.
@Composable
private fun LandmarksCard(scale: Scale) {
    var open by rememberSaveable { mutableStateOf(false) }
    TswaCard("Landmarks") {
        Text(
            (if (open) "▾ " else "▸ ") + "Temperatures worth knowing",
            color = TswaColors.Cyan,
            fontSize = 14.sp,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = { open = !open })
                    .padding(vertical = 6.dp),
        )
        if (open) {
            Column(Modifier.padding(top = 4.dp)) {
                Verdicts.landmarks.forEach { landmark ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            landmark.label,
                            color = TswaColors.Text,
                            fontSize = 13.5.sp,
                            modifier = Modifier.weight(1f),
                        )
                        Text(scale.format(landmark.celsius), color = TswaColors.Muted, fontSize = 13.5.sp)
                    }
                    HorizontalDivider(thickness = 1.dp, color = TswaColors.Line)
                }
            }
        }
        Hint(FORMULAS)
    }
}
