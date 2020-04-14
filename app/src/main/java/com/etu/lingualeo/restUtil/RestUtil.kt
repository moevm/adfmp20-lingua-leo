package com.etu.lingualeo.restUtil

import android.util.Log
import com.beust.klaxon.Klaxon
import com.etu.lingualeo.ui.home.WordListItem
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.lang.Exception

class RestUtil() {

    companion object {
        val instance = RestUtil()
    }

    private val apiBaseUrl = "https://api.lingualeo.com/"
    private val apiLoginUrl = this.apiBaseUrl + "api/login"
    private val apiGetWordsUrl = this.apiBaseUrl + "GetWords"

    private val client = OkHttpClient.Builder()
            .cookieJar(object : CookieJar {
                private val cookieStore: HashMap<HttpUrl, List<Cookie>> = HashMap()
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieStore[url] = cookies
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    val cookies = cookieStore[url]
                    return cookies ?: ArrayList()
                }
            })
            .build()

    private fun get(url: String, params: HashMap<String, String>?, responseCallback: Callback) {
        val httpBuilder = url.toHttpUrlOrNull()?.newBuilder()
        if (params != null) {
            for ((key, value) in params) {
                httpBuilder!!.addQueryParameter(key, value)
            }
        }
        val request = httpBuilder!!.build().let { Request.Builder().url(it).build() }
        client.newCall(request).enqueue(responseCallback)
    }

    private fun post(url: String, json: String = "", responseCallback: Callback) {
        val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
        client.newCall(request).enqueue(responseCallback)
    }

    fun login(email: String, password: String, onResult: (status: Boolean) -> Unit) {
        this.get(this.apiLoginUrl, hashMapOf("email" to email, "password" to password), object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseJsonString = response.body!!.string()
                    val loginResponse = Klaxon().parse<LoginResponseData>(responseJsonString)
                    onResult(true)
                } catch (e: Exception) {
                    onResult(false)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println(e.toString())
                onResult(false)
            }
        })
    }

    fun getWords(onResult: (status: Boolean, words: ArrayList<WordListItem>?) -> Unit) {
        val params = """
            {
                "apiVersion": "1.0.1",
                "attrList": {
                    "id": "id",
                    "wordValue": "wd",
                    "origin": "wo",
                    "wordType": "wt",
                    "translations": "trs",
                    "wordSets": "ws",
                    "created": "cd",
                    "learningStatus": "ls",
                    "progress": "pi",
                    "transcription": "scr",
                    "pronunciation": "pron",
                    "relatedWords": "rw",
                    "association": "as",
                    "trainings": "trainings",
                    "listWordSets": "listWordSets",
                    "combinedTranslation": "trc",
                    "picture": "pic",
                    "speechPartId": "pid",
                    "wordLemmaId": "lid",
                    "wordLemmaValue": "lwd"
                },
                "category": "",
                "dateGroup": "start",
                "mode": "basic",
                "perPage": 30,
                "status": "",
                "wordSetId": 1,
                "offset": null,
                "search": "",
                "training": null,
                "ctx": {
                    "config": {
                        "isCheckData": true,
                        "isLogging": true
                    }
                }
            }
        """.trimIndent()
        this.post(this.apiGetWordsUrl, responseCallback = object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseJsonString = response.body!!.string()
                    Log.i("test", responseJsonString)
                    val getWordsResponse = Klaxon().parse<GetWordsResponseData>(responseJsonString)
                    val wordList = arrayListOf<WordListItem>()
                    if (getWordsResponse != null) {
                        for (item: WordItemData in getWordsResponse.data[0].words) {
                            wordList.add(WordListItem(
                                    word = item.wordValue,
                                    translation = item.combinedTranslation,
                                    imageUrl = item.picture
                            ))
                        }
                        onResult(true, wordList)
                    } else {
                        throw IOException("GetWords returned null value")
                    }
                } catch (e: Exception) {
                    onResult(false, null)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println(e.toString())
                onResult(false, null)
            }
        })
    }

}

data class LoginResponseData(val rev: Number, val user: UserData)
data class UserData(val fname: String, val avatar: String)
class GetWordsResponseData(val data: Array<GetWordsData>)
class GetWordsData(val words: Array<WordItemData>)
data class WordItemData(val wordValue: String, val combinedTranslation: String, val picture: String)