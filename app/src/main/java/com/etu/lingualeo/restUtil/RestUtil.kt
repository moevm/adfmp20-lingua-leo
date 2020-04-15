package com.etu.lingualeo.restUtil

import android.util.Log
import com.beust.klaxon.Json
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
    private val apiGetTranslatesUrl = this.apiBaseUrl + "getTranslates"
    private val apiSetWordsUrl = this.apiBaseUrl + "SetWords"
    private val apiUploadImageUrl = "https://upload.lingualeo.com/UploadImage"

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

    // Авторизация
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

    // Получение списка слов для главного экрана
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
        this.post(this.apiGetWordsUrl, params, responseCallback = object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseJsonString = response.body!!.string()
                    Log.i("test", responseJsonString)
                    val getWordsResponse = Klaxon().parse<GetWordsResponseData>(responseJsonString)
                    val wordList = arrayListOf<WordListItem>()
                    if (getWordsResponse != null) {
                        for (dataItem: GetWordsData in getWordsResponse.data) {
                            for (item: WordItemData in dataItem.words) {
                                var translation: String = item.combinedTranslation
                                if (item.combinedTranslation.contains(';')) {
                                    translation = item.combinedTranslation.split(';')[0]
                                }
                                wordList.add(WordListItem(
                                        word = item.wordValue,
                                        translation = translation,
                                        imageUrl = item.picture,
                                        wordId = item.id
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

    // Получение списка переводов
    fun getTranslations(word: String, onResult: (status: Boolean, translation: TranslationResponseData?) -> Unit) {
        this.post(this.apiGetTranslatesUrl, Klaxon().toJsonString(TranslationRequestData(word)), responseCallback = object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val translationResponseData = response.body?.string()?.let { Klaxon().parse<TranslationResponseData>(it) }
                    onResult(true, translationResponseData)
                } catch (e: Exception) {
                    println(e.toString())
                    onResult(false, null)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println(e.toString())
                onResult(false, null)
            }
        })
    }

    // Добавление слова в словарь
    fun addWord(wordId: Number, translationId: Number, onResult: (status: Boolean) -> Unit) {
        val setWordsRequestData = SetWordsRequestData(
                data = listOf(SetWordsData(
                        wordIds = listOf(wordId),
                        valueList = SetWordsValueListData(
                                translation = SetWordsTranslationData(
                                        id = translationId,
                                        main = translationId,
                                        selected = translationId
                                )
                        )
                ))
        )
        this.post(this.apiSetWordsUrl, Klaxon().toJsonString(setWordsRequestData), responseCallback = object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val setWordsResponseData = response.body?.string()?.let { Klaxon().parse<SetWordsResponseData>(it) }
                    onResult(true)
                } catch (e: Exception) {
                    println(e.toString())
                    onResult(false)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println(e.toString())
                onResult(false)
            }
        })
    }

    // Удаление слова из словаря
    fun deleteWord(wordId: Number, onResult: (status: Boolean) -> Unit) {
        val deleteWordRequestData = DeleteWordRequestData(
                data = listOf(DeleteWordsData(
                        wordIds = listOf(wordId),
                        valueList = DeleteWordsValueListData()
                ))
        )
        this.post(this.apiSetWordsUrl, Klaxon().toJsonString(deleteWordRequestData), responseCallback = object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val setWordsResponseData = response.body?.string()?.let { Klaxon().parse<SetWordsResponseData>(it) }
                    onResult(true)
                } catch (e: Exception) {
                    println(e.toString())
                    onResult(false)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println(e.toString())
                onResult(false)
            }
        })
    }

    // Загрузка изображения на сервер
    fun uploadPicture(base64: String, onResult: (status: Boolean, url: String?) -> Unit) {
        val uploadImageRequestData = UploadImageRequestData(base64)
        this.post(this.apiUploadImageUrl, Klaxon().toJsonString(uploadImageRequestData), responseCallback = object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val uploadImageResponseData = response.body?.string()?.let { Klaxon().parse<UploadImageResponseData>(it) }
                    if ((uploadImageResponseData == null) || (uploadImageResponseData.status != "ok")) {
                        throw IOException("Image uploading error")
                    }
                    onResult(true, uploadImageResponseData.url)
                } catch (e: Exception) {
                    println(e.toString())
                    onResult(false, null)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println(e.toString())
                onResult(false, null)
            }
        })
    }

    fun changePicture(wordId: Number, url: String, onResult: (status: Boolean) -> Unit) {
        val changePictureRequestData = ChangePictureRequestData(
                data = listOf(ChangePictureData(
                        wordIds = listOf(wordId),
                        valueList = ChangePictureValueListData(
                                translation = ChangePictureTranslationData(
                                        pic = url
                                )
                        )
                ))
        )
        this.post(this.apiSetWordsUrl, Klaxon().toJsonString(changePictureRequestData), responseCallback = object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val setWordsResponseData = response.body?.string()?.let { Klaxon().parse<SetWordsResponseData>(it) }
                    if ((setWordsResponseData == null) || (setWordsResponseData.status != "ok")) {
                        throw IOException("Change image error")
                    }
                    onResult(true)
                } catch (e: Exception) {
                    println(e.toString())
                    onResult(false)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println(e.toString())
                onResult(false)
            }
        })
    }

    fun changeTranslation(wordId: Number, translationId: Number, onResult: (status: Boolean) -> Unit) {
        val changeTranslationRequestData = ChangeTranslationRequestData(
                data = listOf(ChangeTranslationData(
                        wordIds = listOf(wordId),
                        valueList = ChangeTranslationValueListData(
                                translation = ChangeTranslationTranslationData(
                                        id = translationId
                                )
                        )
                ))
        )
        this.post(this.apiSetWordsUrl, Klaxon().toJsonString(changeTranslationRequestData), responseCallback = object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val setWordsResponseData = response.body?.string()?.let { Klaxon().parse<SetWordsResponseData>(it) }
                    if ((setWordsResponseData == null) || (setWordsResponseData.status != "ok")) {
                        throw IOException("Change translation error")
                    }
                    onResult(true)
                } catch (e: Exception) {
                    println(e.toString())
                    onResult(false)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println(e.toString())
                onResult(false)
            }
        })
    }

}

