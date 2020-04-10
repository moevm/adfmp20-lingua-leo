package com.etu.lingualeo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.etu.lingualeo.R
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    var words = ArrayList<WordListItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView: FastScrollRecyclerView = root.findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        getMockWords()

        recyclerView.adapter = WordListAdapter(words)
        return root
    }

    fun getMockWords() {
        words.add(WordListItem("indifferent", "безразличный", "https://contentcdn.lingualeo.com/uploads/picture/3053472.png"))
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
}
