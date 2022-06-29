package com.jinkeen.cus.keyboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.jinkeen.cus.keyboard.databinding.ActivityMain2Binding
import com.jinkeen.cus.keyboard.databinding.ActivityMainBinding

class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMain2Binding>(this, R.layout.activity_main2)
    }
}