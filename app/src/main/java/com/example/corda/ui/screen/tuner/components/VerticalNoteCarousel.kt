package com.example.corda.ui.screen.tuner.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.corda.data.tuner.local.entities.MusicNote

private val ITEM_HEIGHT = 56.dp
private const val VISIBLE_ITEMS = 5

//TODO regenerate docs
@Composable
fun VerticalNoteCarousel(
    modifier: Modifier = Modifier,
    notes: List<MusicNote>,
    onSoundSelected: (Int) -> Unit,
) {
    if (notes.isEmpty()) return

    val initialIndex = remember(notes) { notes.size / 2 }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                onSoundSelected( index )
            }
    }

    LazyColumn(
        state = listState,
        flingBehavior = rememberSnapFlingBehavior(listState),
        contentPadding = PaddingValues(vertical = ITEM_HEIGHT * 2),
        modifier = modifier.height(ITEM_HEIGHT * VISIBLE_ITEMS),
    ) {
        itemsIndexed(
            notes
        ) { index, sound ->
            val isSelected = index == listState.firstVisibleItemIndex

            Surface(
                modifier = Modifier.height(ITEM_HEIGHT).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    NoteLabel(
                        musicNote = sound,
                        style = if (isSelected) MaterialTheme.typography.headlineMedium
                        else MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }
}
