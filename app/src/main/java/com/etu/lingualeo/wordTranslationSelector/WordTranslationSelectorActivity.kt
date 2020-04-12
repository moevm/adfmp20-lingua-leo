package com.etu.lingualeo.wordTranslationSelector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.etu.lingualeo.R
import kotlinx.android.synthetic.main.activity_word_translation_selector.*
import java.lang.Math.round


class WordTranslationSelectorActivity : AppCompatActivity() {

    val translations = ArrayList<String>()
    var word: String = ""
    var wordsLeft = ArrayList<String>()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_save -> {
            addToDictionary()
            if (wordsLeft.size > 0) {
                val intent = Intent(this, WordTranslationSelectorActivity::class.java)
                intent.putExtra("words", wordsLeft)
                startActivity(intent)
            }
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_translation_selector)
        supportActionBar?.title = "Выбор перевода"
        val words = (intent.getSerializableExtra("words")) as ArrayList<String>
        word = words.first()
        wordsLeft = words
        wordsLeft.removeAt(0)
        textView.setText(word)
        getTranslations()
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, translations)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
    }

    fun getTranslations() {
        //TODO: получение вариантов перевода
        translations.add(Translation("Тест 1", 0.4f).toString())
        translations.add(Translation("Тест 2", 0.3f).toString())
        translations.add(Translation("Тест 3", 0.2f).toString())
    }

    fun addToDictionary() {
        //TODO: добавить слово с словарь
    }
}

class Translation(val translation: String, val probability: Float) {
    override fun toString(): String {
        return "${round(probability * 100)}%: ${translation}"
    }
}

