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
import com.google.android.exoplayer2.Player
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MusicActivity : AppCompatActivity() {

    private lateinit var seekBar: SeekBar
    private lateinit var playPauseButton: FloatingActionButton
    private var isBound = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var player: ExoPlayer
    private var MusicS: MusicService? = null
    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.LocalBinder
            MusicS = binder.getService()
            player = MusicS?.getPlayer() ?: return

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
            MusicS?.initializePlayer()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.music_page)

          //  NotificationReceiver.createNotificationChannels(this)

            seekBar = findViewById(R.id.seekbarmusic)
            playPauseButton = findViewById(R.id.musicstop)

        playPauseButton.setOnClickListener {
            if (isBound) {
                if (MusicS?.isPlaying() == true) {
                    MusicS?.pause()
                } else {
                    MusicS?.play()
                }
            }
        }

        setupSeekBar()

        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

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
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
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
            if(isBound){
                unbindService(serviceConnection)
                isBound=false
            }
            ExoPlayerSingleton.releasePlayer()
            handler.removeCallbacksAndMessages(null)
        }
    }