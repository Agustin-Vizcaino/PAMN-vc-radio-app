package com.example.voicecontrolradio_pamn

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri

object MediaPlayerManager {
    private var mediaPlayer: MediaPlayer? = null

    fun initializePlayer(context: Context, audioUrl: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, Uri.parse(audioUrl))
                setOnPreparedListener {
                    it.start()
                }
                prepareAsync()
            }
        }
    }

    fun adjustVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    fun pausePlayer() {
        mediaPlayer?.pause()
    }

    fun resumePlayer() {
        mediaPlayer?.start()
    }

    fun stopPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}