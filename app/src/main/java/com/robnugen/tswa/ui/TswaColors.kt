package com.robnugen.tswa.ui

import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor

// The web UI's palette, written exactly as its CSS custom properties,
// so the native app looks like the one Rob already approved.
object TswaColors {
    val Background = css("#0B1220") // --bg
    val Card = css("#131E33") // --card
    val Navy = css("#182642") // --card2
    val Line = css("#24365A") // --line
    val Text = css("#EEF3FB") // --text
    val Muted = css("#9DB1CF") // --muted
    val Amber = css("#FFB454") // --accent
    val OnAmber = css("#1A1206") // text on an amber pill
    val Cyan = css("#57C7FF") // --accent2
    val Warn = css("#FF8A7A") // a search that found nothing or failed
    val TabBar = css("#0E1626") // .tabbar background

    private fun css(hex: String) = Color(AndroidColor.parseColor(hex))
}
