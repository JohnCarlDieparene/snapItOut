package com.example.snapitout
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar


class StickersActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageView
    private lateinit var profileIcon: ImageView
    private lateinit var homeIcon: ImageView
    private lateinit var albumIcon: ImageView
    private lateinit var stickersTxt: TextView
    private lateinit var toolbarTop: MaterialToolbar
    private lateinit var toolbarBottom: MaterialToolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stickers)

        // Initialize views
        backBtn = findViewById(R.id.backBtn)
        profileIcon = findViewById(R.id.profileIcon)
        homeIcon = findViewById(R.id.imageView5)
        albumIcon = findViewById(R.id.imageView8)
        stickersTxt = findViewById(R.id.StickersTxt)
        toolbarTop = findViewById(R.id.materialToolbar)
        toolbarBottom = findViewById(R.id.materialToolbar2)

        // Handle back button
        backBtn.setOnClickListener {
            finish() // Close the activity
        }

        // Example interactions (you can expand these)
        homeIcon.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        albumIcon.setOnClickListener {
            // Handle Album icon click
        }

        profileIcon.setOnClickListener{
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
