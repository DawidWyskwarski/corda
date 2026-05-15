package com.example.corda.domain.tuner.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Streams a sine wave at a target frequency using [AudioTrack] in streaming mode.
 * [play] updates the frequency while already playing without restarting the coroutine.
 */
class ToneGenerator(
    private val sampleRate: Int = TUNER_SAMPLE_RATE,
) : TonePlayer {

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null

    @Volatile
    private var targetFrequencyHz: Float = 440f

    @Volatile
    private var running: Boolean = false

    /**
     * Starts playback at [frequencyHz], or updates the frequency if already playing.
     */
    override fun play(frequencyHz: Float, scope: CoroutineScope) {
        require(frequencyHz > 0f && frequencyHz < sampleRate / 2f) {
            "frequencyHz must be in (0, ${sampleRate / 2f})"
        }
        targetFrequencyHz = frequencyHz
        if (running) return

        running = true
        playbackJob = scope.launch(Dispatchers.Default) {
            val track = obtainTrack()
            track.play()

            val chunkSamples = 1024
            val buffer = ShortArray(chunkSamples)
            var phase = 0.0
            var samplesSinceStart = 0
            val fadeInSamples = (sampleRate * FADE_MS / 1000).coerceAtLeast(1)

            try {
                while (isActive && running) {
                    val freq = targetFrequencyHz.toDouble()

                    for (i in buffer.indices) {
                        val raw = sin(phase)
                        phase += TWO_PI * freq / sampleRate
                        if (phase > TWO_PI * 1024) {
                            phase %= TWO_PI
                        }

                        var amp = VOLUME
                        if (samplesSinceStart < fadeInSamples) {
                            amp *= samplesSinceStart.toDouble() / fadeInSamples
                        }

                        val s = (raw * Short.MAX_VALUE * amp).roundToInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        buffer[i] = s.toShort()
                        samplesSinceStart++
                    }

                    var offset = 0
                    var remaining = buffer.size
                    while (remaining > 0 && isActive && running) {
                        val written = track.write(buffer, offset, remaining)
                        if (written < 0) break
                        offset += written
                        remaining -= written
                    }
                }
            } finally {
                try {
                    track.pause()
                    track.flush()
                } catch (_: Exception) {
                }
                running = false
            }
        }
    }

    /**
     * Stops playback (coroutine cancellation flushes the pipeline).
     * Releases the underlying [AudioTrack] so the next [play] creates a fresh instance.
     */
    override fun stop() {
        running = false
        playbackJob?.cancel()
        playbackJob = null
        audioTrack?.let { track ->
            try {
                track.pause()
                track.flush()
                track.stop()
            } catch (_: Exception) {
            }
            track.release()
        }
        audioTrack = null
    }

    private fun obtainTrack(): AudioTrack {
        audioTrack?.let { return it }

        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        require(minBuf != AudioTrack.ERROR && minBuf != AudioTrack.ERROR_BAD_VALUE) {
            "Invalid buffer size for AudioTrack"
        }

        val bufferSizeBytes = minBuf * 4

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        val track = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(bufferSizeBytes)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        if (track.state != AudioTrack.STATE_INITIALIZED) {
            track.release()
            error("AudioTrack failed to initialize")
        }

        audioTrack = track
        return track
    }

    private companion object {
        const val FADE_MS = 100
        const val VOLUME = 1.0
        val TWO_PI = 2.0 * PI
    }
}
