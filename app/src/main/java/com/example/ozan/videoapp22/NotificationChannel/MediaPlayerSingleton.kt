package com.example.ozan.videoapp22.NotificationChannel

import android.content.Context
import android.media.MediaPlayer
import com.example.ozan.videoapp22.R

object MediaPlayerSingleton {
    private var instance: MediaPlayer? = null

    fun initialize(context: Context) {
        if (instance == null) {
            instance = MediaPlayer.create(context, R.raw.nirnir).apply {
                setOnPreparedListener {
                    isLooping = true
                }
            }
        }
    }

    fun getInstance(): MediaPlayer {
        return instance ?: throw IllegalStateException("MediaPlayerSingleton is not initialized.")
    }

    fun release() {
        instance?.release()
        instance = null
    }
}

