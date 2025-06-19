package com.example.snapitout

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import android.content.Intent

class AlbumActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)

        val imageContainer: GridLayout = findViewById(R.id.albumImageContainer)
        imageContainer.columnCount = 3 // Set the number of columns to 3

        val imageUris = getAllImagesFromSnapItOutFolder()

        for (uri in imageUris) {
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

            // Set a click listener to open the image in full screen
            imageView.setOnClickListener {
                val intent = Intent(this, FullScreenImageActivity::class.java)
                intent.putExtra("imageUri", uri.toString()) // Pass the URI of the clicked image
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
}
