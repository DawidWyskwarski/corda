package com.example.corda.ui.components

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
import com.example.corda.data.tuner.local.entities.Sound

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
    sounds: List<Sound>,
    modifier: Modifier = Modifier,
    selectedIndex: Int? = null,
    onIndexSelected: (Int?) -> Unit = {},
    tunedIndices: Set<Int> = emptySet(),
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
            items = sounds,
            key = { index, _ -> index },
        ) { index, sound ->
            TuningNoteChip(
                note = sound.name,
                isSelected = activeIndex == index,
                isTuned = index in tunedIndices,
                onClick = {
                    val newIndex = if (activeIndex == index) null else index
                    internalIndex = newIndex ?: -1
                    onIndexSelected(newIndex)
                },
            )
        }
    }
}
