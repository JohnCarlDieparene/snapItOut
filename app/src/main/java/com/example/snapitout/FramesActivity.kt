package com.example.snapitout

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class FramesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frames)

        // 🏠 Home & Album Navigation
        val homeButton: ImageView = findViewById(R.id.imageView5)
        val albumButton: ImageView = findViewById(R.id.imageView8)

        homeButton.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        albumButton.setOnClickListener {
            startActivity(Intent(this, AlbumActivity::class.java))
        }

        // 🎨 Frame Containers
        val frame1: ImageView = findViewById(R.id.frameContainer)
        val frame2: ImageView = findViewById(R.id.frameContainer2)
        val frame3: ImageView = findViewById(R.id.frameContainer3)
        val frame4: ImageView = findViewById(R.id.frameContainer4)
        val frame5: ImageView = findViewById(R.id.frameContainer5)
        val frame6: ImageView = findViewById(R.id.frameContainer6)

        // 🧠 Frame click logic (optional)
        frame1.setOnClickListener {
            // TODO: Implement your frame/sticker selection behavior
        }

        // 👤 Navigate to UserActivity from profile icon
        val profileIcon: ImageView = findViewById(R.id.profileIcon)
        profileIcon.setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
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
