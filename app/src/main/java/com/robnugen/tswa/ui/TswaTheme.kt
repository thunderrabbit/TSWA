package com.robnugen.tswa.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val tswaColorScheme =
    darkColorScheme(
        primary = TswaColors.Amber,
        onPrimary = TswaColors.Background,
        secondary = TswaColors.Cyan,
        onSecondary = TswaColors.Background,
        background = TswaColors.Background,
        onBackground = TswaColors.Text,
        surface = TswaColors.Card,
        onSurface = TswaColors.Text,
        surfaceVariant = TswaColors.Navy,
        onSurfaceVariant = TswaColors.Muted,
        outline = TswaColors.Line,
    )

// Material 3, always dark, in the web UI's colours.
@Composable
fun TswaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = tswaColorScheme, content = content)
}
