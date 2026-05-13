package com.example.corda.data.tuner.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioProcessingService {

    companion object {
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE = 5120
        const val OVERLAP = 4096
        private const val PROBABILITY_THRESHOLD = 0.75f
    }

    private val _pitchFlow = MutableSharedFlow<Float?>(extraBufferCapacity = 16)
    val pitchFlow: SharedFlow<Float?> = _pitchFlow.asSharedFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private var recordingJob: Job? = null
    private var audioRecord: AudioRecord? = null

    @SuppressLint("MissingPermission")
    fun start(scope: CoroutineScope) {
        if (_isListening.value) return

        recordingJob = scope.launch(Dispatchers.IO) {
            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val recordBufferSize = maxOf(BUFFER_SIZE * 2, minBufferSize)

            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                recordBufferSize
            )

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                recorder.release()
                return@launch
            }

            audioRecord = recorder

            val format = TarsosDSPAudioFormat(SAMPLE_RATE.toFloat(), 16, 1, true, false)
            val audioEvent = AudioEvent(format).apply {
                setOverlap(OVERLAP)
            }

            val pitchProcessor = PitchProcessor(
                PitchEstimationAlgorithm.YIN,
                SAMPLE_RATE.toFloat(),
                BUFFER_SIZE
            ) { result, _ ->
                val pitch = result.pitch
                val probability = result.probability
                
                if (pitch > 0 && probability > PROBABILITY_THRESHOLD) {
                    _pitchFlow.tryEmit(pitch)
                } else {
                    _pitchFlow.tryEmit(null)
                }
            }

            recorder.startRecording()
            _isListening.value = true

            val shortBuffer = ShortArray(BUFFER_SIZE)
            val floatBuffer = FloatArray(BUFFER_SIZE)
            val stepSize = BUFFER_SIZE - OVERLAP

            try {
                while (isActive) {
                    // Shift overlap portion from end to beginning
                    System.arraycopy(floatBuffer, stepSize, floatBuffer, 0, OVERLAP)

                    val shortsRead = recorder.read(shortBuffer, 0, stepSize)
                    if (shortsRead <= 0) continue

                    for (i in 0 until shortsRead) {
                        floatBuffer[OVERLAP + i] = shortBuffer[i] / 32768f
                    }

                    audioEvent.setFloatBuffer(floatBuffer)
                    pitchProcessor.process(audioEvent)
                }
            } finally {
                recorder.stop()
                recorder.release()
                audioRecord = null
                _isListening.value = false
                pitchProcessor.processingFinished()
            }
        }
    }

    fun stop() {
        recordingJob?.cancel()
        recordingJob = null
    }
}
