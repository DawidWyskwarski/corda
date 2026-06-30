package com.example.corda.ui.screen.metronome.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.corda.ui.screen.metronome.MetronomeViewModel

@Composable
fun BpmSection(
    bpm: Int,
    onBpmChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val onBackground = MaterialTheme.colorScheme.onBackground

    var isEditing by remember { mutableStateOf(false) }
    var bpmText by remember { mutableStateOf(bpm.toString()) }
    var hasFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Keep text in sync with external BPM changes (e.g. slider) while not editing
    LaunchedEffect(bpm) {
        if (!isEditing) bpmText = bpm.toString()
    }

    // Autofocus the field when editing begins
    LaunchedEffect(isEditing) {
        if (isEditing) focusRequester.requestFocus()
    }

    fun commit() {
        val parsed = bpmText.toIntOrNull()
            ?.coerceIn(MetronomeViewModel.BPM_MIN, MetronomeViewModel.BPM_MAX)
            ?: bpm
        onBpmChange(parsed)
        bpmText = parsed.toString()
        isEditing = false
        hasFocused = false
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isEditing) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = bpmText,
                    onValueChange = { bpmText = it.filter { c -> c.isDigit() }.take(3) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { commit() }),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = onBackground,
                        textAlign = TextAlign.Center,
                    ),
                    cursorBrush = SolidColor(primaryColor),
                    decorationBox = { innerTextField ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            innerTextField()
                            Spacer(modifier = Modifier.height(2.dp))
                            HorizontalDivider(color = primaryColor, thickness = 2.dp)
                        }
                    },
                    modifier = Modifier
                        .width(80.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                hasFocused = true
                            } else if (hasFocused) {
                                commit()
                            }
                        },
                )
                Text(
                    text = " BPM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = onBackground,
                )
            }
        } else {
            Text(
                text = "$bpm BPM",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = onBackground,
                modifier = Modifier.clickable {
                    bpmText = bpm.toString()
                    isEditing = true
                },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = bpm.toFloat(),
            onValueChange = { onBpmChange(it.toInt()) },
            valueRange = MetronomeViewModel.BPM_MIN.toFloat()..MetronomeViewModel.BPM_MAX.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor,
                inactiveTrackColor = containerColor,
            ),
        )
    }
}