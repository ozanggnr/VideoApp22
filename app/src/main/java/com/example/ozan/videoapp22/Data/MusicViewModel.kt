package com.example.ozan.videoapp22.Data


//oluşturulacak
//mutable data++
//live data++
//fetch data


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ozan.videoapp22.network.SongMediaService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response

class MusicViewModel : ViewModel() {

    private val _songsList = MutableLiveData<List<Songs>>()


    val songsList: LiveData<List<Songs>> get() = _songsList


    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.jsonkeeper.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    private val apiService = retrofit.create(SongMediaService::class.java)


    fun loadData() {
        apiService.getSongs().enqueue(object : Callback<List<Songs>> {
            override fun onResponse(call: Call<List<Songs>>, response: Response<List<Songs>>) {
                if (response.isSuccessful) {

                    _songsList.value = response.body()
                } else {
                    handleError(response.message())
                }
            }

            override fun onFailure(call: Call<List<Songs>>, t: Throwable) {

                handleError(t.message ?: "ERRRRRRRRRRRORRRRR")
            }
        })
    }


    private fun handleError(message: String) {
        Log.e("MusicViewModel", "Error: $message")
    }
}




    
