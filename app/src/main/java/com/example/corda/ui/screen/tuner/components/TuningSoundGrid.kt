package com.example.corda.ui.screen.tuner.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.corda.data.tuner.local.entities.MusicNote

/**
 * 2-column grid of [TuningNoteChip]s with index-based selection.
 *
 * @param selectedIndex externally controlled selected index, or `null` for no selection.
 *   When `null` is passed, the grid manages its own internal selection state.
 * @param onIndexSelected called with the selected index (or `null` on deselect)
 * @param tunedIndices set of indices that have been successfully tuned
 */
@Composable
fun TuningSoundGrid(
    musicNotes: List<MusicNote>,
    selectedIndex: Int?,
    onIndexSelected: (Int) -> Unit,
    tunedIndices: Set<Int>,
    modifier: Modifier = Modifier,
) {
    var internalIndex by remember { mutableIntStateOf(-1) }
    val activeIndex = selectedIndex ?: internalIndex.takeIf { it >= 0 }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(top = 32.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(
            items = musicNotes,
            key = { index, _ -> index },
        ) { index, sound ->
            TuningNoteChip(
                musicNote = sound,
                isSelected = activeIndex == index,
                isTuned = index in tunedIndices,
                onClick = { onIndexSelected(index) },
            )
        }
    }
}
