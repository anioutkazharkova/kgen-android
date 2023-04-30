package com.azharkova.kspgenandroid

import com.azharkova.test_kmm.data.NewsList

class NewsService {
    private val apiService by lazy {
        ApiService.getInstance()
    }

    suspend fun loadNews():NewsList {
       return apiService.loadNews()
    }
}