package com.example.corda.ui.screen.tuner.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun PitchArc(
    modifier: Modifier = Modifier,
    centsOff: Float?,
) {
    val trackColor = MaterialTheme.colorScheme.primaryContainer
    val targetZoneColor = if (abs(centsOff ?: 6f) > 5) {
        MaterialTheme.colorScheme.inversePrimary
    } else {
        Color(0xFF64F46F)
    }

    val indicatorColor = if (centsOff != null) {
        if (abs(centsOff) < 5f) {
            Color(0xFF35CD3D)
        } else {
            MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
        }
    } else {
        Color.Transparent
    }

    val strokeWidth = 42.dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f),
        ) {

            val strokeWidthPx = strokeWidth.toPx()

            val arcSize = Size(size.width - strokeWidthPx, size.width - strokeWidthPx)
            val topLeft = Offset(x = strokeWidthPx / 2f, y = strokeWidthPx / 2f)

            // Background track
            drawArc(
                color = trackColor,
                topLeft = topLeft,
                size = arcSize,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            )

            // Target zone at top-center
            drawArc(
                color = targetZoneColor,
                topLeft = topLeft,
                size = arcSize,
                startAngle = 265f,
                sweepAngle = 10f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            )

            // Indicator dot
            if (centsOff != null) {
                val clampedOffset = centsOff.coerceIn(-50f, 50f)
                val angle = 270f + (clampedOffset * 1.8f)

                drawArc(
                    color = indicatorColor,
                    topLeft = topLeft,
                    size = arcSize,
                    startAngle = angle,
                    sweepAngle = 0.1f,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = strokeWidth + 8.dp),
                text = "♭",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = strokeWidth + 8.dp),
                text = "♯",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 64.dp),
                text = if (centsOff == null) "Play something"
                else if (centsOff < -5f) "Tune higher"
                else if (centsOff > 5f) "Tune lower"
                else "Perfect",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}
