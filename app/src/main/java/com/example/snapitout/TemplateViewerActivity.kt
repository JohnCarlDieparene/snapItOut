    package com.example.snapitout

    import android.app.Activity
    import android.content.Intent
    import android.net.Uri
    import android.os.Bundle
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Button
    import android.widget.GridLayout
    import android.widget.ImageButton
    import android.widget.ImageView
    import android.widget.TextView
    import androidx.appcompat.app.AppCompatActivity

    class TemplateViewerActivity : AppCompatActivity() {

        private lateinit var grid: GridLayout
        private lateinit var nameView: TextView
        private lateinit var btnEdit: Button
        private lateinit var btnBack: ImageButton

        private var templateId: Long = 0L
        private var templateName: String = ""
        private var templateSlots = arrayListOf<String>()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            supportActionBar?.hide()
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            setContentView(R.layout.activity_template_viewer)

            nameView = findViewById(R.id.templateNameView)
            grid = findViewById(R.id.gridViewer)
            btnEdit = findViewById(R.id.btnEditTemplate)
            btnBack = findViewById(R.id.btnBack)

            templateId = intent.getLongExtra("TEMPLATE_ID", System.currentTimeMillis())
            templateName = intent.getStringExtra("TEMPLATE_NAME") ?: "Untitled Template"
            templateSlots = intent.getStringArrayListExtra("TEMPLATE_SLOTS") ?: arrayListOf()

            nameView.text = templateName
            loadTemplateGrid(templateSlots)

            btnBack.setOnClickListener { finish() }

            btnEdit.setOnClickListener {
                val editIntent = Intent(this, EditTemplateActivity::class.java).apply {
                    putExtra("TEMPLATE_ID", templateId)
                    putExtra("TEMPLATE_NAME", templateName)
                    putStringArrayListExtra("TEMPLATE_SLOTS", templateSlots)
                    putExtra("IS_EDIT_MODE", true)
                }
                startActivityForResult(editIntent, 101)
            }
        }

        // ✅ Reload grid when coming back from EditTemplateActivity
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == 101 && resultCode == Activity.RESULT_OK && data != null) {
                val updatedName = data.getStringExtra("TEMPLATE_NAME") ?: templateName
                val updatedSlots = data.getStringArrayListExtra("TEMPLATE_SLOTS") ?: templateSlots

                templateName = updatedName
                templateSlots = updatedSlots

                nameView.text = updatedName
                loadTemplateGrid(updatedSlots)

                // ✅ Send result back to TemplatesActivity
                val returnIntent = Intent().apply {
                    putExtra("TEMPLATE_ID", templateId)
                    putExtra("TEMPLATE_NAME", updatedName)
                    putStringArrayListExtra("TEMPLATE_SLOTS", updatedSlots)
                }
                setResult(Activity.RESULT_OK, returnIntent)
            }
        }

        // ✅ Helper: load images into the grid
        private fun loadTemplateGrid(slots: List<String>) {
            grid.removeAllViews()
            grid.columnCount = 2

            val screenWidth = resources.displayMetrics.widthPixels
            val itemMargin = (6 * resources.displayMetrics.density).toInt()
            val totalMargins = itemMargin * (grid.columnCount + 1)
            val itemWidth = (screenWidth - totalMargins) / grid.columnCount

            for (uriStr in slots) {
                val imageView = ImageView(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = itemWidth
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                        setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
                    }
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setBackgroundColor(0xFFEFEFEF.toInt())
                    try {
                        setImageURI(Uri.parse(uriStr))
                    } catch (e: Exception) {
                        setBackgroundColor(0xFFCCCCCC.toInt())
                    }
                }
                grid.addView(imageView)
            }
        }
    }
