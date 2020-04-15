package com.etu.lingualeo.ui.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.etu.lingualeo.R
import com.etu.lingualeo.restUtil.RestUtil
import com.etu.lingualeo.wordTranslationSelector.WordTranslationSelectorActivity
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.ByteArrayOutputStream


const val RESULT_PICKER = 3

class HomeFragment : SearchView.OnQueryTextListener, Fragment() {

    var words = ArrayList<WordListItem>()
    var wordsShown = ArrayList<WordListItem>()
    var currentWordImagePickerId = 0

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
        adapter =  WordListAdapter(wordsShown)
        recyclerView.adapter = adapter
        registerForContextMenu(recyclerView)
        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        wordsShown.clear()
        if(newText != null && newText != "") {
            val newWords = words.filter { word -> word.word.contains(newText.capitalize()) || word.translation.contains(newText.capitalize()) }
            for(word in newWords) {
                wordsShown.add(word)
            }
        } else {
            for(word in words) {
                wordsShown.add(word)
            }
        }
        adapter.notifyDataSetChanged()
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.toString()) {
            "Изменить изображение" -> openWordImagePicker(adapter.position)
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
                    activity?.runOnUiThread {
                        progressBar?.visibility = View.GONE
                        statusText?.text = "Неверный логин или пароль"
                        statusText?.visibility = View.VISIBLE
                    }
                } else {
                    RestUtil.instance.getWords { status: Boolean, words: ArrayList<WordListItem>? ->
                        run {
                            if ((status) && (words != null)) {
                                this.words.clear()
                                this.wordsShown.clear()
                                for(word in words) {
                                    word.word = word.word.capitalize()
                                    word.translation = word.translation.capitalize()
                                    this.words.add(word)
                                }
                                this.words.sortBy { it.word }
                                for(word in this.words) {
                                    wordsShown.add(word)
                                }
                                activity?.runOnUiThread { adapter.notifyDataSetChanged() }
                            } else {
                                activity?.runOnUiThread {
                                    statusText?.text = "Ваш словарь пуст"
                                    statusText?.visibility = View.VISIBLE
                                }
                            }
                            activity?.runOnUiThread { progressBar?.visibility = View.GONE }
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

    fun openWordImagePicker(wordId: Int) {
        currentWordImagePickerId = wordId
        val intentGallery = Intent(Intent.ACTION_GET_CONTENT);
        intentGallery.addCategory(Intent.CATEGORY_OPENABLE);
        intentGallery.setType("image/*")
        startActivityForResult(intentGallery, RESULT_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_PICKER && resultCode == Activity.RESULT_OK) {
            val fileName = data!!.data!!
            val picture = MediaStore.Images.Media.getBitmap(context?.contentResolver, fileName);
            val pictureEncoded = encodeImage(picture)
            if(pictureEncoded != null) {
                RestUtil.instance.uploadPicture(pictureEncoded, {status, url ->
                    run {
                        if(url != null) {
                            RestUtil.instance.changePicture(currentWordImagePickerId, url, {status ->
                                if(status) {
                                    activity!!.runOnUiThread { getWords() }
                                }
                            })
                        }
                    }
                })
            }
        }
    }

    private fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }
}
