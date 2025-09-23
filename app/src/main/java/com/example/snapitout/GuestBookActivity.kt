package com.example.snapitout

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class GuestBookActivity : AppCompatActivity() {

    private lateinit var homeIcon: ImageView
    private lateinit var albumIcon: ImageView
    private lateinit var arrowIcon: ImageView
    private lateinit var backArrow: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guestbook)

        // Bottom nav icons
        homeIcon = findViewById(R.id.imageView20)
        albumIcon = findViewById(R.id.imageView21)
        backArrow = findViewById(R.id.imageView29)

        // Arrow on baby pig frame
        arrowIcon = findViewById(R.id.imageView27)

        homeIcon.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }

        albumIcon.setOnClickListener {
            val intent = Intent(this, AlbumActivity::class.java)
            startActivity(intent)
        }

        arrowIcon.setOnClickListener {
            // You can later link this to a detailed view or comment activity
            // Example:
            // val intent = Intent(this, GuestBookEntryDetailActivity::class.java)
            // startActivity(intent)
        }

        backArrow.setOnClickListener {
            finish() // Go back to the previous screen
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
