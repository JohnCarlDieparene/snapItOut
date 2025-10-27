package com.example.snapitout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

    // ✅ For creating new templates
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

    // ✅ For viewing and updating templates
    private val viewLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val data = result.data!!
            val updatedId = data.getLongExtra("TEMPLATE_ID", -1L)
            val updatedName = data.getStringExtra("TEMPLATE_NAME")
            val updatedSlots = data.getStringArrayListExtra("TEMPLATE_SLOTS")

            if (updatedId != -1L && updatedName != null && updatedSlots != null) {
                val index = templates.indexOfFirst { it.id == updatedId }
                if (index != -1) {
                    templates[index] = Template(updatedId, updatedName, updatedSlots)
                    saveTemplatesToPrefs()
                    refreshGridFromTemplates()
                    Toast.makeText(this, "Template updated", Toast.LENGTH_SHORT).show()
                }
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

        val screenWidth = resources.displayMetrics.widthPixels
        val gridPadding = (8 * resources.displayMetrics.density).toInt() * 2
        val itemMargin = (8 * resources.displayMetrics.density).toInt()
        val totalMargins = itemMargin * 2 * 3
        val availableWidth = screenWidth - gridPadding - totalMargins
        val itemSize = availableWidth / 3

        for (template in templates) {
            val uriString = template.slotUris.firstOrNull()
            val imageView = ImageView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = itemSize
                    height = itemSize
                    setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = template.name
                if (uriString != null) {
                    try {
                        setImageURI(Uri.parse(uriString))
                    } catch (_: Exception) {
                        setBackgroundColor(0xFFCCCCCC.toInt())
                    }
                } else {
                    setBackgroundColor(0xFFCCCCCC.toInt())
                }
                setOnClickListener {
                    val intent = Intent(this@TemplatesActivity, TemplateViewerActivity::class.java).apply {
                        putExtra("TEMPLATE_ID", template.id)
                        putExtra("TEMPLATE_NAME", template.name)
                        putStringArrayListExtra("TEMPLATE_SLOTS", ArrayList(template.slotUris))
                    }
                    viewLauncher.launch(intent)
                }
            }
            gridContainer.addView(imageView)
        }

        val addTile = ImageButton(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = itemSize
                height = itemSize
                setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageResource(R.drawable.addbtn)
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
            gridContainer.visibility = View.VISIBLE
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
