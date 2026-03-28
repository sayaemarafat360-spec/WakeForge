package com.wakeforge.app.data.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "SoundManager"
        private const val PREVIEW_DURATION_MS = 5000L
    }

    private var mediaPlayer: MediaPlayer? = null
    private var currentSoundUri: String? = null
    private var previewHandler: android.os.Handler? = null
    private var previewRunnable: Runnable? = null

    fun playAlarm(soundUri: String, volume: Float, looping: Boolean = true) {
        stopAlarm()

        try {
            val uri = getSoundUri(soundUri)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                setDataSource(context, uri)
                isLooping = looping
                setVolume(volume, volume)
                setOnPreparedListener { mp ->
                    mp.start()
                    Log.d(TAG, "Started playing alarm: $soundUri")
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    true
                }
                prepareAsync()
            }
            currentSoundUri = soundUri
        } catch (e: IOException) {
            Log.e(TAG, "Error setting data source for alarm: $soundUri", e)
            playFallbackAlarm(volume, looping)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Illegal state playing alarm: $soundUri", e)
            playFallbackAlarm(volume, looping)
        }
    }

    fun stopAlarm() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Error stopping alarm", e)
        } finally {
            mediaPlayer = null
            currentSoundUri = null
        }
    }

    fun setVolume(volume: Float) {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.setVolume(volume, volume)
            }
        }
    }

    fun previewSound(soundUri: String, volume: Float) {
        stopAlarm()
        cancelPreviewTimer()

        try {
            val uri = getSoundUri(soundUri)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, uri)
                isLooping = false
                setVolume(volume, volume)
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnCompletionListener { mp ->
                    mp.release()
                    mediaPlayer = null
                }
                prepareAsync()
            }
            currentSoundUri = soundUri

            previewHandler = android.os.Handler(android.os.Looper.getMainLooper())
            previewRunnable = Runnable {
                stopAlarm()
            }
            previewHandler?.postDelayed(previewRunnable!!, PREVIEW_DURATION_MS)

        } catch (e: IOException) {
            Log.e(TAG, "Error previewing sound: $soundUri", e)
        }
    }

    private fun cancelPreviewTimer() {
        previewRunnable?.let { runnable ->
            previewHandler?.removeCallbacks(runnable)
        }
        previewRunnable = null
        previewHandler = null
    }

    private fun getSoundUri(soundId: String): Uri {
        val builtinMapping = mapOf(
            "builtin_dawn" to R.raw.builtin_dawn,
            "builtin_rise" to R.raw.builtin_rise,
            "builtin_forge" to R.raw.builtin_forge,
            "builtin_crystal" to R.raw.builtin_crystal,
            "builtin_digital" to R.raw.builtin_digital
        )

        val resourceId = builtinMapping[soundId]
        if (resourceId != null) {
            return Uri.parse("android.resource://${context.packageName}/$resourceId")
        }

        return try {
            Uri.parse(soundId)
        } catch (e: Exception) {
            Log.e(TAG, "Invalid sound URI: $soundId, falling back to default", e)
            Uri.parse("android.resource://${context.packageName}/${R.raw.builtin_dawn}")
        }
    }

    private fun playFallbackAlarm(volume: Float, looping: Boolean) {
        try {
            val fallbackUri = Uri.parse("android.resource://${context.packageName}/${R.raw.builtin_dawn}")
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                setDataSource(context, fallbackUri)
                isLooping = looping
                setVolume(volume, volume)
                setOnPreparedListener { mp ->
                    mp.start()
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback alarm also failed", e)
            try {
                mediaPlayer = MediaPlayer.create(
                    context,
                    android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                )
                mediaPlayer?.apply {
                    isLooping = looping
                    setVolume(volume, volume)
                    start()
                }
            } catch (e2: Exception) {
                Log.e(TAG, "All alarm playback attempts failed", e2)
            }
        }
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun getCurrentSoundUri(): String? {
        return currentSoundUri
    }
}
