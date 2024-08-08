package com.example.ozan.videoapp22.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ozan.videoapp22.R
import com.example.ozan.videoapp22.databinding.Fragment1Binding
import com.example.ozan.videoapp22.databinding.Fragment3Binding

class Fragment3 : Fragment() {
    private var _binding: Fragment3Binding?=null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = Fragment3Binding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}