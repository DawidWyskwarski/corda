package com.example.corda.tuner.ui.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.corda.tuner.ui.components.NoteLabel
import com.example.corda.tuner.ui.components.TuningSoundGrid
import com.example.corda.tuner.ui.main.data.TunerReading
import com.example.corda.tuner.ui.main.data.TuningTarget
import java.util.Locale

@Composable
fun LoadedContent(
    loadedTuningTarget: TuningTarget,
    tunerReading: TunerReading,
    isInEarMode: Boolean,
    currentlyClickedIndex: Int?,
    tunedIndices: Set<Int>,
    onItemClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = !isInEarMode,
            enter = slideInVertically { -it } + expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = slideOutVertically { -it } + shrinkVertically() + fadeOut(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tunerReading.frequency?.let {
                        String.format(Locale.getDefault(), "%.2f Hz", it)
                    } ?: "",
                    style = MaterialTheme.typography.labelMedium,
                )

                Box {
                    PitchArc(
                        centsOff = tunerReading.centsOff,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    NoteLabel(
                        musicNote = tunerReading.note,
                        style = MaterialTheme.typography.displayLargeEmphasized,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }

        when (loadedTuningTarget) {
            is TuningTarget.Standard -> {
                TuningSoundGrid(
                    musicNotes = loadedTuningTarget.notes,
                    selectedIndex = currentlyClickedIndex ?: tunerReading.noteIndex,
                    onIndexSelected = onItemClicked,
                    tunedIndices = tunedIndices,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                )
            }
            is TuningTarget.Chromatic -> {
                if (isInEarMode) {
                    EarModeChromaticContent(
                        allNotes = loadedTuningTarget.notes,
                        onPlayToggle = onItemClicked, // TODO this needs to be changed. It might be better to have 2 functions one to select and other to play. To be investigated.
                        modifier = Modifier
                            .weight(1f),
                    )
                }
            }
        }
    }
}
