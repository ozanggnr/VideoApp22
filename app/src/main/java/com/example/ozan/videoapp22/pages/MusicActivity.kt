package com.example.ozan.videoapp22.pages

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ozan.videoapp22.NotificationChannel.MediaPlayerSingleton
import com.example.ozan.videoapp22.R
import com.example.ozan.videoapp22.services.MusicService
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MusicActivity : AppCompatActivity() {

    private lateinit var seekBar: SeekBar
    private lateinit var playPauseButton: FloatingActionButton
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_page)

        MediaPlayerSingleton.initialize(this)

        seekBar = findViewById(R.id.seekbarmusic)
        playPauseButton = findViewById(R.id.musicstop)

        setupSeekBar()

        playPauseButton.setOnClickListener {
            togglePlayPause()
        }
    }

    private fun togglePlayPause() {
        val action = if (isPlaying) MusicService.ACTION_PAUSE_MUSIC else MusicService.ACTION_PLAY_MUSIC
        startService(Intent(this, MusicService::class.java).apply {
            this.action = action
        })

        isPlaying = !isPlaying
        playPauseButton.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
    }

    private fun setupSeekBar() {
        val mediaPlayer = MediaPlayerSingleton.getInstance()

        val updateSeekBar = object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    seekBar.progress = mediaPlayer.currentPosition
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateSeekBar)

        seekBar.max = mediaPlayer.duration
        seekBar.progress = mediaPlayer.currentPosition

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaPlayerSingleton.release()
        handler.removeCallbacksAndMessages(null)
    }
}
