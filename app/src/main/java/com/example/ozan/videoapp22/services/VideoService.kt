package com.example.ozan.videoapp22.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.ozan.videoapp22.NotificationChannel.ExoPlayerSingleton
import com.example.ozan.videoapp22.NotificationChannel.NotificationReceiver
import com.example.ozan.videoapp22.R
import com.google.android.exoplayer2.ExoPlayer

class VideoService : Service() {

    private lateinit var player: ExoPlayer

    companion object {
        const val NOTIFICATION_ID = 2
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayerSingleton.getPlayer(this)
        NotificationReceiver.createNotificationChannels(this)
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            NotificationReceiver.ACTION_PLAY_VIDEO -> playVideo()
            NotificationReceiver.ACTION_PAUSE_VIDEO -> pauseVideo()
        }
        updateNotification()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        ExoPlayerSingleton.releasePlayer()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun playVideo() {
        if (!player.isPlaying) {
            player.play()
        }
    }

    private fun pauseVideo() {
        if (player.isPlaying) {
            player.pause()
        }
    }

    private fun buildNotification(): Notification {
        val playIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_PLAY_VIDEO
        }
        val playPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_PAUSE_VIDEO
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

        return NotificationCompat.Builder(this, NotificationReceiver.VIDEO_CHANNEL_ID)
            .setSmallIcon(R.drawable.nirvana)
            .setContentTitle("Video Service")
            .setContentText("Playing video")
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
