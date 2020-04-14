package com.etu.lingualeo.ui.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.etu.lingualeo.R
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.SectionedAdapter
import com.squareup.picasso.Picasso

class WordListAdapter(val items: ArrayList<WordListItem>) :
    RecyclerView.Adapter<WordListAdapter.ViewHolder>(), SectionedAdapter {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.word_list_item
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position)
        holder.word.setText(item.word)
        holder.translation.setText(item.translation)
        if(item.imageUrl != null && item.imageUrl != "") Picasso.get().load(item.imageUrl).into(holder.image)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getSectionName(position: Int): String {
        return items.get(position).word.toCharArray().first().toString()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var word: TextView
        var translation: TextView
        var image: ImageView

        init {
            word = itemView.findViewById(R.id.primary_text)
            translation = itemView.findViewById(R.id.sub_text)
            image = itemView.findViewById(R.id.media_image)
        }
    }
}

class WordListItem(var word: String, var translation: String, var imageUrl: String? = null, var wordId: Number)