package com.jinkeen.cus.keyboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.jinkeen.cus.keyboard.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.hideView.setOnClickListener { binding.edit2.visibility = View.GONE }
        binding.showView.setOnClickListener { startActivity(Intent(this, MainActivity2::class.java)) }
    }
}