package com.example.corda.ui.screen.tuner.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.corda.data.tuner.local.entities.MusicNote

/**
 * CHROMATIC ear mode: vertical note carousel plus play / stop for the snapped note.
 */
@Composable
fun EarModeChromaticContent(
    allNotes: List<MusicNote>,
    onPlayToggle: (Int) -> Unit, // Should we use Ints ?
    modifier: Modifier = Modifier,
) {
    if (allNotes.isEmpty()) return

    var isPlaying by remember { mutableStateOf(false) } // TODO this looks like a bad idea. Backend playback state and UI state may be misaligned.
    var selectedIndex by remember { mutableIntStateOf(0) }

    // TODO change every place to have the modifier as the last argument

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        VerticalNoteCarousel(
            notes = allNotes,
            onSoundSelected = { index -> selectedIndex = index },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        PlayNoteButton(
            isPlaying = isPlaying,
            onClick = {
                onPlayToggle(selectedIndex)
                isPlaying = !isPlaying
            },
        )
    }
}
