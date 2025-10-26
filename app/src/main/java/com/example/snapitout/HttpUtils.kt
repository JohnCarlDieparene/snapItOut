package com.example.snapitout.utils

import okhttp3.*
import java.io.IOException

object HttpUtils {

    private val client = OkHttpClient()

    fun postForm(url: String, formBody: FormBody, callback: Callback) {
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()
        client.newCall(request).enqueue(callback)
    }
}