data class LoginRequestData(
        val credentials: LoginCredentialsData,
        val type: String = "mixed"
)

data class LoginCredentialsData(
        val email: String,
        val password: String
)

data class LoginResponseData(
        val accessToken: String,
        val refreshToken: String,
        val expiredAt: Number,
        val userId: Number
)

class GetWordsResponseData(val data: Array<GetWordsData>)

class GetWordsData(val words: Array<WordItemData>)

data class WordItemData(
        val wordValue: String,
        val combinedTranslation: String,
        val picture: String,
        val id: Number
)

data class TranslationRequestData(val text: String)

data class TranslationResponseData(
        @Json(name = "word_id")
        val wordId: Number,

        @Json(name = "word_value")
        val word: String,

        @Json(name = "translate")
        val translations: List<TranslationData>
)

data class TranslationData(
        @Json(name = "id")
        val translationId: Number,

        @Json(name = "value")
        val translation: String,

        val votes: Number,

        @Json(name = "pic_url")
        val picture: String
)

data class SetWordsRequestData(
        val op: String = "actionWithWords {action: add}",
        val data: List<SetWordsData>
)

data class SetWordsData(
        val action: String = "add",
        val mode: String = "0",
        val wordIds: List<Number>,
        val valueList: SetWordsValueListData
)

data class SetWordsValueListData(
        val wordSetId: Number = 1,
        val translation: SetWordsTranslationData
)

data class SetWordsTranslationData(
        val id: Number,
        val main: Number,
        val selected: Number
)

data class SetWordsResponseData(val status: String)

data class DeleteWordRequestData(
        val op: String = "groupActionWithWords {action: delete}",
        val data: List<DeleteWordsData>
)

data class DeleteWordsData(
        val action: String = "delete",
        val mode: String = "delete",
        val wordSetId: Number = 1,
        val wordIds: List<Number>,
        val valueList: DeleteWordsValueListData
)

data class DeleteWordsValueListData(val globalSetId: Number = 1)

data class UploadImageRequestData(
        val base64: String,
        val directory: String = "uploads/picture/translation",
        val apiVersion: String = "1.0.0"
)

data class UploadImageResponseData(
        val url: String,
        val status: String
)

data class ChangePictureRequestData(
        val op: String = "new_updateWordAttr",
        val data: List<ChangePictureData>
)

data class ChangePictureData(
        val action: String = "update",
        val mode: String = "update",
        val wordIds: List<Number>,
        val valueList: ChangePictureValueListData
)

data class ChangePictureValueListData(
        val translation: ChangePictureTranslationData
)

data class ChangePictureTranslationData(
        val pic: String,
        val id: Number = 1
)

data class ChangeTranslationRequestData(
        val op: String = "new_updateWordAttr",
        val data: List<ChangeTranslationData>
)

data class ChangeTranslationData(
        val action: String = "update",
        val mode: String = "update",
        val wordIds: List<Number>,
        val valueList: ChangeTranslationValueListData
)

data class ChangeTranslationValueListData(
        val translation: ChangeTranslationTranslationData
)

data class ChangeTranslationTranslationData(
        val id: Number,
        val selected: Number = 1,
        val main: Number = 1
)