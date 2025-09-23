package com.example.snapitout

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.widget.GridLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import android.content.Intent
import android.view.View

class AlbumActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)

        val homeButton: ImageView = findViewById(R.id.imageView5)

        homeButton.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        val imageContainer: GridLayout = findViewById(R.id.albumImageContainer)
        imageContainer.columnCount = 3 // Set the number of columns to 3

        val imageUris = getAllImagesFromSnapItOutFolder()
        val uriStrings = imageUris.map { it.toString() } // Convert URIs to String list

        for ((index, uri) in imageUris.withIndex()) {
            val imageView = ImageView(this)

            val sizeInDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 110f, resources.displayMetrics
            ).toInt()

            val marginInDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics
            ).toInt()

            val params = GridLayout.LayoutParams().apply {
                width = sizeInDp
                height = sizeInDp
                setMargins(marginInDp, marginInDp, marginInDp, marginInDp)
            }

            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

            Glide.with(this)
                .load(uri)
                .into(imageView)

            // Click listener to open image in full screen
            imageView.setOnClickListener {
                val intent = Intent(this, FullScreenImageActivity::class.java)
                intent.putStringArrayListExtra("imageUris", ArrayList(imageUris.map { it.toString() }))
                intent.putExtra("currentIndex", index) // Use actual index
                startActivity(intent)
            }


            imageContainer.addView(imageView)
        }
    }

    private fun getAllImagesFromSnapItOutFolder(): List<Uri> {
        val imageUris = mutableListOf<Uri>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.RELATIVE_PATH
        )

        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%SnapItOut%")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(collection, id)
                imageUris.add(contentUri)
            }
        }

        return imageUris
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
