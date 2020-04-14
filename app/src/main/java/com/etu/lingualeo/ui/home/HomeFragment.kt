package com.etu.lingualeo.ui.home

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.etu.lingualeo.MainActivity
import com.etu.lingualeo.R
import com.etu.lingualeo.restUtil.RestUtil
import com.etu.lingualeo.wordTranslationSelector.WordTranslationSelectorActivity
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    var words = ArrayList<WordListItem>()

    lateinit var adapter: WordListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView: FastScrollRecyclerView = root.findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        getWords()
        adapter =  WordListAdapter(words)
        recyclerView.adapter = adapter
        registerForContextMenu(recyclerView)
        return root
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.toString()) {
            "Выбрать перевод" -> launchChangeTranslation(adapter.position)
            "Удалить" -> deleteWord(adapter.position)
        }
        return super.onContextItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        getWords()
    }

    fun getWords() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val login = preferences.getString("ll_login", null)
        val password = preferences.getString("ll_password", null)
        RestUtil.instance.login(login.toString(), password.toString(), { status ->
            run {
                if (!status) {
                    activity!!.runOnUiThread {
                        progressBar.visibility = View.GONE
                        statusText.text = "Неверный логин или пароль"
                        statusText.visibility = View.VISIBLE
                    }
                } else {
                    RestUtil.instance.getWords { status: Boolean, words: ArrayList<WordListItem>? ->
                        run {
                            if ((status) && (words != null)) {
                                this.words.clear()
                                for(word in words) {
                                    word.word = word.word.capitalize()
                                    word.translation = word.translation.capitalize()
                                    this.words.add(word)
                                }
                                this.words.sortBy { it.word }
                                activity!!.runOnUiThread { adapter.notifyDataSetChanged() }
                            } else {
                                activity!!.runOnUiThread {
                                    statusText.text = "Ваш словарь пуст"
                                    statusText.visibility = View.VISIBLE
                                }
                            }
                            activity!!.runOnUiThread { progressBar.visibility = View.GONE }
                        }
                    }
                }
            }
        })
    }

    fun launchChangeTranslation(wordId: Int) {
        val word = words.find { word -> word.wordId == wordId }
        if(word != null) {
            val array = ArrayList<String>()
            array.add(word.word)
            val intent = Intent(context, WordTranslationSelectorActivity::class.java)
            intent.putExtra("words", array)
            startActivity(intent)
        }
    }

    fun deleteWord(wordId: Int) {
        val word = words.find { word -> word.wordId == wordId }
        activity!!.runOnUiThread { Toast.makeText(context, "Удаление...", Toast.LENGTH_SHORT).show() }
        if(word != null) {
            RestUtil.instance.deleteWord(wordId, {status ->
                if(status) {
                    activity!!.runOnUiThread { getWords() }
                }
            })
        }
    }
}
