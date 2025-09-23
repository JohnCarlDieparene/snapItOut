package com.example.snapitout

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class PhotoCollabActivity : AppCompatActivity() {

    private lateinit var overlayImage1: ImageView
    private lateinit var overlayImage2: ImageView
    private lateinit var overlayImage3: ImageView
    private lateinit var overlayImage4: ImageView
    private lateinit var selectPhotosButton: Button
    private lateinit var editButton: Button
    private lateinit var homeButton: ImageView
    private lateinit var albumButton: ImageView

    private var selectedImages = mutableListOf<Uri>()

    private val selectImages =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            selectedImages.clear()
            selectedImages.addAll(uris.take(4)) // Limit to 4 images
            updateOverlayImages()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photocollab)

        overlayImage1 = findViewById(R.id.overlayImage1)
        overlayImage2 = findViewById(R.id.overlayImage2)
        overlayImage3 = findViewById(R.id.overlayImage3)
        overlayImage4 = findViewById(R.id.overlayImage4)
        selectPhotosButton = findViewById(R.id.button)
        editButton = findViewById(R.id.button2)
        homeButton = findViewById(R.id.imageView23)
        albumButton = findViewById(R.id.imageView24)


        homeButton.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        albumButton.setOnClickListener {
            startActivity(Intent(this, AlbumActivity::class.java))
            finish()
        }

        selectPhotosButton.setOnClickListener {
            selectImages.launch("image/*")
        }

        editButton.setOnClickListener {
            if (selectedImages.isNotEmpty()) {
                val intent = Intent(this, EditingActivity::class.java).apply {
                    putParcelableArrayListExtra("selected_images", ArrayList(selectedImages))
                }
                startActivity(intent)
            }
        }
    }

    private fun updateOverlayImages() {
        val overlays = listOf(overlayImage1, overlayImage2, overlayImage3, overlayImage4)
        overlays.forEach { it.setImageDrawable(null); it.visibility = ImageView.GONE }

        selectedImages.forEachIndexed { index, uri ->
            overlays[index].setImageURI(uri)
            overlays[index].visibility = ImageView.VISIBLE
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
        if (hasFocus) {
            hideSystemUI()
        }
    }
}
