package com.example.ozan.videoapp22.pages

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ozan.videoapp22.NotificationChannel.ExoPlayerSingleton
import com.example.ozan.videoapp22.NotificationChannel.NotificationReceiver
import com.example.ozan.videoapp22.R
import com.example.ozan.videoapp22.services.MusicService
import com.example.ozan.videoapp22.services.VideoService
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class VideoActivity : AppCompatActivity() {

    private lateinit var seekBar: SeekBar
    private lateinit var playPauseButton: FloatingActionButton
    private lateinit var playerView: StyledPlayerView
    private val handler = Handler(Looper.getMainLooper())
    lateinit var player: ExoPlayer
    private var isBound = false
    private var VideoS: VideoService? = null
    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as VideoService.LocalBinder
            VideoS = binder.getService()
            player = VideoS?.getPlayer() ?: return

            playerView.player = player
            VideoS?.setupSeekBar(seekBar)

            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        seekBar.max = player.duration.toInt()
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        playPauseButton.setImageResource(com.google.android.exoplayer2.R.drawable.exo_icon_pause)
                    } else {
                        playPauseButton.setImageResource(com.google.android.exoplayer2.R.drawable.exo_icon_play)
                    }
                }

                override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                    super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                    //disconnection problemi durumu
                }
            })
            isBound = true
            VideoS?.initializePlayer()
        }


        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_page)

        playerView = findViewById(R.id.videoscreen)
        seekBar = findViewById(R.id.videbar)
        playPauseButton = findViewById(R.id.startbutton)


        playPauseButton.setOnClickListener {
            if (isBound) {
                if (VideoS?.isPlaying() == true) {
                    VideoS?.pause()
                } else {
                    VideoS?.play()
                }
            }
        }
        val intent = Intent(this, VideoService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        ExoPlayerSingleton.releasePlayer()
        handler.removeCallbacksAndMessages(null)
    }
}
