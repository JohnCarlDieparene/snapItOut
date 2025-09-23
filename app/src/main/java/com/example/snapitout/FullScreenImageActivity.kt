package com.example.snapitout

import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import kotlin.math.abs

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var photoView: PhotoView
    private lateinit var indicatorText: TextView

    private var imageUris: List<String> = listOf()
    private var currentIndex = 0

    // Touch position trackers
    private var startX = 0f
    private var startY = 0f

    private val SWIPE_THRESHOLD = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        photoView = findViewById(R.id.fullScreenImageView)
        indicatorText = findViewById(R.id.indicatorText)

        // Get data from intent
        imageUris = intent.getStringArrayListExtra("imageUris") ?: listOf()
        currentIndex = intent.getIntExtra("currentIndex", 0)

        if (imageUris.isEmpty()) {
            finish()
            return
        }

        loadImage()

        // Manual swipe detection
        photoView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val endX = event.x
                    val endY = event.y

                    val diffX = endX - startX
                    val diffY = endY - startY

                    if (abs(diffX) > abs(diffY)) {
                        // Horizontal swipe
                        if (abs(diffX) > SWIPE_THRESHOLD) {
                            if (diffX > 0) {
                                showPreviousImage()
                            } else {
                                showNextImage()
                            }
                        }
                    } else {
                        // Vertical swipe (only down is allowed)
                        if (diffY > SWIPE_THRESHOLD) {
                            finish()
                            overridePendingTransition(0, android.R.anim.fade_out)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun loadImage() {
        val uri = Uri.parse(imageUris[currentIndex])
        Glide.with(this).load(uri).into(photoView)
        indicatorText.text = "${currentIndex + 1} / ${imageUris.size}"
    }

    private fun showNextImage() {
        if (currentIndex < imageUris.size - 1) {
            currentIndex++
            loadImage()
        }
    }

    private fun showPreviousImage() {
        if (currentIndex > 0) {
            currentIndex--
            loadImage()
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
}
