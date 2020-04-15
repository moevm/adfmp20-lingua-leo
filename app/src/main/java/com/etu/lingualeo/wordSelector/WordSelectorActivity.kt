package com.etu.lingualeo.wordSelector

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.etu.lingualeo.R
import com.etu.lingualeo.restUtil.RestUtil
import com.etu.lingualeo.wordTranslationSelector.WordTranslationSelectorActivity
import kotlinx.android.synthetic.main.word_selector_activity.*
import me.saket.bettermovementmethod.BetterLinkMovementMethod

class WordSelectorActivity : AppCompatActivity() {

    var words = ArrayList<String>()
    var selectedWords = ArrayList<String>()
    val knownWords = ArrayList<String>()
    var ss = SpannableString(" ")

    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_save -> {
            val intent = Intent(this, WordTranslationSelectorActivity::class.java)
            intent.putExtra("words", ArrayList(selectedWords.sortedBy { it }))
            startActivity(intent)
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        when (item.toString()) {
            "Выбрать все" -> {
                for (i in 0 until adapter.getCount()) {
                    wordListView.setItemChecked(i, true)
                    selectFromList(words[i], true)
                }
            }
        }
        return true
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(0, v.id, 0, "Выбрать все")
        menu.add(0, v.id, 0, "Выбрать отсутствующие в словаре")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.word_selector_activity)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "Выбор слов из текста"

        ss = SpannableString(intent.getStringExtra("text"))
        val _words = getWordsFromText()
        getKnownWords(_words)
        text.setMovementMethod(BetterLinkMovementMethod.getInstance());
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
        registerForContextMenu(textView5)
        textView5.setOnClickListener { textView5.performLongClick() }
    }

    fun selectFromText(word: String) {
        val isInArray = selectedWords.contains(word)
        if (isInArray) selectedWords.remove(word) else selectedWords.add(word)
        wordListView.setItemChecked(words.indexOf(word), !isInArray)
        adapter.notifyDataSetChanged()
        text.invalidate()
    }

    fun selectFromList(word: String, selected: Boolean) {
        if (selected) selectedWords.add(word) else selectedWords.remove(word)
        text.invalidate()
    }

    fun getWordsFromText(): ArrayList<String> {
        return ArrayList(ss.toString().split(Regex("(?<=[.,/#!?\$%^&*;:{}=\\-_`~() \n\"'])|(?=[.,/#!?\$%^&*;:{}=\\-_`~() \n\"'])")))
    }

    fun generateSelectableString(_words: ArrayList<String>) {
        var currentPosition = 0
        val wordsToRemove = ArrayList<String>()
        for (word in _words) {
            if (word.length <= 1 || knownWords.contains(word.toLowerCase())) {
                currentPosition += word.length
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
        _words.removeAll { word -> wordsToRemove.contains(word) }
        val temp = ArrayList(_words.distinct().sortedBy { it })
        for (word in temp) {
            words.add(word)
        }
        runOnUiThread {
            text.setText(ss)
            adapter.notifyDataSetChanged()
            text.invalidate()
        }
    }

    fun getKnownWords(_words: ArrayList<String>) {
        knownWords.add("I")
        knownWords.add("you")
        knownWords.add("he")
        knownWords.add("she")
        knownWords.add("it")
        knownWords.add("we")
        knownWords.add("they")
        knownWords.add("as")
        knownWords.add("at")
        knownWords.add("by")
        knownWords.add("but")
        knownWords.add("in")
        knownWords.add("of")
        knownWords.add("to")
        knownWords.add("the")
        RestUtil.instance.getWords({ status, words ->
            if (status) {
                if (words != null) {
                    for (word in words) {
                        knownWords.add(word.word)
                    }
                }
                generateSelectableString(_words)
            }
        })
    }
}
