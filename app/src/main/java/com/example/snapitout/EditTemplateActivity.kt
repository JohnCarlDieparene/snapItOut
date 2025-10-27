package com.example.snapitout

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditTemplateActivity : AppCompatActivity() {

    private lateinit var btnAddText: ImageButton
    private lateinit var btnAddSticker: ImageButton
    private lateinit var btnAddImage: ImageButton
    private lateinit var btnColorPicker: ImageButton
    private lateinit var frameContainer: ConstraintLayout
    private lateinit var saveButton: Button
    private lateinit var navHome: ImageButton
    private lateinit var navAlbum: ImageButton

    private var isEditMode = false
    private var existingSlots: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_template)

        btnAddText = findViewById(R.id.btnAddText)
        btnAddSticker = findViewById(R.id.btnAddSticker)
        btnAddImage = findViewById(R.id.btnAddImage)
        btnColorPicker = findViewById(R.id.btnColorPicker)
        frameContainer = findViewById(R.id.frameContainer)
        saveButton = findViewById(R.id.SaveButton)
        navHome = findViewById(R.id.navHome)
        navAlbum = findViewById(R.id.navAlbum)

        // ✅ Check if launched in edit mode
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        existingSlots = intent.getStringArrayListExtra("TEMPLATE_SLOTS")
        if (isEditMode && !existingSlots.isNullOrEmpty()) {
            loadExistingTemplate(existingSlots!!)
        }

        // 🟩 Add text feature
        btnAddText.setOnClickListener {
            val inputField = EditText(this).apply {
                hint = "Type your text"
                setPadding(16)
            }
            AlertDialog.Builder(this)
                .setTitle("Enter Text")
                .setView(inputField)
                .setPositiveButton("Add") { _, _ ->
                    val userText = inputField.text.toString()
                    if (userText.isNotBlank()) {
                        val textView = TextView(this).apply {
                            text = userText
                            textSize = 16f
                            setTextColor(Color.BLACK)
                            setPadding(8)
                            x = 50f
                            y = 50f
                        }
                        makeViewDraggable(textView)
                        frameContainer.addView(textView)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // 🟩 Add sticker feature
        btnAddSticker.setOnClickListener {
            val categories = arrayOf("Faces", "Symbols", "Celebration", "Upload Image")
            val emojiMap = mapOf(
                "Faces" to arrayOf("😊", "😎", "😂", "🥰", "😇", "🤔"),
                "Symbols" to arrayOf("❤️", "✨", "🔥", "💡", "👍", "💬"),
                "Celebration" to arrayOf("🎉", "🎈", "🎂", "🎨", "📸", "🎶")
            )

            AlertDialog.Builder(this)
                .setTitle("Choose Sticker Category")
                .setItems(categories) { _, categoryIndex ->
                    val selectedCategory = categories[categoryIndex]
                    if (selectedCategory == "Upload Image") {
                        Toast.makeText(this, "Image upload not implemented yet", Toast.LENGTH_SHORT).show()
                    } else {
                        val emojis = emojiMap[selectedCategory] ?: return@setItems
                        AlertDialog.Builder(this)
                            .setTitle("Choose a $selectedCategory Sticker")
                            .setItems(emojis) { _, emojiIndex ->
                                val emoji = TextView(this).apply {
                                    text = emojis[emojiIndex]
                                    textSize = 32f
                                    setPadding(8)
                                    x = 100f
                                    y = 100f
                                }
                                makeViewDraggable(emoji)
                                frameContainer.addView(emoji)
                            }
                            .show()
                    }
                }
                .show()
        }

        // 🟩 Background color picker
        btnColorPicker.setOnClickListener {
            val colors = arrayOf("White", "Light Pink", "Sky Blue", "Mint", "Gray")
            val colorValues = arrayOf("#FFFFFF", "#FFEBEE", "#E3F2FD", "#E0F7FA", "#EEEEEE")
            AlertDialog.Builder(this)
                .setTitle("Choose Background Color")
                .setItems(colors) { _, which ->
                    frameContainer.setBackgroundColor(Color.parseColor(colorValues[which]))
                }
                .show()
        }

        // 🟩 Save button
        saveButton.setOnClickListener {
            val savedUriString = saveFrameAsImage()
            val resultIntent = Intent().apply {
                putExtra("templateCreated", true)
                if (isEditMode) {
                    // ✅ Update existing template
                    putExtra("IS_EDIT_MODE", true)
                    putExtra("TEMPLATE_ID", intent.getLongExtra("TEMPLATE_ID", System.currentTimeMillis()))
                    putExtra("TEMPLATE_NAME", intent.getStringExtra("TEMPLATE_NAME"))
                    val updatedSlots = arrayListOf<String>()
                    savedUriString?.let { updatedSlots.add(it) }
                    putStringArrayListExtra("TEMPLATE_SLOTS", updatedSlots)
                } else {
                    // ✅ New template
                    putExtra("TEMPLATE_ID", System.currentTimeMillis())
                    putExtra("TEMPLATE_NAME", savedUriString?.substringAfterLast('/') ?: "My Template")
                    val slots = arrayListOf<String>()
                    savedUriString?.let { slots.add(it) }
                    putStringArrayListExtra("TEMPLATE_SLOTS", slots)
                }
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        navHome.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        navAlbum.setOnClickListener {
            startActivity(Intent(this, AlbumActivity::class.java))
            finish()
        }
    }

    // ✅ Loads existing template images when editing
    private fun loadExistingTemplate(slots: ArrayList<String>) {
        for (uriStr in slots) {
            try {
                val imageView = ImageView(this).apply {
                    setImageURI(Uri.parse(uriStr))
                    layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT
                    )
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
                frameContainer.addView(imageView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun makeViewDraggable(view: View) {
        var dX = 0f
        var dY = 0f
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    v.animate().x(event.rawX + dX).y(event.rawY + dY).setDuration(0).start()
                }
            }
            true
        }
    }

    private fun saveFrameAsImage(): String? {
        val view = frameContainer
        if (view.width == 0 || view.height == 0) {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST)
            view.measure(widthSpec, heightSpec)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        }

        val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        view.draw(canvas)

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "template_$timestamp.jpg"
        val relativePath = "Pictures/SnapItOut/Templates"

        val resolver = contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = MediaStore.Images.Media.getContentUri(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.VOLUME_EXTERNAL_PRIMARY
            else "external"
        )

        val uri = try {
            resolver.insert(collection, values)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (uri == null) return null

        return try {
            resolver.openOutputStream(uri)?.use { out ->
                val compressed = bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                if (!compressed) throw IOException("Bitmap compress returned false")
            } ?: throw IOException("Failed to open output stream for uri: $uri")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            uri.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            try { resolver.delete(uri, null, null) } catch (_: Exception) {}
            null
        }
    }
}
