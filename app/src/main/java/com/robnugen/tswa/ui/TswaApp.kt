package com.robnugen.tswa.ui

import android.util.Log
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.robnugen.tswa.places.LegacyReader
import com.robnugen.tswa.weather.Place
import kotlinx.coroutines.launch

@Composable
fun TswaApp(state: AppState) {
    var current by rememberSaveable { mutableStateOf(Tab.Weather) }
    // Each tab keeps its own scroll position, as the web page did.
    val scrolls = Tab.entries.associateWith { rememberScrollState() }
    // Fetch the weather once at start: Adelaide until saved places and
    // onboarding (mg #897, #899) choose the place.
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        // The WebView version's saved places come over once (mg #897).
        state.importLegacyOnce {
            LegacyReader(context).read().also { found ->
                val places = found?.get("tswa-places")
                Log.i("TSWA", "Old WebView storage: ${found?.keys}; tswa-places ${places?.length ?: 0} chars")
            }
        }
        state.weather.load(Place.ADELAIDE)
    }
    // The converter starts from the weather's temperature.
    val currentC =
        state.weather.weather
            ?.forecast
            ?.current
            ?.temperatureC
    LaunchedEffect(currentC) { currentC?.let(state::seedConverter) }
    // A swiped-away place can be brought back for a few seconds.
    val snackbar = remember { SnackbarHostState() }
    val deleted = state.lastDeleted
    LaunchedEffect(deleted) {
        if (deleted != null) {
            val name = deleted.second.shortName
            val answer = snackbar.showSnackbar("Deleted $name", "Undo", duration = SnackbarDuration.Short)
            if (answer == SnackbarResult.ActionPerformed) state.undoDelete() else state.forgetDeleted()
        }
    }
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
            PageContent(current, state)
            Footer()
        }
        SnackbarHost(snackbar)
        TabBar(current, onSelect = { current = it })
    }
}

// The page for the chosen tab.
@Composable
private fun PageContent(
    current: Tab,
    state: AppState,
) {
    val scope = rememberCoroutineScope()
    when (current) {
        Tab.Converter -> {
            ConverterPage(
                celsius = state.converterC,
                scale = state.scale,
                enabled = state.enabledScales,
                onStep = state::stepScale,
                onSet = state::setConverter,
            )
        }

        Tab.Weather -> {
            SearchRow(onSearch = { query -> scope.launch { state.weather.search(query) } })
            PlaceChips(
                saved = state.savedPlaces,
                current = state.weather.currentPlace,
                onPick = { place -> scope.launch { state.weather.load(place) } },
            )
            WeatherPage(
                weather = state.weather,
                control = ScaleControl(state.scale, state::stepScale),
                enabled = state.enabledScales,
            )
        }

        Tab.Settings -> {
            SettingsPage(
                enabled = state.enabledScales,
                onToggle = state::toggleScale,
                current = state.weather.currentPlace,
                places =
                    PlaceActions(
                        saved = state.savedPlaces,
                        onSave = state::saveCurrentPlace,
                        onMove = state::movePlace,
                        onDelete = state::deletePlace,
                    ),
            )
        }
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
