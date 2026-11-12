package com.example.corda.tuner.ui.main.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.corda.R

/**
 * Toggle control for starting / stopping chromatic reference tone playback in ear mode.
 */
@Composable
fun PlayNoteButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledIconToggleButton(
        checked = isPlaying,
        onCheckedChange = { onClick() },
        modifier = modifier.size(72.dp),
        shape = CircleShape,
    ) {
        Crossfade(targetState = isPlaying, label = "playStopIcon") { playing ->
            Icon(
                imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (playing) {
                    stringResource(R.string.tuner_main_ear_mode_stop_note_description)
                } else {
                    stringResource(R.string.tuner_main_ear_mode_play_note_description)
                },
                modifier = Modifier.size(36.dp),
            )
        }
    }
}
