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
    private val apiLoginUrl = "https://lingualeo.com/auth"
    private val apiGetWordsUrl = this.apiBaseUrl + "GetWords"

    private val apiHeaderReferer = "https://lingualeo.com/ru"

    private val client = OkHttpClient.Builder()
            .cookieJar(object : CookieJar {
                private var cookies: List<Cookie> = listOf()
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    this.cookies = cookies
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return this.cookies
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

    private fun post(url: String, json: String = "", customHeaders: HashMap<String, String>? = null, responseCallback: Callback) {
        val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = json.toRequestBody(jsonMediaType)
        var requestBuilder = Request.Builder().url(url)
        if (customHeaders != null) {
            for ((header, value) in customHeaders) {
                requestBuilder = requestBuilder.addHeader(header, value)
            }
        }
        requestBuilder = requestBuilder.post(body)
        val request = requestBuilder.build()
        client.newCall(request).enqueue(responseCallback)
    }

    fun login(email: String, password: String, onResult: (status: Boolean) -> Unit) {
        val refererHeaders = hashMapOf<String, String>("Referer" to this.apiHeaderReferer)
        val loginRequestData = LoginRequestData(LoginCredentialsData(email, password))
        this.post(this.apiLoginUrl, Klaxon().toJsonString(loginRequestData), refererHeaders, object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val loginResponseData = response.body?.string()?.let { Klaxon().parse<LoginResponseData>(it) }
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
        this.post(this.apiGetWordsUrl, params, hashMapOf("Content-type" to "application/json"), object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseJsonString = response.body!!.string()
                    Log.i("test", responseJsonString)
                    val getWordsResponse = Klaxon().parse<GetWordsResponseData>(responseJsonString)
                    val wordList = arrayListOf<WordListItem>()
                    if (getWordsResponse != null) {
                        for (dataItem: GetWordsData in getWordsResponse.data) {
                            for (item: WordItemData in dataItem.words) {
                                wordList.add(WordListItem(
                                        word = item.wordValue,
                                        translation = item.combinedTranslation,
                                        imageUrl = item.picture
                                ))
                            }
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

data class LoginRequestData(val credentials: LoginCredentialsData, val type: String = "mixed")
data class LoginCredentialsData(val email: String, val password: String)
data class LoginResponseData(val accessToken: String, val refreshToken: String, val expiredAt: Number, val userId: Number)

class GetWordsResponseData(val data: Array<GetWordsData>)
class GetWordsData(val words: Array<WordItemData>)
data class WordItemData(val wordValue: String, val combinedTranslation: String, val picture: String)