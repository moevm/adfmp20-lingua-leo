package com.etu.lingualeo.restUtil

import com.beust.klaxon.Klaxon
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.lang.Exception

class RestUtil() {

    private val apiBaseUrl = "https://api.lingualeo.com/"
    private val apiLoginUrl = this.apiBaseUrl + "api/login"

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

    fun login(email: String, password: String, onResult: (status: Boolean) -> Unit) {
        this.get(this.apiLoginUrl, hashMapOf("email" to email, "password" to password), object: Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseJsonString = response.body!!.string()
                    val loginResponse = Klaxon().parse<LoginResponseData>(responseJsonString)
                    if (loginResponse != null) {
                        println(loginResponse.user.fname)
                    }
                    onResult(true)
                } catch(e: Exception) {
                    onResult(false)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Login Failure")
                println(e.toString())
                onResult(false)
            }
        })
    }

}

data class LoginResponseData(val rev: Number, val user: UserData)

data class UserData(val fname: String, val avatar: String)