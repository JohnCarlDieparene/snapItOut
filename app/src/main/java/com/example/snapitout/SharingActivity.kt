package com.example.snapitout

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class SharingActivity : AppCompatActivity() {

    private lateinit var mainFrames: List<ImageView>
    private lateinit var cameraButton: ImageView
    private lateinit var homeButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sharing)

        // Reference ImageViews from layout
        mainFrames = listOf(
            findViewById(R.id.photo1)
        )

        // Buttons for navigation
        cameraButton = findViewById(R.id.camcam)
        homeButton = findViewById(R.id.homehome)

        // Try to receive new "sharedImagePaths" from EditingActivity
        val sharedImagePaths = intent.getStringArrayListExtra("sharedImagePaths")

        if (!sharedImagePaths.isNullOrEmpty()) {
            // Loop through the shared image paths and display them
            for (i in sharedImagePaths.indices) {
                if (i < mainFrames.size) {
                    val imagePath = sharedImagePaths[i]
                    val uri = Uri.parse(imagePath)

                    // Check if it's a content URI (for scoped storage)
                    if (imagePath.startsWith("content://")) {
                        val inputStream = contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        mainFrames[i].setImageBitmap(bitmap)
                        mainFrames[i].visibility = View.VISIBLE
                    } else {
                        val file = File(imagePath)
                        if (file.exists()) {
                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                            mainFrames[i].setImageBitmap(bitmap)
                            mainFrames[i].visibility = View.VISIBLE
                        } else {
                            Toast.makeText(this, "Image file not found at: $imagePath", Toast.LENGTH_SHORT).show()
                            mainFrames[i].visibility = View.GONE
                        }
                    }
                }
            }

            // Optionally hide the remaining ImageViews if there are fewer images than ImageViews
            for (i in sharedImagePaths.size until mainFrames.size) {
                mainFrames[i].visibility = View.GONE
            }

        } else {
            Toast.makeText(this, "No images received to display", Toast.LENGTH_SHORT).show()
        }

        // Set onClick listener for Camera Button
        cameraButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        // Set onClick listener for Home Button
        homeButton.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }
    }
}
