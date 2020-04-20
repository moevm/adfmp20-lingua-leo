package com.etu.lingualeo.wordTranslationAutoSelector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.etu.lingualeo.R
import com.etu.lingualeo.restUtil.RestUtil
import com.etu.lingualeo.wordTranslationSelector.WordTranslationSelectorActivity
import kotlinx.android.synthetic.main.activity_word_translation_auto_selector.*

class WordTranslationAutoSelectorActivity : AppCompatActivity() {

    var totalWords = 0

    lateinit var words: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_translation_auto_selector)
        supportActionBar?.title = "Автоматический перевод"
        words = (intent.getSerializableExtra("words")) as ArrayList<String>
        totalWords = words.size
        textView2.setText("0/${totalWords}")
        translateWords()
    }

    fun translateWords() {
        val word = words.first()
        words.removeAt(0)
        RestUtil.instance.getTranslations(word, { status, translation ->
            if (status && translation != null) {
                val translationOptions =
                    translation.translations.sortedByDescending { option -> option.votes.toInt() }
                RestUtil.instance.addWord(
                    translation.wordId,
                    translationOptions[0].translationId,
                    { status ->
                        runOnUiThread {
                            textView2.setText("${totalWords - words.size}/${totalWords}")
                            progressBar2.setProgress((100.0f * (totalWords - words.size) / totalWords).toInt())
                            if (words.size > 0) {
                                translateWords()
                            } else {
                                finish()
                            }
                        }
                    })
            }
        })
    }
}