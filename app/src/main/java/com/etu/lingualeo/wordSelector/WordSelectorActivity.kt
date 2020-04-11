package com.etu.lingualeo.wordSelector

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.etu.lingualeo.R
import kotlinx.android.synthetic.main.word_selector_activity.*
import me.saket.bettermovementmethod.BetterLinkMovementMethod


const val TEST_STR = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
        "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud " +
        "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure " +
        "dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
        "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt " +
        "mollit anim id est laborum."

class WordSelectorActivity : AppCompatActivity() {

    var words = ArrayList<String>()
    var wordsDistinct = ArrayList<String>()
    var selectedWords = ArrayList<String>()
    val knownWords = ArrayList<String>()
    var ss = SpannableString(" ")

    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.word_selector_activity)
        supportActionBar?.setHomeButtonEnabled(true)

        ss = SpannableString(TEST_STR)

        getWordsFromText()
        generateSelectableString()
        text.setMovementMethod(BetterLinkMovementMethod.getInstance());
        text.setText(ss)
        text.highlightColor = Color.LTGRAY

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, words)
        wordListView.adapter = adapter
        wordListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        wordListView.setOnItemClickListener { parent, view, position, id ->
            selectFromList((view as TextView).text.toString(), wordListView.isItemChecked(position))
        }

        adapter.notifyDataSetChanged()
        textView4.setOnClickListener {
            text.invalidate()
        }
    }

    fun selectFromText(word: String) {
        val isInArray = selectedWords.contains(word)
        if (isInArray) selectedWords.remove(word) else selectedWords.add(word)
        wordListView.setItemChecked(words.indexOf(word), !isInArray)
        adapter.notifyDataSetChanged()
        text.invalidate()
    }

    fun selectFromList(word: String, selected: Boolean) {
        if(selected) selectedWords.add(word) else selectedWords.remove(word)
        text.invalidate()
    }

    fun getWordsFromText() {
        words =
            ArrayList(TEST_STR.split(Regex("(?<=[.,/#!\$%^&*;:{}=\\-_`~() ])|(?=[.,/#!\$%^&*;:{}=\\-_`~() ])")))
    }

    fun getKnownWords() {
        knownWords.add("ipsum")
        knownWords.add("sit")
        knownWords.add("adipiscing")
        knownWords.add("exercitation")
        knownWords.add("voluptate")
    }

    fun generateSelectableString() {
        var currentPosition = 0
        val wordsToRemove = ArrayList<String>()
        for (word in words) {
            if (word.length <= 1) {
                currentPosition++
                wordsToRemove.add(word)
            } else {
                val span: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        this@WordSelectorActivity.selectFromText(word)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = if (selectedWords.contains(word)) Color.RED else Color.BLACK
                    }
                }
                ss.setSpan(
                    span,
                    currentPosition,
                    currentPosition + word.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                currentPosition += word.length
            }
        }
        words.removeAll { word -> wordsToRemove.contains(word) }
        Log.i("ar", words.toString())
        words = ArrayList(words.distinct().sortedBy { it })
    }
}
