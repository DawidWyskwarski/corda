package com.example.corda.ui.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.corda.data.tuner.local.entities.Sound

private val ITEM_HEIGHT = 56.dp
private const val VISIBLE_ITEMS = 5

/**
 * Snap-to-center vertical picker for [Sound] items.
 *
 * Uses [contentPadding] equal to 2 item heights so the snapped
 * item (`firstVisibleItemIndex`) sits visually in the center.
 */
@Composable
fun VerticalNoteCarousel(
    sounds: List<Sound>,
    selectedSound: Sound,
    onSoundSelected: (Sound) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (sounds.isEmpty()) return

    val initialIndex = remember(selectedSound, sounds) {
        sounds.indexOfFirst { it.soundId == selectedSound.soundId }.coerceAtLeast(0)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    LaunchedEffect(Unit) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                if (index in sounds.indices) onSoundSelected(sounds[index])
            }
    }

    LazyColumn(
        state = listState,
        flingBehavior = rememberSnapFlingBehavior(listState),
        contentPadding = PaddingValues(vertical = ITEM_HEIGHT * 2),
        modifier = modifier.height(ITEM_HEIGHT * VISIBLE_ITEMS),
    ) {
        itemsIndexed(sounds, key = { _, s -> s.soundId }) { index, sound ->
            val isSelected = index == listState.firstVisibleItemIndex

            Surface(
                modifier = Modifier.height(ITEM_HEIGHT).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = sound.name,
                        style = if (isSelected) MaterialTheme.typography.headlineMedium
                                else MaterialTheme.typography.titleLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
