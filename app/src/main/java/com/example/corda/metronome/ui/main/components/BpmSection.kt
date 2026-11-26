package com.example.corda.metronome.ui.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.byValue
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
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
import com.example.corda.metronome.ui.MetronomeViewModel

@Composable
fun BpmSection(bpm: Int, onBpmChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    var isEditing by remember { mutableStateOf(false) }
    var hasFocused by remember { mutableStateOf(false) }
    val bpmTextFieldState = rememberTextFieldState(bpm.toString())
    val focusRequester = remember { FocusRequester() }
    val bpmRange = MetronomeViewModel.BPM_MIN.toFloat()..MetronomeViewModel.BPM_MAX.toFloat()
    val sliderState =
        rememberSliderState(
            value = bpm.toFloat(),
            trackRange = bpmRange,
        )
    sliderState.value = bpm.toFloat()

    // Keep text in sync with external BPM changes (e.g. slider) while not editing
    LaunchedEffect(bpm) {
        if (!isEditing) {
            bpmTextFieldState.setTextAndPlaceCursorAtEnd(bpm.toString())
        }
    }

    // Autofocus the field when editing begins
    LaunchedEffect(isEditing) {
        if (isEditing) focusRequester.requestFocus()
    }

    fun commit() {
        val parsed =
            bpmTextFieldState.text
                .toString()
                .toIntOrNull()
                ?.coerceIn(MetronomeViewModel.BPM_MIN, MetronomeViewModel.BPM_MAX)
                ?: bpm
        onBpmChange(parsed)
        bpmTextFieldState.setTextAndPlaceCursorAtEnd(parsed.toString())
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
                    state = bpmTextFieldState,
                    inputTransformation =
                    InputTransformation.byValue { _, proposed ->
                        proposed.filter { it.isDigit() }.take(3)
                    },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    onKeyboardAction = KeyboardActionHandler { commit() },
                    textStyle =
                    TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorator =
                    TextFieldDecorator { innerTextField ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            innerTextField()
                            Spacer(modifier = Modifier.height(2.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.primary,
                                thickness = 2.dp,
                            )
                        }
                    },
                    modifier =
                    Modifier
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
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        } else {
            Text(
                text = "$bpm BPM",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier =
                Modifier.clickable {
                    bpmTextFieldState.setTextAndPlaceCursorAtEnd(bpm.toString())
                    isEditing = true
                },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            state = sliderState,
            onValueChange = {
                sliderState.value = it
                onBpmChange(it.toInt())
            },
            modifier = Modifier.fillMaxWidth(),
            colors =
            SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        )
    }
}
