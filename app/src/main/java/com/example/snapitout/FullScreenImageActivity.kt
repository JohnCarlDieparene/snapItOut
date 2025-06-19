package com.example.snapitout

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class FullScreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)

        val fullScreenImageView: ImageView = findViewById(R.id.fullScreenImageView)

        // Use Glide to load the image in full-screen view
        Glide.with(this)
            .load(imageUri)
            .into(fullScreenImageView)
    }
}
