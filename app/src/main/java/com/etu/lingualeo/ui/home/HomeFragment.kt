package com.etu.lingualeo.ui.home

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.etu.lingualeo.MainActivity
import com.etu.lingualeo.R
import com.etu.lingualeo.restUtil.RestUtil
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    var words = ArrayList<WordListItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView: FastScrollRecyclerView = root.findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        getWords()

        recyclerView.adapter = WordListAdapter(words)
        return root
    }

    fun getMockWords() {
        words.add(
            WordListItem(
                "indifferent",
                "безразличный",
                "https://contentcdn.lingualeo.com/uploads/picture/3053472.png"
            )
        )
        words.add(WordListItem("help", "помощь"))
        words.add(WordListItem("accomplish", "достигать"))
        words.add(WordListItem("acknowledge", "признавать"))
        words.add(WordListItem("acquaintance", "знакомый"))
        words.add(WordListItem("agenda", "повестка дня"))
        words.add(WordListItem("biased", "предвзятый"))
        words = ArrayList(words.map { word ->
            word.word = word.word.capitalize()
            word.translation = word.translation.capitalize()
            word
        })
        words = ArrayList(words.sortedBy { it.word })
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
                        Log.i("test", words.toString())
                        run {
                            if ((status) && (words != null)) {
                                this.words = ArrayList(words.map { word ->
                                    word.word = word.word.capitalize()
                                    word.translation = word.translation.capitalize()
                                    word
                                })
                                this.words = ArrayList(words.sortedBy { it.word })
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
}
