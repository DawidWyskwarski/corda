package com.example.corda.domain.metronome

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Plays short click sounds for each metronome beat using [AudioTrack] in static mode.
 *
 * Two distinct tones are generated programmatically:
 * - Accent click (beat 1 of each bar): higher pitch, slightly louder
 * - Regular click (all other beats): lower pitch
 */
class MetronomeAudioPlayer @Inject constructor() {

    private val sampleRate = 44100

    private val accentTrack: AudioTrack = buildStaticTrack(
        generateClick(frequencyHz = 1400.0, durationMs = 45, amplitude = 0.9),
    )
    private val regularTrack: AudioTrack = buildStaticTrack(
        generateClick(frequencyHz = 900.0, durationMs = 40, amplitude = 0.75),
    )

    fun playBeat(isAccent: Boolean) {
        val track = if (isAccent) accentTrack else regularTrack
        if (track.state != AudioTrack.STATE_INITIALIZED) return
        try {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop()
            }
            track.setPlaybackHeadPosition(0)
            track.play()
        } catch (_: IllegalStateException) {
            // Ignore races between stop/play during rapid BPM changes
        }
    }

    fun release() {
        accentTrack.release()
        regularTrack.release()
    }

    private fun buildStaticTrack(buffer: ShortArray): AudioTrack {
        val bufferBytes = buffer.size * Short.SIZE_BYTES
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setBufferSizeInBytes(bufferBytes)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(buffer, 0, buffer.size)
        return track
    }

    private fun generateClick(
        frequencyHz: Double,
        durationMs: Int,
        amplitude: Double,
    ): ShortArray {
        val numSamples = sampleRate * durationMs / 1000
        val buffer = ShortArray(numSamples)
        val decayFactor = 80.0 / durationMs * 35
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-t * decayFactor)
            val sample = amplitude * Short.MAX_VALUE * envelope * sin(2.0 * PI * frequencyHz * t)
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }
}
