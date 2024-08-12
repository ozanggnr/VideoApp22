package com.example.ozan.videoapp22.NotificationChannel

import android.content.Context
import android.media.MediaPlayer
import com.google.android.exoplayer2.ExoPlayer

object ExoPlayerSingleton {
    private var player: ExoPlayer? = null

    fun getPlayer(context: Context): ExoPlayer {
        return player ?: ExoPlayer.Builder(context).build().apply {
            player = this
        }
    }

    fun getInstance():ExoPlayer{
        return player?:throw IllegalStateException("MediaPlayerSingleton is not initialized.")
    }

    fun releasePlayer() {
        player?.release()
        player = null
    }
}
