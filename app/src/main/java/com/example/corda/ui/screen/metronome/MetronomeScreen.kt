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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.corda.R
import kotlinx.coroutines.launch

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
                    SettingsPill(state = state, onClick = openSettings)
                },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = stringResource(R.string.open_drawer),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
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
    val ringScale = remember { Animatable(1f) }
    val ringAlpha = remember { Animatable(0f) }
    val animScope = rememberCoroutineScope()

    LaunchedEffect(state.currentBeat) {
        if (!state.isRunning) return@LaunchedEffect
        val isAccent = state.currentBeat == 1
        scale.snapTo(if (isAccent) 1.20f else 1.12f)
        if (isAccent) {
            // Ring animation runs independently so it can outlast the beat interval
            animScope.launch {
                ringScale.snapTo(1f)
                ringAlpha.snapTo(0.5f)
                launch { ringScale.animateTo(1.65f, tween(600, easing = LinearOutSlowInEasing)) }
                ringAlpha.animateTo(0f, tween(600))
            }
        }
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
                // Fading ring — only visible on beat 1
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer(
                            scaleX = ringScale.value,
                            scaleY = ringScale.value,
                            alpha = ringAlpha.value,
                        )
                        .clip(CircleShape)
                        .background(circleColor),
                )
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

@Composable
private fun SettingsPill(
    state: MetronomeUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = modifier
            .widthIn(min = 180.dp)
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 6.dp),
            ) {
                Text(
                    text = stringResource(R.string.metronome_beats_label, state.beatsPerBar),
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
                if (state.mutingEnabled) {
                    Text(
                        text = stringResource(
                            R.string.metronome_pill_muting_label,
                            state.playBars,
                            state.muteBars,
                        ),
                        color = contentColor,
                        fontSize = 12.sp,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun BpmSection(
    bpm: Int,
    onBpmChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val onBackground = MaterialTheme.colorScheme.onBackground

    var isEditing by remember { mutableStateOf(false) }
    var bpmText by remember { mutableStateOf(bpm.toString()) }
    var hasFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Keep text in sync with external BPM changes (e.g. slider) while not editing
    LaunchedEffect(bpm) {
        if (!isEditing) bpmText = bpm.toString()
    }

    // Auto-focus the field when editing begins
    LaunchedEffect(isEditing) {
        if (isEditing) focusRequester.requestFocus()
    }

    fun commit() {
        val parsed = bpmText.toIntOrNull()
            ?.coerceIn(MetronomeViewModel.BPM_MIN, MetronomeViewModel.BPM_MAX)
            ?: bpm
        onBpmChange(parsed)
        bpmText = parsed.toString()
        isEditing = false
        hasFocused = false
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isEditing) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = bpmText,
                    onValueChange = { bpmText = it.filter { c -> c.isDigit() }.take(3) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { commit() }),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = onBackground,
                        textAlign = TextAlign.Center,
                    ),
                    cursorBrush = SolidColor(primaryColor),
                    decorationBox = { innerTextField ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            innerTextField()
                            Spacer(modifier = Modifier.height(2.dp))
                            HorizontalDivider(color = primaryColor, thickness = 2.dp)
                        }
                    },
                    modifier = Modifier
                        .width(80.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                hasFocused = true
                            } else if (hasFocused) {
                                commit()
                            }
                        },
                )
                Text(
                    text = " BPM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = onBackground,
                )
            }
        } else {
            Text(
                text = "$bpm BPM",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = onBackground,
                modifier = Modifier.clickable {
                    bpmText = bpm.toString()
                    isEditing = true
                },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = bpm.toFloat(),
            onValueChange = { onBpmChange(it.toInt()) },
            valueRange = MetronomeViewModel.BPM_MIN.toFloat()..MetronomeViewModel.BPM_MAX.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor,
                inactiveTrackColor = containerColor,
            ),
        )
    }
}
