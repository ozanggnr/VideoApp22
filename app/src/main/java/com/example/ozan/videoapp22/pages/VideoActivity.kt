package com.example.ozan.videoapp22.pages

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ozan.videoapp22.NotificationChannel.ExoPlayerSingleton
import com.example.ozan.videoapp22.NotificationChannel.NotificationReceiver
import com.example.ozan.videoapp22.R
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_page)


        NotificationReceiver.createNotificationChannels(this)

        playerView = findViewById(R.id.videoscreen)
        seekBar = findViewById(R.id.videbar)
        playPauseButton = findViewById(R.id.startbutton)

        initializePlayer()

        playPauseButton.setOnClickListener {
            togglePlayPause()
        }


        setupSeekBar()
    }

    private fun initializePlayer() {
        player = ExoPlayerSingleton.getPlayer(this)
        playerView.player = player
        val mediaItem =
            MediaItem.fromUri("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4")
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true


        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    seekBar.max = player.duration.toInt()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    playPauseButton.setImageResource(com.google.android.exoplayer2.R.drawable.exo_icon_pause)
                } else {
                    playPauseButton.setImageResource(com.google.android.exoplayer2.R.drawable.exo_icon_play)

                }
            }
        })

        val intent = Intent(this, VideoService::class.java).apply {
            action = if (player.isPlaying) {
                NotificationReceiver.ACTION_PAUSE_VIDEO

            } else {
                NotificationReceiver.ACTION_PLAY_VIDEO
            }
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else player.play()
        /*val intent = Intent(this, VideoService::class.java).apply {
            action = if (player.isPlaying) {
                NotificationReceiver.ACTION_PAUSE_VIDEO

            } else {
                NotificationReceiver.ACTION_PLAY_VIDEO
            }
        }*/


        /*if (player.isPlaying) {
            playPauseButton.setImageResource(com.google.android.exoplayer2.R.drawable.exo_icon_play)
            player.pause()
        } else {
            playPauseButton.setImageResource(com.google.android.exoplayer2.R.drawable.exo_icon_pause)
            player.play()
        }*/
    }

    private fun setupSeekBar() {
        val player = ExoPlayerSingleton.getPlayer(this)

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
        handler.removeCallbacksAndMessages(null)
    }
}
