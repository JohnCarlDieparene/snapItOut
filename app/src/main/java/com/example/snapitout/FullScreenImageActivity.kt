package com.example.snapitout

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        // Initialize the ImageView
        imageView = findViewById(R.id.full_screen_image_view)

        // Get the image URI passed from the CameraActivity
        val imageUriString = intent.getStringExtra("image_uri")
        if (!imageUriString.isNullOrEmpty()) {
            val imageUri = Uri.parse(imageUriString)

            // Set the image URI to the ImageView
            imageView.setImageURI(imageUri)
        } else {
            // Handle error: URI not passed properly
            // Show a default image or an error message
        }
    }
}
