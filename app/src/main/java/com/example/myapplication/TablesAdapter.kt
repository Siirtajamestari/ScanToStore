package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TablesAdapter(
    private val tables: List<String>,
    private val onTableClick: (String) -> Unit
) : RecyclerView.Adapter<TablesAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(tableName: String) {
            itemView.apply {
                findViewById<TextView>(R.id.nameView).text = tableName
                setOnClickListener { onTableClick(tableName) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_map, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tables[position])
    }

    override fun getItemCount() = tables.size
}
