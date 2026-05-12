package com.myra.assistant.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.util.concurrent.ConcurrentLinkedQueue

class LiveAudioManager(context: Context) {
    private val TAG = "MYRA_AUDIO"
    private var audioTrack: AudioTrack? = null
    private val audioQueue = ConcurrentLinkedQueue<ByteArray>()
    private var isPlaying = false
    private var playThread: Thread? = null

    // Gemini Live API returns PCM 24kHz mono 16-bit little-endian
    private val sampleRate = 24000
    private val channelConfig = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    // Track if audio is currently playing
    var isSpeaking = false
        private set

    // Callback for when queue fully drains
    var onQueueDrained: (() -> Unit)? = null

    init {
        createAudioTrack()
    }

    private fun createAudioTrack() {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                .coerceAtLeast(sampleRate * 2) // At least 1 second buffer

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(audioFormat)
                        .setChannelMask(channelConfig)
                        .build()
                )
                .setBufferSizeInBytes(minBufSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            Log.d(TAG, "AudioTrack created: ${sampleRate}Hz mono 16bit")
        } catch (e: Exception) {
            Log.e(TAG, "AudioTrack creation failed: ${e.message}")
        }
    }

    fun playChunk(data: ByteArray) {
        if (data.isEmpty()) return
        isSpeaking = true
        audioQueue.offer(data)
        if (!isPlaying) startPlaybackLoop()
    }

    private fun startPlaybackLoop() {
        isPlaying = true
        playThread = Thread {
            var emptyCount = 0
            while (isPlaying) {
                val chunk = audioQueue.poll()
                if (chunk != null) {
                    emptyCount = 0
                    try {
                        audioTrack?.write(chunk, 0, chunk.size)
                    } catch (e: Exception) {
                        Log.e(TAG, "Audio write error: ${e.message}")
                    }
                } else {
                    Thread.sleep(10)
                    emptyCount++
                    // If queue has been empty for ~500ms (50 * 10ms), consider drained
                    if (emptyCount > 50) {
                        isSpeaking = false
                        Log.d(TAG, "MYRA_TURN_COMPLETE: Audio queue drained, isSpeaking=false")
                        onQueueDrained?.invoke()
                        // Keep thread alive but mark not speaking
                        emptyCount = 0
                    }
                }
            }
        }.apply {
            name = "MYRA-Audio-Player"
            start()
        }
    }

    fun stop() {
        isPlaying = false
        isSpeaking = false
        audioQueue.clear()
        playThread?.join(500)
        playThread = null
        try {
            audioTrack?.stop()
            audioTrack?.reloadStaticData()
            audioTrack?.play()
        } catch (_: Exception) {}
        Log.d(TAG, "Audio stopped")
    }

    fun release() {
        stop()
        try {
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
