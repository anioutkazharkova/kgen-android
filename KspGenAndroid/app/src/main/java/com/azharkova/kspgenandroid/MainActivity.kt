package com.azharkova.kspgenandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.azharkova.kspgenandroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(LayoutInflater.from(this))

        setContentView(binding.root)
        val adapter = TestAdapterImpl().apply {
            binding.list.layoutManager = LinearLayoutManager(this@MainActivity)
            binding.list.adapter = this
        }
        adapter.setupItems(listOf(1,2,3,4,5))
    }
}

