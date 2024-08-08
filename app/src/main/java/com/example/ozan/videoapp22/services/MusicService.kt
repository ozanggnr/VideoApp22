package com.example.ozan.videoapp22.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.ozan.videoapp22.NotificationChannel.MediaPlayerSingleton
import com.example.ozan.videoapp22.NotificationChannel.NotificationReceiver
import com.example.ozan.videoapp22.R

class MusicService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY_MUSIC = "com.example.ozan.videoapp22.ACTION_PLAY_MUSIC"
        const val ACTION_PAUSE_MUSIC = "com.example.ozan.videoapp22.ACTION_PAUSE_MUSIC"
        const val ACTION_SEEKBAR_UPDATE = "com.example.ozan.videoapp22.ACTION_SEEKBAR_UPDATE"
    }

    private val handler = Handler(Looper.getMainLooper())
    private val mediaPlayer = MediaPlayerSingleton.getInstance()

    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                val intent = Intent(ACTION_SEEKBAR_UPDATE).apply {
                    putExtra("current_position", mediaPlayer.currentPosition)
                    putExtra("duration", mediaPlayer.duration)
                }
                sendBroadcast(intent)
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        MediaPlayerSingleton.initialize(applicationContext)
        NotificationReceiver.createNotificationChannels(this)
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_MUSIC -> playMusic()
            ACTION_PAUSE_MUSIC -> pauseMusic()
        }
        updateNotification()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaPlayerSingleton.release()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun playMusic() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            handler.post(updateSeekBarRunnable)
        }
    }

    private fun pauseMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            handler.removeCallbacks(updateSeekBarRunnable)
        }
    }

    private fun buildNotification(): Notification {
        val playIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_PLAY_MUSIC
        }
        val playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val pauseIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_PAUSE_MUSIC
        }
        val pausePendingIntent = PendingIntent.getBroadcast(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val isPlaying = mediaPlayer.isPlaying
        val actionIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
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
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }
}
