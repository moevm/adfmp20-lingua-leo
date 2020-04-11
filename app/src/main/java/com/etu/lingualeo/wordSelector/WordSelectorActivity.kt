package com.etu.lingualeo.wordSelector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.etu.lingualeo.R

class WordSelectorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.word_selector_activity)
        supportActionBar?.setHomeButtonEnabled(true)
    }
}
