package com.example.snapitout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TemplateAdapter(
    private val context: Context,
    private val items: MutableList<Template>,
    private val itemClick: (Template) -> Unit
) : RecyclerView.Adapter<TemplateAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(context).inflate(R.layout.item_template_vertical_strip, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        holder.name.text = t.name
        // If you want to show thumbnail, load the first slotUri into holder.thumb using any image loader.
        // For now keep the ImageView empty or use a placeholder resource.
        holder.itemView.setOnClickListener { itemClick(t) }
    }

    override fun getItemCount(): Int = items.size

    fun addTemplateAtTop(template: Template) {
        items.add(0, template)
        notifyItemInserted(0)
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvTemplateName)
        val thumb: ImageView = view.findViewById(R.id.ivTemplateThumb)
    }
}