package com.example.corda.domain.tuner.pitch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.pow

class PitchSmootherTest {

    @Test
    fun process_firstValidFrequency_returnsValueNearInput() {
        val smoother = PitchSmoother()
        val result = smoother.process(440f)
        assertNotNull(result)
        assertEquals(440f, result!!, 1f)
    }

    @Test
    fun process_nullFramesBelowThreshold_holdsLastSmoothedOutput() {
        val smoother = PitchSmoother(silenceFrames = 3)
        val last = smoother.process(440f)
        assertNotNull(last)

        val held = smoother.process(null)
        assertNotNull(held)
        assertEquals(last!!, held!!, 1f)
    }

    @Test
    fun process_nullFramesAtThreshold_returnsNullAndResets() {
        val smoother = PitchSmoother(silenceFrames = 3)
        smoother.process(440f)

        smoother.process(null)
        smoother.process(null)
        val result = smoother.process(null)

        assertNull(result)

        val afterReset = smoother.process(440f)
        assertNotNull(afterReset)
        assertEquals(440f, afterReset!!, 1f)
    }

    @Test
    fun process_harmonicSnap_snapsToFundamental() {
        val smoother = PitchSmoother(silenceFrames = 32)
        smoother.process(110f)

        val nearOctave = (220.0 * 2.0.pow(-5.0 / 1200.0)).toFloat()
        val result = smoother.process(nearOctave)

        assertNotNull(result)
        assertEquals(110f, result!!, 2f)
    }

    @Test
    fun reset_afterProcessing_nextFrameTreatedAsFresh() {
        val smoother = PitchSmoother()
        smoother.process(440f)
        smoother.reset()

        val result = smoother.process(220f)
        assertNotNull(result)
        assertEquals(220f, result!!, 1f)
    }
}
