package com.example.snapitout

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.snapitout.utils.CollageUtils
import com.example.snapitout.utils.RaphaelApiClient
import com.google.android.material.button.MaterialButton
import java.io.File
import androidx.appcompat.app.AlertDialog

class AlbumActivity : AppCompatActivity() {

    private lateinit var albumGrid: GridLayout
    private val selectedImages = mutableListOf<Uri>()
    private lateinit var albumFolder: File

    // Optional API key for Raphael AI
    private val raphaelApiKey: String? = null // Replace with your key if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)

        albumGrid = findViewById(R.id.albumImageContainer)
        albumGrid.columnCount = 3

        // Prepare album folder
        val picturesDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        albumFolder = File(picturesDir, "SnapItOut")
        if (!albumFolder.exists()) albumFolder.mkdirs()

        loadAlbumImages()

        // 🏠 Home button
        findViewById<ImageView>(R.id.imageView5).setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        // 📸 Auto Collage
        findViewById<MaterialButton>(R.id.autoCollageButton).setOnClickListener {
            createAutoCollage()
        }

        // ✂️ Scrapbook Collage
        findViewById<MaterialButton>(R.id.scrapbookCollageButton).setOnClickListener {
            createScrapbookCollage()
        }

        // 🧠 AI Scrapbook
        findViewById<MaterialButton>(R.id.aiScrapbookButton).setOnClickListener {
            generateAIScrapbook()
        }

        // ❤️ Shape Collage
        findViewById<MaterialButton>(R.id.shapeCollageButton)?.setOnClickListener {
            createShapeCollage()
        }
    }

    /** Load all images in the album folder */
    private fun loadAlbumImages() {
        albumGrid.removeAllViews()
        val imageUris = getAllImages().sortedByDescending { getFileModifiedTime(it) }

        imageUris.forEach { uri ->
            val imageView = ImageView(this)

            val sizeInDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                110f,
                resources.displayMetrics
            ).toInt()

            val marginInDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                6f,
                resources.displayMetrics
            ).toInt()

            val params = GridLayout.LayoutParams().apply {
                width = sizeInDp
                height = sizeInDp
                setMargins(marginInDp, marginInDp, marginInDp, marginInDp)
            }

            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.alpha = if (selectedImages.contains(uri)) 0.5f else 1.0f

            Glide.with(this).load(uri).into(imageView)

            // Tap → view full screen
            imageView.setOnClickListener {
                val allImageUris = getAllImages().map { it.toString() }
                val currentIndex = allImageUris.indexOf(uri.toString())

                val intent = Intent(this, FullScreenImageActivity::class.java)
                intent.putStringArrayListExtra("imageUris", ArrayList(allImageUris))
                intent.putExtra("currentIndex", currentIndex)
                startActivity(intent)
            }

            // Long press → select/deselect
            imageView.setOnLongClickListener {
                if (selectedImages.contains(uri)) selectedImages.remove(uri)
                else selectedImages.add(uri)
                loadAlbumImages()
                true
            }

            albumGrid.addView(imageView)
        }
    }

    private fun getAllImages(): List<Uri> {
        return albumFolder.listFiles()?.map { Uri.fromFile(it) } ?: emptyList()
    }

    private fun getFileModifiedTime(uri: Uri): Long {
        return File(uri.path ?: "").lastModified()
    }

    /** Auto Collage (simple grid) */
    private fun createAutoCollage() {
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Select at least 1 image", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmaps = selectedImages.mapNotNull { uri ->
            BitmapFactory.decodeFile(File(uri.path ?: "").absolutePath)
        }

        val collage = CollageUtils.createMegaCollage(bitmaps)
        if (collage != null) {
            CollageUtils.saveBitmapToAlbum(collage, albumFolder)
            loadAlbumImages()
            Toast.makeText(this, "Auto Collage created!", Toast.LENGTH_SHORT).show()
        }
    }

    /** Scrapbook Collage (overlapping) */
    private fun createScrapbookCollage() {
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Select at least 1 image", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmaps = selectedImages.mapNotNull { uri ->
            BitmapFactory.decodeFile(File(uri.path ?: "").absolutePath)
        }

        val collage = CollageUtils.createScrapbookCollage(bitmaps)
        if (collage != null) {
            CollageUtils.saveBitmapToAlbum(collage, albumFolder)
            loadAlbumImages()
            Toast.makeText(this, "Scrapbook Collage created!", Toast.LENGTH_SHORT).show()
        }
    }

    /** ❤️ Shape Collage (Random Shape Mask per photo) */
    /** ❤️ Shape Collage (all photos form one big shape) */
    /** ❤️ Shape Collage (user chooses shape before creating) */
    private fun createShapeCollage() {
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Select at least 1 image", Toast.LENGTH_SHORT).show()
            return
        }

        val shapes = arrayOf("❤️ Heart", "⭐ Star", "🔺 Triangle", "🔵 Circle", "⬜ Square")
        val shapeResIds = arrayOf(
            R.drawable.shape_heart,
            R.drawable.shape_star,
            R.drawable.shape_triangle,
            R.drawable.shape_circle,
            R.drawable.shape_square
        )



        // 🧩 Show selection dialog
        AlertDialog.Builder(this)
            .setTitle("Choose Shape")
            .setItems(shapes) { _, which ->
                val chosenShape = shapeResIds[which]
                generateShapeCollage(chosenShape)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /** 🧠 Creates the collage after user chooses a shape */
    private fun generateShapeCollage(shapeResId: Int) {
        val bitmaps = selectedImages.mapNotNull { uri ->
            BitmapFactory.decodeFile(File(uri.path ?: "").absolutePath)
        }

        try {
            val maskDrawable = ContextCompat.getDrawable(this, shapeResId)
            if (maskDrawable == null) {
                Toast.makeText(this, "Shape not found!", Toast.LENGTH_SHORT).show()
                return
            }

            // Convert shape to bitmap mask
            val maskBitmap = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)
            val maskCanvas = Canvas(maskBitmap)
            maskDrawable.setBounds(0, 0, maskCanvas.width, maskCanvas.height)
            maskDrawable.draw(maskCanvas)

            // Create shape collage using CollageUtils
            val collage = CollageUtils.createShapeCollage(bitmaps, maskBitmap)
            if (collage != null) {
                CollageUtils.saveBitmapToAlbum(collage, albumFolder)
                loadAlbumImages()
                Toast.makeText(this, "✅ Shape Collage created!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to create collage", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }



    /** AI Scrapbook using Raphael API */
    private fun generateAIScrapbook() {
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Select at least 1 image", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Generating AI Scrapbook...", Toast.LENGTH_SHORT).show()

        val base64Images = selectedImages.mapNotNull { uri ->
            val file = File(uri.path ?: "")
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            bitmap?.let { bmp ->
                val byteArrayOutputStream = java.io.ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                android.util.Base64.encodeToString(
                    byteArrayOutputStream.toByteArray(),
                    android.util.Base64.DEFAULT
                )
            }
        }

        if (base64Images.isEmpty()) {
            Toast.makeText(this, "Failed to read selected images", Toast.LENGTH_SHORT).show()
            return
        }

        val prompt = "Create a scrapbook-style collage using ${base64Images.size} images, with pastel textures and polaroid frames."

        RaphaelApiClient.generateImage(prompt, base64Images, raphaelApiKey) { bitmap ->
            runOnUiThread {
                if (bitmap != null) {
                    CollageUtils.saveBitmapToAlbum(bitmap, albumFolder)
                    loadAlbumImages()
                    Toast.makeText(this, "AI Scrapbook created!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to generate AI Scrapbook.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}