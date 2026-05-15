package com.example.corda.ui.screen.tuner.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import com.example.corda.data.tuner.local.entities.Sound

@Composable
fun NoteLabel(
    pitchClass: String,
    octave: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = Color.Unspecified,
    subscriptScale: Float = 0.65f,
) {
    Text(
        text = noteLabelAnnotated(pitchClass, octave, style, subscriptScale),
        modifier = modifier,
        style = style,
        color = color,
    )
}

fun soundsPreviewAnnotated(
    sounds: List<Sound>,
    baseStyle: TextStyle,
    subscriptScale: Float = 0.65f,
): AnnotatedString = buildAnnotatedString {
    sounds.forEachIndexed { index, sound ->
        if (index > 0) append(" ")
        append(noteLabelAnnotated(sound.name, sound.octave, baseStyle, subscriptScale))
    }
}

fun noteLabelAnnotated(
    pitchClass: String,
    octave: Int,
    baseStyle: TextStyle,
    subscriptScale: Float = 0.65f,
): AnnotatedString = buildAnnotatedString {
    append(pitchClass)
    withStyle(
        SpanStyle(
            fontSize = baseStyle.fontSize * subscriptScale,
            fontWeight = FontWeight.Normal,
            baselineShift = BaselineShift(-0.25f),
        ),
    ) {
        append(octave.toString())
    }
}

@Composable
fun NoteLabel(
    sound: Sound,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = Color.Unspecified,
    subscriptScale: Float = 0.65f,
) {
    NoteLabel(
        pitchClass = sound.name,
        octave = sound.octave,
        modifier = modifier,
        style = style,
        color = color,
        subscriptScale = subscriptScale,
    )
}
