package com.example.corda.ui.screen.tuner.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.corda.R
import com.example.corda.data.tuner.local.entities.MusicNote

@Composable
fun TuningNoteChip(
    musicNote: MusicNote,
    isSelected: Boolean,
    onClick: () -> Unit,
    isTuned: Boolean,
    modifier: Modifier = Modifier
) {
    // TODO might be a good idea to also add some stronger indicator that the note was tuned
    //  Something like colored outline on the chip or something

    FilterChip(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        horizontalArrangement = Arrangement.Center,
        selected = isSelected,
        onClick = onClick,
        label = {
            NoteLabel(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                musicNote = musicNote,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        trailingIcon = if (isTuned) {
            {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(R.string.tuned),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            null
        },
    )
}
