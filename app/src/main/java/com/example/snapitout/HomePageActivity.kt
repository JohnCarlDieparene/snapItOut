package com.example.snapitout

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Contacts.Photo
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class HomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // 📸 Photobooth Section
        val photoboothText: TextView = findViewById(R.id.textView7)
        val photoboothImage: ImageView = findViewById(R.id.camlogo)
        val photoboothContainer: MaterialToolbar = findViewById(R.id.materialToolbar3)

        // 🎨 Frames Section
        val framesText: TextView = findViewById(R.id.textView11)
        val framesImage: ImageView = findViewById(R.id.imageView6)

        // 🌟 Stickers Section
        val stickersText: TextView = findViewById(R.id.textView12)
        val stickersImage: ImageView = findViewById(R.id.imageView7)

        // ℹ️ Photo Collab Section
        val photoCollabText: TextView = findViewById(R.id.textView)
        val photoCollabImage: ImageView = findViewById(R.id.imageView3)

        // 🖼 Album Section
        val albumImage: ImageView = findViewById(R.id.imageView8)

        // 👤 User Profile Icon
        val profileIcon: ImageView = findViewById(R.id.profileIcon)

        // 👉 Navigate to CameraActivity
        photoboothText.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        photoboothImage.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        photoboothContainer.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        // 👉 Navigate to FramesActivity
        framesText.setOnClickListener {
            startActivity(Intent(this, FramesActivity::class.java))
        }
        framesImage.setOnClickListener {
            startActivity(Intent(this, FramesActivity::class.java))
        }

        // 👉 Navigate to StickersActivity
        stickersText.setOnClickListener {
            startActivity(Intent(this, StickersActivity::class.java))
        }
        stickersImage.setOnClickListener {
            startActivity(Intent(this, StickersActivity::class.java))
        }

        // ❌ PhotoCollab still no action
        photoCollabText.setOnClickListener {
            startActivity(Intent(this, PhotoCollabActivity::class.java))
        }
        photoCollabImage.setOnClickListener {
            startActivity(Intent(this, PhotoCollabActivity::class.java))
        }

        // ✅ Navigate to AlbumActivity
        albumImage.setOnClickListener {
            startActivity(Intent(this, AlbumActivity::class.java))
        }

        // ✅ Navigate to UserActivity when profile icon is clicked
        profileIcon.setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
        }
    }
}
