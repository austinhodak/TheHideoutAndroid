package com.austinhodak.thehideout.bsg

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.ActivityLoadoutCreatorBinding

class CreatorMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoadoutCreatorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_TheHideout)
        binding = ActivityLoadoutCreatorBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.md_nav_back)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}