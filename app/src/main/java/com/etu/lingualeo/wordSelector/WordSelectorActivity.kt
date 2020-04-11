package com.etu.lingualeo.wordSelector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.etu.lingualeo.R
import kotlinx.android.synthetic.main.word_selector_activity.*

const val TEST_STR = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
        "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud " +
        "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure " +
        "dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
        "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt " +
        "mollit anim id est laborum."

class WordSelectorActivity : AppCompatActivity() {

    var words = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.word_selector_activity)
        supportActionBar?.setHomeButtonEnabled(true)
        generateWords()
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, words)
        wordListView.adapter = adapter
        wordListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        text.setText(TEST_STR)
    }

    fun generateWords() {
        words = ArrayList(TEST_STR.split(" ").distinct().sortedBy { it })
    }

}
