package com.example.snapitout

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
}
