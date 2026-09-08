package com.example.corda.ui.screen.tuner.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import com.example.corda.data.tuner.local.entities.MusicNote

@Composable
fun NoteLabel(
    musicNote: MusicNote?,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (musicNote != null)
            noteLabelAnnotation(musicNote.name, musicNote.octave, style)
        else
            AnnotatedString(""),
        modifier = modifier,
        style = style,
    )
}

fun noteLabelAnnotation( // TODO this needs to be redesigned
    name: String,
    octave: Int,
    baseStyle: TextStyle,
    subscriptScale: Float = 0.65f,
): AnnotatedString = buildAnnotatedString {
    append(name)
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

fun annotateMusicNotes(
    musicNotes: List<MusicNote>,
    baseStyle: TextStyle,
    separator: String = " ",
): AnnotatedString = buildAnnotatedString {
    musicNotes.forEachIndexed { index, note ->
        if (index > 0) append(separator)
        append(noteLabelAnnotation(note.name, note.octave, baseStyle))
    }
}
