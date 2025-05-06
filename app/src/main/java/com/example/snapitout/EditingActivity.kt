package com.example.snapitout

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class EditingActivity : AppCompatActivity() {

    private lateinit var saveButton: Button
    private lateinit var retakeButton: Button

    private lateinit var colorCircles: List<View>
    private lateinit var stickers: List<ImageView>
    private lateinit var mainFrames: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editing)

        // Buttons
        saveButton = findViewById(R.id.saveButton)
        retakeButton = findViewById(R.id.retakeButton)

        // Color circle views
        colorCircles = listOf(
            findViewById(R.id.colorCircle1),
            findViewById(R.id.colorCircle2),
            findViewById(R.id.colorCircle3),
            findViewById(R.id.colorCircle4),
            findViewById(R.id.colorCircle5),
            findViewById(R.id.colorCircle6)
        )

        // Sticker image views
        stickers = listOf(
            findViewById(R.id.sticker1),
            findViewById(R.id.sticker2),
            findViewById(R.id.sticker3),
            findViewById(R.id.sticker4),
            findViewById(R.id.sticker5)
        )

        // Frame images (updated to match your layout)
        mainFrames = listOf(
            findViewById(R.id.mainFrame1),
            findViewById(R.id.mainFrame2),
            findViewById(R.id.mainFrame3),
            findViewById(R.id.mainFrame4)
        )

        // Load photos from intent
        val photoPaths = intent.getStringArrayListExtra("photoPaths")

        if (photoPaths != null && photoPaths.size >= 4) {
            for (i in 0 until 4) {
                val bitmap = BitmapFactory.decodeFile(photoPaths[i])
                mainFrames[i].setImageBitmap(bitmap)
            }
        } else {
            Toast.makeText(this, "No photos to display", Toast.LENGTH_SHORT).show()
        }

        // Set listeners for color selection
        colorCircles.forEachIndexed { index, view ->
            view.setOnClickListener {
                Toast.makeText(this, "Color ${index + 1} selected", Toast.LENGTH_SHORT).show()
                // TODO: Apply frame color change logic
            }
        }

        // Set listeners for sticker selection
        stickers.forEachIndexed { index, sticker ->
            sticker.setOnClickListener {
                Toast.makeText(this, "Sticker ${index + 1} selected", Toast.LENGTH_SHORT).show()
                // TODO: Apply sticker logic
            }
        }

        // Save button logic
        saveButton.setOnClickListener {
            Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show()
            // TODO: Add save functionality
        }

        // Retake button logic
        retakeButton.setOnClickListener {
            Toast.makeText(this, "Retake initiated!", Toast.LENGTH_SHORT).show()
            // TODO: Add retake functionality (e.g. go back to CameraActivity)
        }
    }
}
