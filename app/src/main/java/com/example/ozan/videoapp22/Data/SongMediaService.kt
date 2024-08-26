package com.example.ozan.videoapp22.network

import com.example.ozan.videoapp22.Data.Songs
import retrofit2.Call
import retrofit2.http.GET

interface SongMediaService {
    @GET("b/K6GR")
    fun getSongs(): Call<List<Songs>>
}
