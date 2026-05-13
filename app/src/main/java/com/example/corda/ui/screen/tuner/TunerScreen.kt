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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.ui.components.NavigationPill
import com.example.corda.ui.components.PitchArc
import com.example.corda.ui.components.TuningSoundGrid
import com.example.corda.ui.components.UserInfo
import com.example.corda.ui.screen.tuner.settings.TuningMode
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TunerScreen(
    viewModel: TunerViewModel,
    modifier: Modifier = Modifier,
    openDrawer: () -> Unit,
    openSettings: () -> Unit
) {
    val selectedTuning by viewModel.selectedTuning.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val tunerState by viewModel.tunerUiState.collectAsStateWithLifecycle()
    var isEarModeEnabled by remember { mutableStateOf(false) }

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
        if (granted) viewModel.startListening()
    }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        if (hasPermission) viewModel.startListening()
        onDispose { viewModel.stopListening() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    NavigationPill(
                        text = when {
                            selectedMode == TuningMode.CHROMATIC -> "Chromatic Mode"
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
                        Icon(Icons.Rounded.Menu, contentDescription = "Open drawer")
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = isEarModeEnabled,
                        onCheckedChange = { isEarModeEnabled = it },
                        enabled = !(selectedMode == TuningMode.STANDARD && selectedTuning == null)
                    ) {
                        Icon(
                            imageVector = if (isEarModeEnabled) Icons.AutoMirrored.Rounded.VolumeUp
                            else Icons.AutoMirrored.Rounded.VolumeOff,
                            contentDescription = if (isEarModeEnabled) "Disable ear mode"
                            else "Enable ear mode",
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
            when {
                selectedMode == TuningMode.STANDARD && selectedTuning == null -> {
                    UserInfo(
                        modifier = Modifier.fillMaxSize(),
                        mainText = "No tunings found",
                        supportingText = "Please select or add a tuning"
                    )
                }

                else -> {
                    AnimatedVisibility(
                        visible = !isEarModeEnabled,
                        enter = slideInVertically { -it } + expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                        exit = slideOutVertically { -it } + shrinkVertically() + fadeOut(),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tunerState.detectedFrequency?.let {
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

                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = tunerState.detectedNote ?: "",
                                    style = MaterialTheme.typography.displayLargeEmphasized,
                                )
                            }
                        }
                    }

                    if (selectedMode == TuningMode.STANDARD && selectedTuning != null) {
                        val highlightedIndex = tunerState.focusedSoundIndex
                            ?: tunerState.autoSelectedSoundIndex

                        TuningSoundGrid(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            sounds = selectedTuning!!.sounds,
                            selectedIndex = highlightedIndex,
                            onIndexSelected = { viewModel.onNoteChipClicked(it) },
                            tunedIndices = tunerState.tunedSoundIndices,
                        )
                    }
                }
            }
        }
    }
}
