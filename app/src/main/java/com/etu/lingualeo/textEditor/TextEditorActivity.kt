package com.etu.lingualeo.textEditor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.etu.lingualeo.R
import com.etu.lingualeo.wordSelector.WordSelectorActivity
import kotlinx.android.synthetic.main.activity_text_editor.*

class TextEditorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_editor)
        val text = intent.getStringExtra("text")
        if (text != null) editText.setText(text)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_save -> {
            val intent = Intent(this, WordSelectorActivity::class.java)
            intent.putExtra("text", editText.text.toString())
            startActivity(intent)
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
