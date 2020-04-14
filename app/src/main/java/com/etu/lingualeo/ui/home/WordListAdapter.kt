package com.etu.lingualeo.ui.home

import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
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

    var position = 0

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
        if (item.imageUrl != null && item.imageUrl != "") Picasso.get().load(item.imageUrl).into(
            holder.image
        )
        holder.itemView.setOnLongClickListener {
            this.position = items[holder.position].wordId
            false
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getSectionName(position: Int): String {
        return items.get(position).word.toCharArray().first().toString()
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder)
    }

    class ViewHolder(itemView: View) : View.OnCreateContextMenuListener,
        RecyclerView.ViewHolder(itemView) {
        var word: TextView
        var translation: TextView
        var image: ImageView

        init {
            word = itemView.findViewById(R.id.primary_text)
            translation = itemView.findViewById(R.id.sub_text)
            image = itemView.findViewById(R.id.media_image)
            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu,
            v: View,
            menuInfo: ContextMenuInfo?
        ) {
            menu.add(0, v.id, 0, "Изменить изображение")
            menu.add(0, v.id, 0, "Выбрать перевод")
            menu.add(0, v.id, 0, "Удалить")
        }
    }
}

class WordListItem(var word: String, var translation: String, var imageUrl: String? = null, var wordId: Int)