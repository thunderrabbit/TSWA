package com.robnugen.tswa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robnugen.tswa.BuildConfig
import com.robnugen.tswa.R

// Where the converter starts when there is no weather to seed it.
private const val DEFAULT_CONVERTER_C = 22.2

@Composable
fun TswaApp(state: AppState) {
    var current by rememberSaveable { mutableStateOf(Tab.Weather) }
    var converterC by rememberSaveable { mutableDoubleStateOf(DEFAULT_CONVERTER_C) }
    // Each tab keeps its own scroll position, as the web page did.
    val scrolls = Tab.entries.associateWith { rememberScrollState() }
    Column(
        Modifier
            .fillMaxSize()
            .background(TswaColors.Background),
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrolls.getValue(current))
                .padding(horizontal = 14.dp, vertical = 16.dp),
        ) {
            Header()
            Spacer(Modifier.height(10.dp))
            when (current) {
                Tab.Converter -> {
                    ConverterPage(
                        celsius = converterC,
                        scale = state.scale,
                        enabled = state.enabledScales,
                        onStep = state::stepScale,
                        onSet = { converterC = it },
                    )
                }

                Tab.Settings -> {
                    SettingsPage(enabled = state.enabledScales, onToggle = state::toggleScale)
                }

                else -> {
                    TabPage(current)
                }
            }
            Footer()
        }
        TabBar(current, onSelect = { current = it })
    }
}

// "TSWA" with the WA in amber, the full name, and the version, as on
// the web page. The version comes from the binary itself.
@Composable
private fun Header() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            buildAnnotatedString {
                append("TS")
                withStyle(SpanStyle(color = TswaColors.Amber)) { append("WA") }
            },
            color = TswaColors.Text,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        Text(stringResource(R.string.full_name), color = TswaColors.Muted, fontSize = 14.sp)
        Text(
            "v" + BuildConfig.VERSION_NAME,
            color = TswaColors.Muted,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

// The line at the foot of every page, as on the web version.
@Composable
private fun Footer() {
    Text(
        stringResource(R.string.footer),
        color = TswaColors.Muted,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    )
}

// An empty page that only says which tab it is; later issues fill them.
@Composable
private fun TabPage(tab: Tab) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Text(
            stringResource(tab.title),
            color = TswaColors.Text,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// The web tab bar: equal-width tabs, the chosen one amber with an amber
// line along its top edge.
@Composable
private fun TabBar(
    current: Tab,
    onSelect: (Tab) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(TswaColors.TabBar)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(thickness = 1.dp, color = TswaColors.Line)
        Row(Modifier.fillMaxWidth().selectableGroup()) {
            Tab.entries.forEach { tab ->
                TabButton(tab, selected = tab == current, onClick = { onSelect(tab) })
            }
        }
    }
}

@Composable
private fun RowScope.TabButton(
    tab: Tab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) TswaColors.Amber else TswaColors.Muted
    Column(
        Modifier
            .weight(1f)
            .heightIn(min = 56.dp)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(if (selected) TswaColors.Amber else Color.Transparent),
        )
        Column(
            Modifier.padding(top = 6.dp, bottom = 11.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(
                painterResource(tab.icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp),
            )
            Text(
                stringResource(tab.title),
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
