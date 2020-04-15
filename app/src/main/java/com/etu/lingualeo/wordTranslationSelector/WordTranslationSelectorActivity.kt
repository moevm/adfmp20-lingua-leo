package com.etu.lingualeo.wordTranslationSelector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.etu.lingualeo.R
import com.etu.lingualeo.restUtil.RestUtil
import kotlinx.android.synthetic.main.activity_word_translation_selector.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.lang.Math.round


class WordTranslationSelectorActivity : AppCompatActivity() {

    val translationsRaw = ArrayList<Translation>()
    val translations = ArrayList<String>()
    var word: String = ""
    var wordsLeft = ArrayList<String>()
    var selectionId = 0
    var wordId = 0
    var isChanging = false

    lateinit var adapter: ArrayAdapter<String>

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_save -> {
            if(isChanging) changeTranslation() else addToDictionary()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_translation_selector)
        supportActionBar?.title = "Выбор перевода"
        val words = (intent.getSerializableExtra("words")) as ArrayList<String>
        isChanging = intent.getBooleanExtra("isChanging", false)
        Log.i("dsadr", isChanging.toString())
        word = words.first()
        wordsLeft = words
        wordsLeft.removeAt(0)
        textView.setText(word)
        getTranslations()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, translations)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        listView.setItemChecked(selectionId, true)
        listView.setOnItemClickListener { parent, view, position, id -> selectionId = id.toInt() }

    }

    fun getTranslations() {
        RestUtil.instance.getTranslations(word, { status, translation ->
            if (status && translation != null) {
                wordId = translation.wordId.toInt()
                translations.clear()
                translationsRaw.clear()
                val translationOptions = translation.translations
                var totalVotes = 0
                for (translationOption in translationOptions) {
                    totalVotes += translationOption.votes.toInt()
                }
                for (translationOption in translationOptions) {
                    translationsRaw.add(
                        Translation(
                            translationOption.translation,
                            (translationOption.votes.toFloat() / totalVotes),
                            translationOption.translationId
                        )
                    )
                }
                for (tranlation in translationsRaw) {
                    translations.add(tranlation.toString())
                }
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    fun addToDictionary() {
        val translation = translationsRaw[selectionId]
        RestUtil.instance.addWord(wordId, translation.translationId, { status ->
            if (status) {
                runOnUiThread {
                    if (wordsLeft.size > 0) {
                        val intent = Intent(this, WordTranslationSelectorActivity::class.java)
                        intent.putExtra("words", wordsLeft)
                        startActivity(intent)
                    }
                    finish()
                }
            }
        })
    }

    fun changeTranslation() {
        val translation = translationsRaw[selectionId]
        RestUtil.instance.changeTranslation(wordId, translation.translationId, { status ->
            if (status) {
                runOnUiThread { finish() }
            }
        })
    }
}

class Translation(val translation: String, val probability: Float, val translationId: Number) {
    override fun toString(): String {
        return "${round(probability * 100)}%: ${translation.capitalize()}"
    }
}

