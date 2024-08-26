package com.example.ozan.videoapp22.Services

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
import com.example.ozan.videoapp22.Data.Songs
import com.example.ozan.videoapp22.NotificationChannel.NotificationReceiver
import com.example.ozan.videoapp22.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class MusicService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1
    }

    private lateinit var player: ExoPlayer
    private val binder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())
    private var mediaItems: List<MediaItem> = emptyList()
    private var currentSongIndex = 0

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun getPlayer(): ExoPlayer {
        return player
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        NotificationReceiver.createNotificationChannels(this)
        startForeground(NOTIFICATION_ID, buildNotification(isPlaying()))

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    broadcastSongDetails()
                }
            }
        })
    }

    private fun broadcastSongDetails() {
        val intent = Intent("com.example.ozan.videoapp22.UPDATE_SONG_DETAILS").apply {
            putExtra("title", mediaItems[currentSongIndex].mediaMetadata.title)
            putExtra("image", mediaItems[currentSongIndex].mediaMetadata.artworkUri.toString())
        }
        sendBroadcast(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            NotificationReceiver.ACTION_PLAY_MUSIC -> play()
            NotificationReceiver.ACTION_PAUSE_MUSIC -> pause()
            NotificationReceiver.ACTION_NEXT_SONG -> nextSong()
            NotificationReceiver.ACTION_PREVIOUS_SONG -> previousSong()
            else -> initializePlayer(currentSongIndex)
        }
        updateNotification()
        return START_STICKY
    }

    fun setMediaItems(songs: List<Songs>) {
        mediaItems = songs.map { song ->
            MediaItem.fromUri(song.music)
        }
        initializePlayer(currentSongIndex)
    }

    fun initializePlayer(songIndex: Int) {
        if (songIndex >= mediaItems.size) return
        val mediaItem = mediaItems[songIndex]
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

    fun setupSeekBar(seekBar: SeekBar) {

        seekBar.max = player.duration.toInt()

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

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateSeekBar)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handler.post(updateSeekBar)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        stopSelf()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
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

    fun nextSong() {
        currentSongIndex = (currentSongIndex + 1) % mediaItems.size
        initializePlayer(currentSongIndex)
        updateNotification()
    }

    fun previousSong() {
        currentSongIndex = (currentSongIndex - 1 + mediaItems.size) % mediaItems.size
        initializePlayer(currentSongIndex)
        updateNotification()
    }

    private fun buildNotification(isPlaying: Boolean): Notification {
        val playPauseIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = if (isPlaying) NotificationReceiver.ACTION_PAUSE_MUSIC
            else NotificationReceiver.ACTION_PLAY_MUSIC
        }
        val playPausePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_NEXT_SONG
        }
        val nextPendingIntent = PendingIntent.getBroadcast(
            this, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val previousIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_PREVIOUS_SONG
        }
        val previousPendingIntent = PendingIntent.getBroadcast(
            this, 2, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) {
            com.google.android.exoplayer2.ui.R.drawable.exo_icon_pause
        } else {
            com.google.android.exoplayer2.ui.R.drawable.exo_icon_play
        }
        val playPauseText = if (isPlaying) "Pause" else "Play"

        return NotificationCompat.Builder(this, NotificationReceiver.MUSIC_CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Playing song")
            .setSmallIcon(R.drawable.logo)
            .addAction(androidx.media3.ui.R.drawable.exo_icon_previous, "Previous", previousPendingIntent)
            .addAction(playPauseIcon, playPauseText, playPausePendingIntent)
            .addAction(androidx.media3.ui.R.drawable.exo_icon_next, "Next", nextPendingIntent)
            .setContentIntent(playPausePendingIntent)
            .setStyle(MediaStyle().setShowActionsInCompactView(1))
            .build()
    }

    fun updateNotification() {
        val notification = buildNotification(isPlaying())
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
