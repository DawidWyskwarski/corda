package com.example.corda.ui.screen.tuner.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.corda.data.tuner.local.entities.Sound

/**
 * CHROMATIC ear mode: vertical note carousel plus play / stop for the snapped note.
 */
@Composable
fun EarModeChromaticContent(
    allNotes: List<Sound>,
    selectedNote: Sound,
    isPlaying: Boolean,
    onNoteSelected: (Sound) -> Unit,
    onPlayToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (allNotes.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        VerticalNoteCarousel(
            sounds = allNotes,
            selectedSound = selectedNote,
            onSoundSelected = onNoteSelected,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        PlayNoteButton(
            isPlaying = isPlaying,
            onClick = onPlayToggle,
        )
    }
}
