package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DataAdapter(
    private val tablename: String,
    private val data: List<Map<String, Any>>,
    private val onDeleteClick: (String, Int) -> Unit
) : RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.nameView)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_map, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = data[position]

        val rowText = row.entries.joinToString(", ") { "${it.key}: ${it.value}" }
        holder.textView.text = rowText

        val itemId = (row["id"] as? Double)?.toInt() ?: 0

        holder.deleteButton.setOnClickListener {
            onDeleteClick(tablename, itemId)
        }
    }

    override fun getItemCount(): Int = data.size
}


