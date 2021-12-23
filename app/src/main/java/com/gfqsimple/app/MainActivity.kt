package com.gfqsimple.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gfqsimple.app.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun layout(): Int =R.layout.activity_main

    override fun initView() {
       binding.tvMutiSelect.setOnClickListener {
           startActivity(Intent(this,MutiSelectActivity::class.java))
       }
        binding.tvSingleSelect.setOnClickListener {
           startActivity(Intent(this,SingleSelectActivity::class.java))
       }
    }
}