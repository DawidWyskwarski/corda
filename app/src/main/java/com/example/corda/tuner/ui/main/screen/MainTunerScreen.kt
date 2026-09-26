package com.example.corda.tuner.ui.main.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.R
import com.example.corda.core.ui.components.NavigationPill
import com.example.corda.core.ui.components.UserInfo
import com.example.corda.core.ui.state.UiState
import com.example.corda.tuner.ui.main.components.EarModeToggleButton
import com.example.corda.tuner.ui.main.components.LoadedContent
import com.example.corda.tuner.ui.main.data.TuningTarget

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainTunerScreen(
    openDrawer: () -> Unit,
    openSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainTunerViewModel = hiltViewModel(),
) {
    val tuningTargetState by viewModel.tuningTargetState.collectAsStateWithLifecycle()
    val tunerReading by viewModel.tunerReading.collectAsStateWithLifecycle()
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

                    val (title, supportingText) = when (val state = tuningTargetState) {
                        is UiState.Loading -> "Loading..." to ""
                        is UiState.Error -> "Error" to stringResource(state.errorRes)
                        is UiState.Loaded -> when (state.data) {
                            is TuningTarget.Chromatic -> stringResource(R.string.chromatic_mode) to ""
                            is TuningTarget.Standard -> state.data.tuningName to state.data.instrumentName.get()
                        }
                    }

                    NavigationPill(
                        text = title,
                        supportingText = supportingText,
                        onClick = openSettings
                    )
                },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Rounded.Menu, stringResource(R.string.open_drawer))
                    }
                },
                actions = {
                    EarModeToggleButton(
                        isInEarMode = isEarModeEnabled,
                        onToggle = viewModel::toggleEarMode,
                        isEnabled = tuningTargetState is UiState.Loaded
                    )
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
            when (val state = tuningTargetState) {
                is UiState.Loading -> {
                    LoadingIndicator(
                        Modifier.fillMaxHeight()
                    )
                }
                is UiState.Error -> {
                    UserInfo(
                        mainText = "Oops, something went wrong",
                        supportingText = stringResource(state.errorRes),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                is UiState.Loaded -> {
                    LoadedContent(
                        loadedTuningTarget = state.data,
                        tunerReading = tunerReading,
                        isInEarMode = isEarModeEnabled,
                        currentlyClickedIndex = currentlyClickedIndex,
                        tunedIndices = tunedIndices,
                        onItemClicked = viewModel::onItemClicked,
                    )
                }
            }
        }
    }
}
