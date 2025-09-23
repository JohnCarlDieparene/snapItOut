package com.example.snapitout

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

@Suppress("DEPRECATION")
class EditingActivity : AppCompatActivity() {

    private lateinit var saveButton: Button
    private lateinit var retakeButton: Button

    private lateinit var btnNormal: Button
    private lateinit var btnBW: Button
    private lateinit var btnVintage: Button
    private lateinit var btnOld: Button

    private lateinit var colorCircles: List<View>
    private lateinit var stickers: List<ImageView>
    private lateinit var mainFrames: List<ImageView>

    private lateinit var frameContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editing)

        saveButton = findViewById(R.id.saveButton)
        retakeButton = findViewById(R.id.retakeButton)

        btnNormal = findViewById(R.id.NormalBtn)
        btnBW = findViewById(R.id.BWBtn)
        btnVintage = findViewById(R.id.VintageBtn)
        btnOld = findViewById(R.id.OldPhotoBtn)

        frameContainer = findViewById(R.id.frameContainer)

        colorCircles = listOf(
            findViewById(R.id.colorCircle1),
            findViewById(R.id.colorCircle2),
            findViewById(R.id.colorCircle3),
            findViewById(R.id.colorCircle4),
            findViewById(R.id.colorCircle5),
            findViewById(R.id.colorCircle6)
        )

        stickers = listOf(
            findViewById(R.id.sticker1),
            findViewById(R.id.sticker2),
            findViewById(R.id.sticker3),
            findViewById(R.id.sticker4),
            findViewById(R.id.sticker5)
        )

        mainFrames = listOf(
            findViewById(R.id.mainFrame1),
            findViewById(R.id.mainFrame2),
            findViewById(R.id.mainFrame3),
            findViewById(R.id.mainFrame4)
        )

        val selectedUris = intent.getParcelableArrayListExtra<Uri>("selected_images")
        val photoPaths = intent.getStringArrayListExtra("photoPaths")

        if (!selectedUris.isNullOrEmpty()) {
            for (i in 0 until minOf(4, selectedUris.size)) {
                mainFrames[i].setImageURI(selectedUris[i])
            }
        } else if (!photoPaths.isNullOrEmpty()) {
            for (i in 0 until minOf(4, photoPaths.size)) {
                val bitmap = BitmapFactory.decodeFile(photoPaths[i]) // ✅ No rotation applied
                mainFrames[i].setImageBitmap(bitmap)
            }
        } else {
            Toast.makeText(this, "No photos to display", Toast.LENGTH_SHORT).show()
        }

        fun applyFilterToAllFrames(filter: ColorMatrixColorFilter?) {
            mainFrames.forEach { imageView ->
                imageView.colorFilter = filter
            }
        }

        btnNormal.setOnClickListener { applyFilterToAllFrames(null) }

        btnBW.setOnClickListener {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            applyFilterToAllFrames(ColorMatrixColorFilter(matrix))
        }

        btnVintage.setOnClickListener {
            val matrix = ColorMatrix(
                floatArrayOf(
                    0.9f, 0.3f, 0.1f, 0f, 0f,
                    0.0f, 0.9f, 0.1f, 0f, 0f,
                    0.0f, 0.3f, 0.7f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            applyFilterToAllFrames(ColorMatrixColorFilter(matrix))
        }

        btnOld.setOnClickListener {
            val matrix = ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            applyFilterToAllFrames(ColorMatrixColorFilter(matrix))
        }

        val backgroundImages = listOf(
            R.drawable.frame1,
            R.drawable.frame2,
            R.drawable.frame3,
            R.drawable.frame4,
            R.drawable.frame5,
            R.drawable.frame6
        )

        colorCircles.forEachIndexed { index, view ->
            view.setOnClickListener {
                frameContainer.setBackgroundResource(backgroundImages[index])
                Toast.makeText(this, "Background ${index + 1} applied", Toast.LENGTH_SHORT).show()
            }
        }

        val stickerBackgrounds = listOf(
            R.drawable.fsticker1,
            R.drawable.fsticker2,
            R.drawable.fsticker3,
            R.drawable.fsticker4,
            R.drawable.fsticker5
        )

        stickers.forEachIndexed { index, sticker ->
            sticker.setOnClickListener {
                frameContainer.setBackgroundResource(stickerBackgrounds[index])
                Toast.makeText(this, "Sticker ${index + 1} applied as background", Toast.LENGTH_SHORT).show()
            }
        }

        saveButton.setOnClickListener {
            val bitmap = Bitmap.createBitmap(
                frameContainer.width,
                frameContainer.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            frameContainer.draw(canvas)

            val filename = "SnapIt_${System.currentTimeMillis()}.jpg"
            val savedImagePaths = mutableListOf<String>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SnapItOut")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                    savedImagePaths.add(uri.toString())
                }
            } else {
                val storageDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "SnapItOut"
                )
                if (!storageDir.exists()) storageDir.mkdirs()
                val file = File(storageDir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                savedImagePaths.add(file.absolutePath)
            }

            if (savedImagePaths.isNotEmpty()) {
                Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, SharingActivity::class.java)
                intent.putStringArrayListExtra("sharedImagePaths", ArrayList(savedImagePaths))

                val albumIntent = Intent(this, AlbumActivity::class.java)
                albumIntent.putStringArrayListExtra("albumImagePaths", ArrayList(savedImagePaths))
                startActivity(albumIntent)

                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }

        retakeButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
            finish()
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
