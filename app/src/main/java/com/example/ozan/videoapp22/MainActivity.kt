package com.example.ozan.videoapp22

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.ozan.videoapp22.Pages.MusicActivity
import com.example.ozan.videoapp22.Pages.VideoActivity


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragment1: ImageView = findViewById(R.id.fragmentContainerView)
        val fragment2: ImageView = findViewById(R.id.fragmentContainerView2)

        fragment1.setOnClickListener {
            val intent = Intent(this, MusicActivity::class.java)
            startActivity(intent)
        }

        fragment2.setOnClickListener {
            val intent = Intent(this, VideoActivity::class.java)
            startActivity(intent)
        }
    }
}