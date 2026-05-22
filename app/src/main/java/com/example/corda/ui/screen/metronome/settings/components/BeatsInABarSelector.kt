package com.example.corda.ui.screen.metronome.settings.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

private const val BEATS = 12
// To simulate infinite scroll
private const val VIRTUAL_COUNT = BEATS * 1000

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BeatsInABarSelector(
    selectedBeats: Int,
    onBeatsSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedBackground = MaterialTheme.colorScheme.primaryContainer
    val selectedContent = MaterialTheme.colorScheme.onPrimaryContainer
    val unselectedContent = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)

    val itemSizeDp = 56.dp

    //Starting around the middle of the virtual list
    val centerBase = VIRTUAL_COUNT / 2
    val initialIndex = centerBase + (selectedBeats - 1)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    // Snapping the nearest item to the center
    val snapFlingBehavior = rememberSnapFlingBehavior(
        snapLayoutInfoProvider = remember(listState) {
            SnapLayoutInfoProvider(lazyListState = listState, snapPosition = SnapPosition.Center)
        }
    )

    // Notify parent when scroll settles and find the visually centered item
    LaunchedEffect(listState) {
        var wasScrolling = false
        snapshotFlow { listState.isScrollInProgress }
            .collect { isScrolling ->
                if (!isScrolling && wasScrolling) {
                    val layoutInfo = listState.layoutInfo

                    // Calculating center of view
                    val viewportCenter =
                        (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

                    // For each visible item, calculating its absolute distance from the center of view and selecting the one with the lowest score
                    val centeredItem = layoutInfo.visibleItemsInfo.minByOrNull {
                        abs(it.offset + it.size / 2 - viewportCenter)
                    }
                    centeredItem?.let { onBeatsSelected((it.index % BEATS) + 1) }
                }
                wasScrolling = isScrolling
            }
    }

    // Scroll to center when selectedBeats changes externally (e.g. from a tap)
    LaunchedEffect(selectedBeats) {
        val layoutInfo = listState.layoutInfo

        // Calculating center of view
        val viewportCenter =
            (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

        // For each visible item, calculating its absolute distance from the center of view and selecting the one with the lowest score
        val currentCenteredItem = layoutInfo.visibleItemsInfo.minByOrNull {
            abs(it.offset + it.size / 2 - viewportCenter)
        }
        val currentBeat = currentCenteredItem?.let { (it.index % BEATS) + 1 }
        if (currentBeat != selectedBeats) {
            val currentIndex = currentCenteredItem?.index ?: listState.firstVisibleItemIndex
            val currentBeatFallback = (currentIndex % BEATS) + 1

            // Adding BEATS to ensure a positive value
            val offset = (selectedBeats - currentBeatFallback + BEATS) % BEATS
            val targetIndex = if (offset <= BEATS / 2) {
                currentIndex + offset
            } else {
                currentIndex - (BEATS - offset)
            }
            listState.animateScrollToItem(targetIndex.coerceIn(0, VIRTUAL_COUNT - 1), 0)
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val horizontalPadding = (maxWidth - itemSizeDp) / 2

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            flingBehavior = snapFlingBehavior,
        ) {
            items(VIRTUAL_COUNT) { index ->
                val beat = (index % BEATS) + 1
                val isSelected = beat == selectedBeats
                Box(
                    modifier = Modifier
                        .size(itemSizeDp)
                        .clip(CircleShape)
                        .background(if (isSelected) selectedBackground else Color.Transparent)
                        .clickable { onBeatsSelected(beat) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$beat",
                        fontSize = 22.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) selectedContent else unselectedContent,
                    )
                }
            }
        }
    }
}
