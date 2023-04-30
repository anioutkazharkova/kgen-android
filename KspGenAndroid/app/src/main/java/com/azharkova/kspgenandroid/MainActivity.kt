package com.azharkova.kspgenandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.compose.setContent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.azharkova.kspgenandroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsListScreenViewComposable(viewModel = NewsListVM())
        }
       /* val binding = ActivityMainBinding.inflate(LayoutInflater.from(this))

        setContentView(binding.root)
        val adapter = TestAdapterImpl().apply {
            binding.list.layoutManager = LinearLayoutManager(this@MainActivity)
            binding.list.adapter = this
        }
        adapter.setupItems(listOf(1,2,3,4,5))*/
    }
}

fun ViewBinding.get(id: Int): View {
    return this.root.rootView.findViewById(id)
}

fun ViewBinding.getName(id: Int):String {
    return this.root.resources.getResourceName(id)
}