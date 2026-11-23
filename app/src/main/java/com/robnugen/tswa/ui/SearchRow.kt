package com.robnugen.tswa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val fieldShape = RoundedCornerShape(10.dp)

// "Search a place…" and Go, at the top of the Weather tab. The keyboard's
// search key searches too, and the keyboard closes once a search starts.
@Composable
fun SearchRow(onSearch: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val go = {
        onSearch(query)
        keyboard?.hide()
        focus.clearFocus()
    }
    Row(
        Modifier.fillMaxWidth().padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            textStyle = TextStyle(color = TswaColors.Text, fontSize = 15.sp),
            cursorBrush = SolidColor(TswaColors.Amber),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { go() }),
            modifier =
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(TswaColors.Navy, fieldShape)
                    .border(1.dp, TswaColors.Line, fieldShape),
            decorationBox = { field ->
                Box(Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) Text("Search a place…", color = TswaColors.Muted, fontSize = 15.sp)
                    field()
                }
            },
        )
        Surface(onClick = go, shape = fieldShape, color = TswaColors.Amber, modifier = Modifier.height(44.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "Go",
                    color = TswaColors.OnAmber,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
