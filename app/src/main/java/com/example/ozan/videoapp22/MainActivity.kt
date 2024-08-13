package com.example.ozan.videoapp22

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentContainerView
import com.example.ozan.videoapp22.pages.MusicActivity
import com.example.ozan.videoapp22.pages.VideoActivity

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragment1: FragmentContainerView = findViewById(R.id.fragmentContainerView)
        val fragment2: FragmentContainerView = findViewById(R.id.fragmentContainerView2)

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