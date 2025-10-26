package com.example.snapitout.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object RaphaelApiClient {

    private val client = OkHttpClient()

    /**
     * Generate an AI image from Raphael.
     *
     * @param prompt Text prompt for Raphael AI
     * @param base64Images Optional list of Base64 images to include in the collage
     * @param apiKey Optional API key for Raphael AI
     * @param onResult Callback with generated Bitmap or null on failure
     */
    fun generateImage(
        prompt: String,
        base64Images: List<String> = emptyList(),
        apiKey: String? = null,
        onResult: (Bitmap?) -> Unit
    ) {
        val url = "https://raphael.app/generate" // Raphael AI endpoint

        // Build JSON payload
        val json = JSONObject().apply {
            put("prompt", prompt)
            put("style", "scrapbook")
            if (base64Images.isNotEmpty()) {
                val arr = JSONArray()
                base64Images.forEach { arr.put(it) }
                put("images", arr)
            }
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = RequestBody.create(mediaType, json.toString())

        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)

        // Add API key header if provided
        apiKey?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                    val bitmap = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                    onResult(bitmap)
                } else {
                    println("Raphael API error: ${response.code} - ${response.message}")
                    onResult(null)
                }
            }
        })
    }
}