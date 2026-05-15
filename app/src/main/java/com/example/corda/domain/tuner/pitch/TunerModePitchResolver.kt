package com.example.corda.domain.tuner.pitch

import com.example.corda.data.tuner.local.entities.relations.TuningWithInstrumentAndSounds
import com.example.corda.domain.tuner.TuningMode

sealed class ResolvedPitch {
    data class Chromatic(val result: PitchResult) : ResolvedPitch()

    data class Standard(
        val result: PitchResult,
        /** Index into [TuningWithInstrumentAndSounds.sounds] when auto-matching; same as focused when locked. */
        val matchingSoundIndex: Int?,
    ) : ResolvedPitch()
}

object TunerModePitchResolver {

    fun resolve(
        smoothedFreq: Float,
        mode: TuningMode,
        tuning: TuningWithInstrumentAndSounds?,
        focusedSoundIndex: Int?,
    ): ResolvedPitch? = when (mode) {
        TuningMode.CHROMATIC ->
            ResolvedPitch.Chromatic(PitchHelpers.findClosestNote(smoothedFreq))

        TuningMode.STANDARD -> {
            val t = tuning ?: return null
            val sounds = t.sounds
            if (sounds.isEmpty()) return null
            if (focusedSoundIndex != null && focusedSoundIndex in sounds.indices) {
                ResolvedPitch.Standard(
                    result = PitchHelpers.findClosestToSingleTarget(smoothedFreq, sounds[focusedSoundIndex]),
                    matchingSoundIndex = focusedSoundIndex,
                )
            } else {
                val result = PitchHelpers.findClosestNoteInSet(smoothedFreq, sounds)
                val matchingIndex = PitchHelpers.findClosestNoteInSetIndex(smoothedFreq, sounds)
                    .takeIf { it >= 0 }
                ResolvedPitch.Standard(result, matchingIndex)
            }
        }
    }
}
