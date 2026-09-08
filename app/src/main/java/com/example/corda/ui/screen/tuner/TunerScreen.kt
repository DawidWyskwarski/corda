package com.example.corda.ui.screen.tuner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.R
import com.example.corda.domain.tuner.TuningMode
import com.example.corda.ui.components.NavigationPill
import com.example.corda.ui.components.UserInfo
import com.example.corda.ui.screen.tuner.components.EarModeChromaticContent
import com.example.corda.ui.screen.tuner.components.NoteLabel
import com.example.corda.ui.screen.tuner.components.PitchArc
import com.example.corda.ui.screen.tuner.components.TuningSoundGrid
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TunerScreen(
    modifier: Modifier = Modifier,
    viewModel: TunerViewModel,
    openDrawer: () -> Unit,
    openSettings: () -> Unit
) {
    val selectedTuning by viewModel.selectedTuning.collectAsStateWithLifecycle()
    val selectedMode by viewModel.tuningMode.collectAsStateWithLifecycle()
    val tunerState by viewModel.tunerState.collectAsStateWithLifecycle()
    val isEarModeEnabled by viewModel.isEarModeEnabled.collectAsStateWithLifecycle()
    val currentlyClickedIndex by viewModel.currentlyClickedIndex.collectAsStateWithLifecycle()
    val tunedIndices by viewModel.tunedIndices.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LifecycleResumeEffect(hasPermission, isEarModeEnabled) {
        if (hasPermission && !isEarModeEnabled) {
            viewModel.startListening()
        }
        onPauseOrDispose {
            viewModel.stopListening()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    NavigationPill(
                        text = when {
                            selectedMode == TuningMode.CHROMATIC -> stringResource(R.string.chromatic_mode)
                            selectedTuning != null -> selectedTuning!!.tuningName
                            else -> "No tunings found"
                        },
                        supportingText = when {
                            selectedMode == TuningMode.CHROMATIC -> ""
                            else -> selectedTuning?.instrumentName ?: ""
                        },
                        onClick = openSettings
                    )
                },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Rounded.Menu, stringResource(R.string.open_drawer))
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = isEarModeEnabled,
                        onCheckedChange = { viewModel.toggleEarMode() },
                        enabled = !(selectedMode == TuningMode.STANDARD && selectedTuning == null)
                    ) {
                        Icon(
                            imageVector = if (isEarModeEnabled) Icons.AutoMirrored.Rounded.VolumeUp
                                else Icons.AutoMirrored.Rounded.VolumeOff,
                            contentDescription = if (isEarModeEnabled) stringResource(R.string.disable_ear_mode)
                                else stringResource(R.string.enable_ear_mode),
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(top = 48.dp)
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (selectedMode) {

                TuningMode.STANDARD -> {
                    if (selectedTuning == null) {
                        UserInfo(
                            modifier = Modifier.fillMaxSize(),
                            mainText = "No tunings found",
                            supportingText = "Please select or add a tuning"
                        )
                    } else {

                        AnimatedVisibility(
                            visible = !isEarModeEnabled,
                            enter = slideInVertically { -it } + expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                            exit = slideOutVertically { -it } + shrinkVertically() + fadeOut(),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = tunerState.frequency?.let {
                                        String.format(Locale.getDefault(), "%.2f Hz", it) } ?: "",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                                Box {
                                    PitchArc(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        centsOff = tunerState.centsOff
                                    )

                                    NoteLabel(
                                        modifier = Modifier.align(Alignment.Center),
                                        musicNote = tunerState.note,
                                        style = MaterialTheme.typography.displayLargeEmphasized
                                    )
                                }
                            }
                        }

                        TuningSoundGrid(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            musicNotes = selectedTuning!!.musicNotes,
                            selectedIndex = currentlyClickedIndex ?: tunerState.noteIndex,
                            onIndexSelected = { viewModel.onItemClicked(it) },
                            tunedIndices = tunedIndices,
                        )
                    }
                }
                TuningMode.CHROMATIC -> {

                    if (selectedTuning == null) {
                        UserInfo(
                            modifier = Modifier.fillMaxSize(),
                            mainText = "Oops, something went wrong.",
                            supportingText = "Failed to load data."
                        )
                    } else {

                        AnimatedVisibility(
                            visible = !isEarModeEnabled,
                            enter = slideInVertically { -it } + expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                            exit = slideOutVertically { -it } + shrinkVertically() + fadeOut(),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = tunerState.frequency?.let {
                                        String.format(Locale.getDefault(), "%.2f Hz", it)
                                    } ?: "",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                                Box {
                                    PitchArc(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        centsOff = tunerState.centsOff
                                    )

                                    NoteLabel(
                                        modifier = Modifier.align(Alignment.Center),
                                        musicNote = tunerState.note,
                                        style = MaterialTheme.typography.displayLargeEmphasized
                                    )
                                }
                            }
                        }

                        if (isEarModeEnabled) {
                            EarModeChromaticContent(
                                modifier = Modifier
                                    .weight(1f),
                                allNotes = selectedTuning!!.musicNotes,
                                onPlayToggle = { index ->
                                    viewModel.onItemClicked(index) // TODO this needs to be changed. It might be better to have 2 functions one to select and other to play. To be investigated.
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
