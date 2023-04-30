package com.azharkova.kspgenandroid

import android.os.Bundle
import androidx.activity.compose.setContent
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.azharkova.annotations.BindClick
import com.azharkova.kspgenandroid.databinding.ActivityNewsItemBinding

class NewsItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsItemBinding
    var view = binding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
setContent { 
    NewsListScreenViewComposable(viewModel = NewsListVM())
}
    }

    @BindClick(R.id.fab)
    fun showAction(){
        print("Hello")
    }
}