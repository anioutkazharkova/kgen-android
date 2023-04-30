package com.azharkova.kspgenandroid

import com.azharkova.test_kmm.data.NewsList
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService{

    @GET(URL)
    suspend fun loadNews(@Header("X-Api-Key")value: String = NetworkConfig.apiKey): NewsList

    companion object {
        const val  URL = "v2/everything?q=science"

        var apiService: ApiService? = null
        fun getInstance() : ApiService {
            if (apiService == null) {
                apiService = Retrofit.Builder()
                    .baseUrl("https://newsapi.org/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(ApiService::class.java)
            }
            return apiService!!
        }
    }
}

class NetworkConfig {
    companion object shared{
        val apiUrl = "newsapi.org"
        val apiKey = "5b86b7593caa4f009fea285cc74129e2"

        val header: HashMap<String, String> =  hashMapOf("X-Api-Key" to apiKey)
    }
}