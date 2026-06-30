package com.example.corda.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Class that represents the different screens in the app.
 *
 * Navigation3 requires each screen to be a [NavKey],
 * so it knows that it is a valid destination
 *
 * We define [Screen] as a `sealed class` so each child of a [Screen] is known at compile time.
 * If you add a screen but forget to handle it in a 'when' statement, the code won't compile.
 *
 * `@Serializable` allows navigation library to save the state of the app.
 * If system kills the app to save battery, the state can be restored.
 *
 * Since these screens don't take any parameters yet (like a UserID), we use
 * 'data object' to create a single, memory-efficient instance.
 *
 * Use `data class` if you want to pass parameters.
 */
sealed class Screen: NavKey {
    @Serializable
    data object Tuner: Screen()
    @Serializable
    data object TunerSettings: Screen()
    @Serializable
    data object Metronome: Screen()
    @Serializable
    data object MetronomeSettings: Screen()
    @Serializable
    data class AddEditTuning(val tuningId: Int? = null): Screen()
    @Serializable
    data object Settings: Screen()
    @Serializable
    data object Help: Screen()
}