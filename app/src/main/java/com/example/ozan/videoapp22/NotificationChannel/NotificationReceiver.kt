    package com.example.ozan.videoapp22.NotificationChannel

    import android.app.NotificationChannel
    import android.app.NotificationManager
    import android.content.BroadcastReceiver
    import android.content.Context
    import android.content.Intent
    import android.os.Build
    import androidx.core.content.ContextCompat
    import com.example.ozan.videoapp22.Services.MusicService
    import com.example.ozan.videoapp22.Services.VideoService

    class NotificationReceiver : BroadcastReceiver() {

        companion object {
            const val MUSIC_CHANNEL_ID = "music_notification_channel"
            const val VIDEO_CHANNEL_ID = "video_notification_channel"

            const val ACTION_PLAY_MUSIC = "com.example.ozan.videoapp22.ACTION_PLAY_MUSIC"
            const val ACTION_PAUSE_MUSIC = "com.example.ozan.videoapp22.ACTION_PAUSE_MUSIC"
            const val ACTION_NEXT_SONG = "com.example.ozan.videoapp22.ACTION_NEXT_SONG"
            const val ACTION_PREVIOUS_SONG = "com.example.ozan.videoapp22.ACTION_PREVIOUS_SONG"
            const val ACTION_PLAY_VIDEO = "com.example.ozan.videoapp22.ACTION_PLAY_VIDEO"
            const val ACTION_PAUSE_VIDEO = "com.example.ozan.videoapp22.ACTION_PAUSE_VIDEO"

            fun createNotificationChannels(context: Context) {
                val musicChannel = NotificationChannel(
                    MUSIC_CHANNEL_ID,
                    "Music Notifications",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Music Player NC"
                }

                val videoChannel = NotificationChannel(
                    VIDEO_CHANNEL_ID,
                    "Video Notifications",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Video Player NC"
                }

                val notificationManager = context.getSystemService(NotificationManager::class.java)

                notificationManager.createNotificationChannel(musicChannel)
                notificationManager.createNotificationChannel(videoChannel)
            }
        }

        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action ?: return
            val serviceIntent = Intent(context, when(action) {
                ACTION_PLAY_MUSIC, ACTION_PAUSE_MUSIC, ACTION_NEXT_SONG, ACTION_PREVIOUS_SONG -> MusicService::class.java
                ACTION_PLAY_VIDEO, ACTION_PAUSE_VIDEO -> VideoService::class.java
                else -> return
            }).apply {
                this.action = action
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
