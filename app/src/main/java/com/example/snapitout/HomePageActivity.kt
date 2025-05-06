package com.example.snapitout

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page) // Replace with your actual XML file name

        // Photobooth Section
        val photoboothText: TextView = findViewById(R.id.textView7)
        val photoboothImage: ImageView = findViewById(R.id.camlogo)

        // Frames Section (No action, just design)
        val framesText: TextView = findViewById(R.id.textView11)
        val framesImage: ImageView = findViewById(R.id.imageView6)

        // Stickers Section (No action, just design)
        val stickersText: TextView = findViewById(R.id.textView12)
        val stickersImage: ImageView = findViewById(R.id.imageView7)

        // About Us Section (No action, just design)
        val aboutUsText: TextView = findViewById(R.id.textView)
        val aboutUsImage: ImageView = findViewById(R.id.imageView3)

        // Album Section (No action, just design)
        val albumImage: ImageView = findViewById(R.id.imageView8)

        // Click Listeners for Photobooth Section
        photoboothText.setOnClickListener {
            // Navigate to CameraActivity when Photobooth is clicked
            startActivity(Intent(this, CameraActivity::class.java))
        }
        photoboothImage.setOnClickListener {
            // Navigate to CameraActivity when Photobooth image is clicked
            startActivity(Intent(this, CameraActivity::class.java))
        }

        // Frames Section (Just design, no action)
        framesText.setOnClickListener {
            // No action, just a design
        }
        framesImage.setOnClickListener {
            // No action, just a design
        }

        // Stickers Section (Just design, no action)
        stickersText.setOnClickListener {
            // No action, just a design
        }
        stickersImage.setOnClickListener {
            // No action, just a design
        }

        // About Us Section (Just design, no action)
        aboutUsText.setOnClickListener {
            // No action, just a design
        }
        aboutUsImage.setOnClickListener {
            // No action, just a design
        }

        // Album Section (Just design, no action)
        albumImage.setOnClickListener {
            // No action, just a design
        }
    }
}
