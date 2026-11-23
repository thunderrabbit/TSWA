package com.robnugen.tswa.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.robnugen.tswa.R

// The three pages, in tab-bar order.
enum class Tab(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
) {
    Weather(R.string.tab_weather, R.drawable.ic_tab_weather),
    Converter(R.string.tab_converter, R.drawable.ic_tab_converter),
    Settings(R.string.tab_settings, R.drawable.ic_tab_settings),
}
