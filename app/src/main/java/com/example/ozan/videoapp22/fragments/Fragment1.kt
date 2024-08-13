package com.example.ozan.videoapp22.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.ozan.videoapp22.R
import com.example.ozan.videoapp22.databinding.Fragment1Binding

class Fragment1 : Fragment(R.layout.fragment_1) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageView:ImageView=view.findViewById(R.id.imageView)
        imageView.setImageResource(R.drawable.nirnirv)
    }
}