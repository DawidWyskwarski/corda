package com.example.corda.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CordaExtraColors(
    val tunerInTune: Color,
    val tunerInTuneBright: Color,
)

val LocalCordaColors = staticCompositionLocalOf {
    CordaExtraColors(
        tunerInTune = tunerInTuneLight,
        tunerInTuneBright = tunerInTuneBrightLight,
    )
}
