package com.example.corda.data.tuner.audio

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.roundToInt

class PitchSmoother(
    private val emaAlpha: Float = 0.2f,
    private val silenceFrames: Int = 32,
    private val maxConsecutiveSnaps: Int = 16
) {

    private var smoothedLogFreq: Double = 0.0
    private var hasHistory: Boolean = false
    private var lockedFrequency: Float? = null

    private var silenceCounter: Int = 0
    private var consecutiveSnapCount: Int = 0

    fun process(rawFrequency: Float?): Float? {
        if (rawFrequency == null || rawFrequency <= 0f) {
            silenceCounter++
            if (silenceCounter >= silenceFrames) {
                reset()
                return null
            }
            return lockedFrequency?.let { exp(smoothedLogFreq).toFloat() }
        }

        silenceCounter = 0

        val fundamental = lockedFrequency
        val corrected: Float
        val wasSnapped: Boolean

        if (fundamental != null && fundamental > 0f) {
            val snapped = snapHarmonicToFundamental(rawFrequency, fundamental)
            wasSnapped = snapped != rawFrequency
            corrected = snapped
        } else {
            wasSnapped = false
            corrected = rawFrequency
        }

        if (wasSnapped) {
            consecutiveSnapCount++
            
            if (consecutiveSnapCount >= maxConsecutiveSnaps) {
                lockedFrequency = rawFrequency
                consecutiveSnapCount = 0
                hasHistory = false
                smoothedLogFreq = ln(rawFrequency.toDouble())
                hasHistory = true
                return rawFrequency
            }
        } else {
            consecutiveSnapCount = 0
        }

        val logFreq = ln(corrected.toDouble())
        smoothedLogFreq = if (!hasHistory) {
            hasHistory = true
            logFreq
        } else {
            emaAlpha * logFreq + (1 - emaAlpha) * smoothedLogFreq
        }

        val centsFromLocked = if (fundamental != null && fundamental > 0f) {
            abs(1200.0 * log2(corrected.toDouble() / fundamental.toDouble()))
        } else {
            0.0
        }

        if (fundamental == null || centsFromLocked < 200) {
            lockedFrequency = corrected
        }

        return exp(smoothedLogFreq).toFloat()
    }

    /**
     * If the new reading is near an integer multiple (2x-6x) or sub-multiple
     * (1/2x-1/6x) of the locked fundamental, snap back to the fundamental.
     */
    private fun snapHarmonicToFundamental(rawFrequency: Float, fundamental: Float): Float {
        val ratio = rawFrequency.toDouble() / fundamental.toDouble()

        val nearestHarmonic = ratio.roundToInt()
        if (nearestHarmonic in 2..6) {
            val idealFreq = fundamental * nearestHarmonic
            val centsDiff = abs(1200.0 * log2(rawFrequency.toDouble() / idealFreq.toDouble()))
            if (centsDiff < 50) return fundamental
        }

        val inverseRatio = fundamental.toDouble() / rawFrequency.toDouble()
        val nearestSubHarmonic = inverseRatio.roundToInt()
        if (nearestSubHarmonic in 2..6) {
            val idealFreq = fundamental / nearestSubHarmonic
            val centsDiff = abs(1200.0 * log2(rawFrequency.toDouble() / idealFreq.toDouble()))
            if (centsDiff < 50) return fundamental
        }

        return rawFrequency
    }

    fun reset() {
        hasHistory = false
        smoothedLogFreq = 0.0
        lockedFrequency = null
        silenceCounter = 0
        consecutiveSnapCount = 0
    }
}
