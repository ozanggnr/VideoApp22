package com.example.ozan.videoapp22.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.SeekBar
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.ozan.videoapp22.NotificationChannel.ExoPlayerSingleton
import com.example.ozan.videoapp22.NotificationChannel.NotificationReceiver
import com.example.ozan.videoapp22.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.upstream.RawResourceDataSource

class MusicService : Service() {

    private lateinit var player: ExoPlayer
    private val binder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun getPlayer(): ExoPlayer {
        return player
    }

    companion object {
        const val NOTIFICATION_ID = 1
    }


    override fun onCreate() {
        super.onCreate()

        //stopVideoService()
        player = ExoPlayerSingleton.getPlayer(this)
        NotificationReceiver.createNotificationChannels(this)
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            NotificationReceiver.ACTION_PLAY_MUSIC -> play()
            NotificationReceiver.ACTION_PAUSE_MUSIC -> pause()
            else -> initializePlayer()
        }
        updateNotification()
        return START_STICKY
    }


    private fun stopVideoService() {
        val stopIntent = Intent(this, VideoService::class.java)
        stopService(stopIntent)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(VideoService.NOTIFICATION_ID)
    }


    fun setupSeekBar(seekBar: SeekBar) {
        val updateSeekBar = object : Runnable {
            override fun run() {
                if (player.isPlaying) {
                    seekBar.progress = player.currentPosition.toInt()
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateSeekBar)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        ExoPlayerSingleton.releasePlayer()
        stopSelf()
        /*player.playWhenReady=false
        player.clearMediaItems()*/
        val stopIntent = Intent(this, MusicService::class.java)
        stopService(stopIntent)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        ServiceCompat.stopForeground(this,ServiceCompat.STOP_FOREGROUND_REMOVE)
    }


    fun play() {
        if (!player.isPlaying) {
            player.play()
        }
    }

    fun pause() {
        if (player.isPlaying) {
            player.pause()
        }
    }

    fun isPlaying(): Boolean {
        return player.isPlaying
    }

    fun initializePlayer() {
        player = ExoPlayerSingleton.getPlayer(this)
        val rawUri = RawResourceDataSource.buildRawResourceUri(R.raw.nirnir)
        val mediaItem = MediaItem.fromUri(rawUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

        val intent = Intent(this, MusicService::class.java).apply {
            action = if (player.isPlaying) {
                NotificationReceiver.ACTION_PAUSE_MUSIC

            } else {
                NotificationReceiver.ACTION_PLAY_MUSIC
            }
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun buildNotification(): Notification {
        val playIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_PLAY_MUSIC

        }
        val playPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_PAUSE_MUSIC
        }
        val pausePendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isPlaying = player.isPlaying
        val actionIcon =
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val actionIntent = if (isPlaying) pausePendingIntent else playPendingIntent

        return NotificationCompat.Builder(this, NotificationReceiver.MUSIC_CHANNEL_ID)
            .setSmallIcon(R.drawable.nirvana)
            .setContentTitle("Music Service")
            .setContentText("Playing music")
            .addAction(actionIcon, "Play/Pause", actionIntent)
            .setStyle(MediaStyle().setShowActionsInCompactView(0))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }
}