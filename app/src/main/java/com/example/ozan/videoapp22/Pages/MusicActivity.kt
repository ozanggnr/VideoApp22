package com.example.ozan.videoapp22.Pages

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.GestureDetector
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ozan.videoapp22.Adapter.RecyclerViewImg
import com.example.ozan.videoapp22.Data.MusicViewModel
import com.example.ozan.videoapp22.Gestures.SwipeGestureDetector
import com.example.ozan.videoapp22.NotificationChannel.ExoPlayerSingleton
import com.example.ozan.videoapp22.R
import com.example.ozan.videoapp22.Services.MusicService
import com.example.ozan.videoapp22.Services.VideoService
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MusicActivity : AppCompatActivity() {

    private lateinit var viewModel: MusicViewModel
    private lateinit var recyclerViewAdapter: RecyclerViewImg
    private var currentPosition = 0
    private var musicService: MusicService? = null
    private var isBound = false
    lateinit var player: ExoPlayer

    private lateinit var recyclerView: RecyclerView
    private lateinit var rwmusic: FloatingActionButton
    private lateinit var ffmusic: FloatingActionButton
    private lateinit var musicStop: FloatingActionButton
    private lateinit var seekBar: SeekBar
    private lateinit var songNameTextView: TextView

    private lateinit var slideInLeft: Animation
    private lateinit var slideOutRight: Animation
    private lateinit var slideInRight: Animation
    private lateinit var slideOutLeft: Animation
    private lateinit var gestureDetector: GestureDetector

    private val handler = Handler()


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.LocalBinder
            musicService = binder.getService()
            isBound = true
            player = musicService?.getPlayer() ?: return

            musicService?.setMediaItems(viewModel.songsList.value ?: emptyList())
            musicService?.initializePlayer(currentPosition)

            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        seekBar.max = player.duration.toInt()
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        musicStop.setImageResource(com.google.android.exoplayer2.R.drawable.exo_icon_pause)
                    } else {
                        musicStop.setImageResource(com.google.android.exoplayer2.R.drawable.exo_icon_play)
                    }
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                    //disconnection problemi durumu
                }
            })
            musicService?.setupSeekBar(seekBar)

            songNameTextView.text = viewModel.songsList.value?.get(currentPosition)?.title

            isBound = true
            musicService?.initializePlayer(currentPosition)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_page)

        viewModel = ViewModelProvider(this).get(MusicViewModel::class.java)
        recyclerView = findViewById(R.id.recylcer)
        rwmusic = findViewById(R.id.rwmusic)
        ffmusic = findViewById(R.id.ffmusic)
        musicStop = findViewById(R.id.musicstop)
        seekBar = findViewById(R.id.seekbarmusic)
        songNameTextView = findViewById(R.id.songname)

        slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_reverse)
        slideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_reverse)
        slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in)
        slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out)

        recyclerViewAdapter = RecyclerViewImg(this)
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = recyclerViewAdapter

        viewModel.songsList.observe(this, Observer { songs ->
            recyclerViewAdapter.setData(songs)
            if (songs.isNotEmpty()) {
                musicService?.setMediaItems(songs)
                songNameTextView.text = songs[currentPosition].title
                musicService?.initializePlayer(currentPosition)
            }
        })

        viewModel.loadData()

        rwmusic.setOnClickListener {
            if (isBound) {
                musicService?.nextSong()
                musicService?.updateNotification()
                currentPosition = (currentPosition + 1) % (recyclerViewAdapter.itemCount)
                songNameTextView.text = viewModel.songsList.value?.get(currentPosition)?.title
                recyclerView.startAnimation(slideOutLeft)
                Handler().postDelayed({
                    recyclerView.scrollToPosition(currentPosition)
                    recyclerView.startAnimation(slideInRight)
                }, slideOutLeft.duration)
            }
        }

        ffmusic.setOnClickListener {
            if (isBound) {
                musicService?.previousSong()
                musicService?.updateNotification()
                currentPosition =
                    (currentPosition - 1 + recyclerViewAdapter.itemCount) % recyclerViewAdapter.itemCount
                songNameTextView.text = viewModel.songsList.value?.get(currentPosition)?.title
                recyclerView.startAnimation(slideOutRight)
                Handler().postDelayed({
                    recyclerView.scrollToPosition(currentPosition)
                    recyclerView.startAnimation(slideInLeft)
                }, slideOutRight.duration)
            }
        }

        musicStop.setOnClickListener {
            if (isBound) {
                if (musicService?.isPlaying() == true) {
                    musicService?.pause()
                    musicStop.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_icon_play)
                } else {
                    musicService?.play()
                    musicStop.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_icon_pause)
                }
                musicService?.updateNotification()
            }
        }

        gestureDetector = GestureDetector(
            this, SwipeGestureDetector(object : SwipeGestureDetector.OnSwipeListener {
                override fun onSwipeLeft() {
                    ffmusic.performClick()
                }

                override fun onSwipeRight() {
                    rwmusic.performClick()
                }
            })
        )
        recyclerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        stopService(Intent(this, MusicService::class.java))
        ExoPlayerSingleton.releasePlayer()
        handler.removeCallbacksAndMessages(null)
    }
}
