package com.example.corda.ui.screen.metronome

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.corda.R
import com.example.corda.ui.components.NavigationPill
import kotlinx.coroutines.launch
import com.example.corda.ui.screen.metronome.components.BpmSection

@Composable
fun MetronomeScreen(
    modifier: Modifier = Modifier,
    viewModel: MetronomeViewModel,
    openDrawer: () -> Unit,
    openSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Crossfade(
        targetState = state.isRunning,
        animationSpec = tween(durationMillis = 300),
        modifier = modifier,
        label = "metronome_state",
    ) { isRunning ->
        if (isRunning) {
            ActiveMetronomeScreen(
                state = state,
                onToggle = viewModel::toggleMetronome,
            )
        } else {
            InactiveMetronomeScreen(
                state = state,
                openDrawer = openDrawer,
                openSettings = openSettings,
                onToggle = viewModel::toggleMetronome,
                onBpmChange = viewModel::setBpm,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InactiveMetronomeScreen(
    state: MetronomeUiState,
    openDrawer: () -> Unit,
    openSettings: () -> Unit,
    onToggle: () -> Unit,
    onBpmChange: (Int) -> Unit,
) {
    val circleColor = MaterialTheme.colorScheme.primaryContainer
    val circleContentColor = MaterialTheme.colorScheme.onPrimaryContainer

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    NavigationPill(
                        text = stringResource(R.string.metronome_beats_label, state.beatsPerBar),
                        supportingText = when {
                            state.mutingEnabled -> stringResource(
                                R.string.metronome_pill_muting_label,
                                state.playBars,
                                state.muteBars,
                            )
                            else -> ""
                        },
                        onClick = openSettings
                    )
                },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = stringResource(R.string.open_drawer),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(circleColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggle,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.metronome_start),
                        color = circleContentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        letterSpacing = 2.sp,
                    )
                }
            }
            BpmSection(
                bpm = state.bpm,
                onBpmChange = onBpmChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp),
            )
        }
    }
}

@Composable
private fun ActiveMetronomeScreen(
    state: MetronomeUiState,
    onToggle: () -> Unit,
) {
    val backgroundColor = MaterialTheme.colorScheme.primaryContainer
    val circleColor = MaterialTheme.colorScheme.primary
    val circleBeatColor = MaterialTheme.colorScheme.onPrimary
    val textColor = MaterialTheme.colorScheme.onPrimaryContainer

    val scale = remember { Animatable(1f) }

    // Three independent "ripple" layers so the animations can overlap
    // Thanks to Animatable, UI can read .value and update accordingly
    val ringScales = remember { Array(3) { Animatable(1f) } }
    val ringAlphas = remember { Array(3) { Animatable(0f) } }
    // Round robin index to allow the animations to play concurrently
    val ringSlot = remember { object { var index = 0 } }
    val animScope = rememberCoroutineScope()

    LaunchedEffect(state.beatTick) {
        if (!state.isRunning) return@LaunchedEffect
        val isAccent = state.currentBeat == 1
        scale.snapTo(if (isAccent) 1.20f else 1.12f)
        if (isAccent) {
            val i = ringSlot.index % 3
            ringSlot.index++
            val ringDuration = (70_000 / state.bpm).coerceIn(150, 1200) // 70000 instead of 60000, so the animation is slightly longer (looks better at high bpm)

            // Animations launched concurrently
            animScope.launch {
                ringScales[i].snapTo(1f)
                ringAlphas[i].snapTo(0.5f)

                // "Ripples" change in size and fade concurrent
                launch { ringScales[i].animateTo(1.65f, tween(ringDuration, easing = LinearOutSlowInEasing)) }
                ringAlphas[i].animateTo(0f, tween(ringDuration))
            }
        }

        // Main circle animation
        scale.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center) {
                // Three ring layers drawn concurrently, each slot fades independently
                ringScales.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .graphicsLayer(
                                scaleX = ringScales[i].value,
                                scaleY = ringScales[i].value,
                                alpha = ringAlphas[i].value,
                            )
                            .clip(CircleShape)
                            .background(circleColor),
                    )
                }
                // Main beat circle
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
                        .clip(CircleShape)
                        .background(circleColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${state.currentBeat}",
                        color = circleBeatColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 72.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp),
            ) {
                Text(
                    text = "${state.bpm} BPM",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                )
                if (state.mutingEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.metronome_active_muting_label,
                            state.playBars,
                            state.muteBars,
                        ),
                        color = textColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}