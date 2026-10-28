package com.example.colorchain.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural low-latency synthesized sound engine using AudioTrack.
 * Completely offline, zero asset files, instant playback.
 */
class SoundEffectManager {

    private val sampleRate = 22050
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    var isEnabled: Boolean = true

    /**
     * Correct tap sound: bright, clean chime whose frequency scales with chain length
     */
    fun playCorrectSound(chain: Int) {
        if (!isEnabled) return
        scope.launch {
            val baseFreq = 523.25 // C5
            val step = (chain % 12)
            val freq = baseFreq * Math.pow(2.0, step / 12.0)
            val pcm = generateTone(freq = freq, durationMs = 120, attackMs = 5, releaseMs = 80)
            playPcm(pcm)
        }
    }

    /**
     * Wrong tap sound: low dual buzz
     */
    fun playWrongSound() {
        if (!isEnabled) return
        scope.launch {
            val pcm = generateDualTone(freq1 = 180.0, freq2 = 140.0, durationMs = 280, attackMs = 10, releaseMs = 150)
            playPcm(pcm)
        }
    }

    /**
     * Timer warning tick: short soft blip
     */
    fun playWarningTick() {
        if (!isEnabled) return
        scope.launch {
            val pcm = generateTone(freq = 880.0, durationMs = 45, attackMs = 2, releaseMs = 35)
            playPcm(pcm)
        }
    }

    /**
     * Milestone celebration: rapid upward arpeggio
     */
    fun playMilestoneSound() {
        if (!isEnabled) return
        scope.launch {
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
            val combined = ArrayList<Short>()
            for (freq in notes) {
                val pcm = generateTone(freq = freq, durationMs = 80, attackMs = 5, releaseMs = 50)
                combined.addAll(pcm.toList())
            }
            playPcm(combined.toShortArray())
        }
    }

    /**
     * Level complete: triumphant melody
     */
    fun playLevelCompleteSound() {
        if (!isEnabled) return
        scope.launch {
            val notes = doubleArrayOf(440.0, 554.37, 659.25, 880.0)
            val combined = ArrayList<Short>()
            for (freq in notes) {
                val pcm = generateTone(freq = freq, durationMs = 110, attackMs = 5, releaseMs = 70)
                combined.addAll(pcm.toList())
            }
            playPcm(combined.toShortArray())
        }
    }

    /**
     * Game over: descending notes
     */
    fun playGameOverSound() {
        if (!isEnabled) return
        scope.launch {
            val notes = doubleArrayOf(440.0, 392.0, 349.23, 293.66)
            val combined = ArrayList<Short>()
            for (freq in notes) {
                val pcm = generateTone(freq = freq, durationMs = 120, attackMs = 10, releaseMs = 90)
                combined.addAll(pcm.toList())
            }
            playPcm(combined.toShortArray())
        }
    }

    private fun generateTone(
        freq: Double,
        durationMs: Int,
        attackMs: Int,
        releaseMs: Int
    ): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val attackSamples = (sampleRate * attackMs) / 1000
        val releaseSamples = (sampleRate * releaseMs) / 1000
        val sustainSamples = (totalSamples - attackSamples - releaseSamples).coerceAtLeast(0)

        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val envelope: Double = when {
                i < attackSamples -> i.toDouble() / attackSamples
                i < attackSamples + sustainSamples -> 1.0
                else -> {
                    val relIndex = i - attackSamples - sustainSamples
                    (1.0 - (relIndex.toDouble() / releaseSamples)).coerceAtLeast(0.0)
                }
            }
            val angle = 2.0 * PI * i * freq / sampleRate
            val sample = (sin(angle) * envelope * 24000).toInt()
            buffer[i] = sample.coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateDualTone(
        freq1: Double,
        freq2: Double,
        durationMs: Int,
        attackMs: Int,
        releaseMs: Int
    ): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val envelope = (1.0 - (i.toDouble() / totalSamples)).coerceIn(0.0, 1.0)
            val angle1 = 2.0 * PI * i * freq1 / sampleRate
            val angle2 = 2.0 * PI * i * freq2 / sampleRate
            val sample = ((sin(angle1) + sin(angle2) * 0.5) * envelope * 18000).toInt()
            buffer[i] = sample.coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun playPcm(pcm: ShortArray) {
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBuf, pcm.size * 2)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()

            // Release after playing
            val durationMs = (pcm.size * 1000L) / sampleRate
            Thread.sleep(durationMs + 50)
            track.stop()
            track.release()
        } catch (_: Exception) {
            // Audio error gracefully ignored on devices with disabled audio
        }
    }
}
