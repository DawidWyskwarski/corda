package com.example.corda.ui.screen.tuner.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.corda.data.tuner.local.entities.Sound

/**
 * STANDARD ear mode: grid of note chips. Tap a chip to play its reference tone until you tap
 * it again (or choose another string); stopping is also handled when ear mode is turned off.
 */
@Composable
fun EarModeStandardContent(
    sounds: List<Sound>,
    playingIndex: Int?,
    onNoteToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(top = 32.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(
            items = sounds,
            key = { _, sound -> sound.soundId },
        ) { index, sound ->
            TuningNoteChip(
                pitchClass = sound.name,
                octave = sound.octave,
                isSelected = playingIndex == index,
                isTuned = false,
                onClick = { onNoteToggle(index) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
