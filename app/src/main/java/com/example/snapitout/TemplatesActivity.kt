package com.example.snapitout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TemplatesActivity : AppCompatActivity() {

    private lateinit var gridContainer: GridLayout
    private lateinit var rvTemplates: View
    private lateinit var tvNoTemplates: TextView
    private lateinit var floatingBtnDoc: ImageButton

    private val templates = mutableListOf<Template>()
    private val gson = Gson()
    private val prefsName = "templates_prefs"
    private val keyTemplates = "key_templates"

    private val editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val data = result.data!!
            val templateCreated = data.getBooleanExtra("templateCreated", false)
            if (templateCreated) {
                val id = data.getLongExtra("TEMPLATE_ID", System.currentTimeMillis())
                val name = data.getStringExtra("TEMPLATE_NAME") ?: "Untitled"
                val slots = data.getStringArrayListExtra("TEMPLATE_SLOTS") ?: arrayListOf()
                val newTemplate = Template(id = id, name = name, slotUris = slots)
                addTemplateAtTop(newTemplate)
                Toast.makeText(this, "Template saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_templates)

        gridContainer = findViewById(R.id.albumImageContainer)
        rvTemplates = findViewById(R.id.rvTemplates)
        tvNoTemplates = findViewById(R.id.tvNoTemplates)
        floatingBtnDoc = findViewById(R.id.btnDoc)

        loadTemplatesFromPrefs()
        refreshGridFromTemplates()
        updateNoTemplatesUi()

        // Keep original floating button as a fallback opener, but hide it to avoid duplicate UI
        floatingBtnDoc.visibility = View.GONE

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageButton?>(R.id.navHome)?.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }
    }

    private fun addTemplateAtTop(template: Template) {
        templates.add(0, template)
        saveTemplatesToPrefs()
        refreshGridFromTemplates()
        updateNoTemplatesUi()
    }

    private fun refreshGridFromTemplates() {
        gridContainer.removeAllViews()

        // Add thumbnails for each template
        for ((index, template) in templates.withIndex()) {
            val uriString = template.slotUris.firstOrNull()
            val imageView = ImageView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = resources.displayMetrics.widthPixels / 3 - 16
                    height = width
                    setMargins(8, 8, 8, 8)
                    setGravity(Gravity.CENTER)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = template.name
                if (uriString != null) {
                    try { setImageURI(Uri.parse(uriString)) } catch (_: Exception) {}
                } else {
                    // placeholder background if no image
                    setBackgroundColor(0xFFCCCCCC.toInt())
                }
                setOnClickListener {
                    val intent = Intent(this@TemplatesActivity, EditTemplateActivity::class.java).apply {
                        putExtra("TEMPLATE_ID", template.id)
                        putExtra("TEMPLATE_NAME", template.name)
                        putStringArrayListExtra("TEMPLATE_SLOTS", ArrayList(template.slotUris))
                    }
                    editLauncher.launch(intent)
                }
            }
            gridContainer.addView(imageView)
        }

        // Add the "Add Template" tile at the end of the grid
        val addTile = ImageButton(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = resources.displayMetrics.widthPixels / 3 - 16
                height = width
                setMargins(8, 8, 8, 8)
                setGravity(Gravity.CENTER)
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageResource(R.drawable.addbtn) // ensure this drawable exists
            background = null
            contentDescription = "Create Template"
            setOnClickListener {
                val intent = Intent(this@TemplatesActivity, EditTemplateActivity::class.java)
                editLauncher.launch(intent)
            }
        }

        gridContainer.addView(addTile)
    }

    private fun updateNoTemplatesUi() {
        if (templates.isEmpty()) {
            tvNoTemplates.visibility = View.VISIBLE
            gridContainer.visibility = View.VISIBLE // show grid with only add tile
            rvTemplates.visibility = View.GONE
        } else {
            tvNoTemplates.visibility = View.GONE
            gridContainer.visibility = View.VISIBLE
            rvTemplates.visibility = View.GONE
        }
    }

    private fun saveTemplatesToPrefs() {
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = gson.toJson(templates)
        prefs.edit().putString(keyTemplates, json).apply()
    }

    private fun loadTemplatesFromPrefs() {
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = prefs.getString(keyTemplates, null) ?: return
        val type = object : TypeToken<List<Template>>() {}.type
        val list: List<Template> = try { gson.fromJson(json, type) } catch (_: Exception) { emptyList() }
        templates.clear()
        templates.addAll(list)
    }
}