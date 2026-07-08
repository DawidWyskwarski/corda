package com.example.corda.ui.screen.metronome.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.corda.R
import com.example.corda.ui.components.SingleClickIconButton
import com.example.corda.ui.screen.metronome.MetronomeViewModel
import com.example.corda.ui.screen.metronome.settings.components.BarCountRow
import com.example.corda.ui.screen.metronome.settings.components.BeatsInABarSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomeSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: MetronomeViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.metronome_settings)) },
                navigationIcon = { SingleClickIconButton(onClick = onBack) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.metronome_beats_in_a_bar),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(10.dp))

            BeatsInABarSelector(
                selectedBeats = state.beatsPerBar,
                onBeatsSelected = viewModel::setBeatsPerBar,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.metronome_muting_options),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = state.mutingEnabled,
                    onCheckedChange = viewModel::setMutingEnabled,
                )
            }

            AnimatedVisibility(
                visible = state.mutingEnabled,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                    BarCountRow(
                        label = stringResource(R.string.metronome_play),
                        value = state.playBars,
                        onValueChange = viewModel::setPlayBars,
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                    BarCountRow(
                        label = stringResource(R.string.metronome_mute),
                        value = state.muteBars,
                        onValueChange = viewModel::setMuteBars,
                    )
                }
            }
        }
    }
}