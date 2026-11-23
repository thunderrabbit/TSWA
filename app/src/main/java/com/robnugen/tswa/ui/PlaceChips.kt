package com.robnugen.tswa.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robnugen.tswa.places.SavedPlaces
import com.robnugen.tswa.weather.Place

// The saved places as a sideways row of chips under the search field,
// in the saved order. The chip for the place on screen has an amber edge.
@Composable
fun PlaceChips(
    saved: List<Place>,
    current: Place,
    onPick: (Place) -> Unit,
) {
    if (saved.isEmpty()) {
        Text(
            "Save the places you care about in Settings.",
            color = TswaColors.Muted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        )
        return
    }
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        saved.forEach { place ->
            val here = SavedPlaces.same(place, current)
            Surface(
                onClick = { onPick(place) },
                shape = CircleShape,
                color = TswaColors.Navy,
                border = BorderStroke(1.dp, if (here) TswaColors.Amber else TswaColors.Line),
            ) {
                Text(
                    place.shortName,
                    color = TswaColors.Text,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}
