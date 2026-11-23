package com.robnugen.tswa.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit

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
        // The Undo snackbar: navy, with the action in amber.
        inverseSurface = TswaColors.Navy,
        inverseOnSurface = TswaColors.Text,
        inversePrimary = TswaColors.Amber,
    )

// Plain Text uses bodyLarge, whose fixed 24sp line height spreads out
// small wrapped text. Let the line height follow the font size instead,
// as it did on the web page.
private val tswaTypography =
    Typography().let { base ->
        base.copy(bodyLarge = base.bodyLarge.copy(lineHeight = TextUnit.Unspecified))
    }

// Material 3, always dark, in the web UI's colours.
@Composable
fun TswaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = tswaColorScheme, typography = tswaTypography, content = content)
}
