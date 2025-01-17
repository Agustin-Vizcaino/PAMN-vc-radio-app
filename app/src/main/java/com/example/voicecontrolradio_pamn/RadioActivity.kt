package com.example.voicecontrolradio_pamn

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri

object MediaPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    // Número positivo significa pausado, cero significa en reproducción
    private var UserPaused = -1

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
                    UserPaused = 0
                }
                prepareAsync()
            }
        }
    }

    fun adjustVolume(volume: Int) {
        mediaPlayer?.setVolume(volume.toFloat()/100, volume.toFloat()/100)
    }

    fun pausePlayer(user: Boolean = false) {
        if (user) UserPaused = 1
        mediaPlayer?.pause()
    }

    fun resumePlayer(user: Boolean = false) {
        if (UserPaused == 1 && !user) return
        if (user) UserPaused = 0
        mediaPlayer?.start()
    }

    fun stopPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun checkPlayer(): Boolean {
        return mediaPlayer != null
    }
}