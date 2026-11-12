package com.example.corda.tuner.ui.main.components

import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.corda.R

@Composable
fun EarModeToggleButton(
    isInEarMode: Boolean,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconToggleButton(
        checked = isInEarMode,
        onCheckedChange = { onToggle() },
        enabled = isEnabled,
        modifier = modifier
    ) {
        Crossfade(
            targetState = isInEarMode,
        ) {
            Icon(
                imageVector = if (it) Icons.AutoMirrored.Rounded.VolumeUp
                    else Icons.AutoMirrored.Rounded.VolumeOff,
                contentDescription = if (it) stringResource(R.string.tuner_main_ear_mode_disable_description)
                    else stringResource(R.string.tuner_main_ear_mode_enable_description),
            )
        }
    }
}
