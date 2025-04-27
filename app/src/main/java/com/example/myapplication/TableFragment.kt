package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class DbFragment : Fragment() {
    private val args: DbFragmentArgs by navArgs()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DataAdapter
    private lateinit var scanButton: FloatingActionButton
    private val tableData = mutableListOf<Map<String, Any>>()
    private val filteredData = mutableListOf<Map<String, Any>>()

    private var currentQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_db_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.table_data_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val tableName = args.tableName

        if (tableName.isNotEmpty()) {
            fetchTableData(tableName)
        }

        val searchView: SearchView = view.findViewById(R.id.searchView)
        searchView.setQuery(currentQuery, false)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                filterData(currentQuery)

                val clearButton: ImageView = searchView.findViewById(androidx.appcompat.R.id.search_close_btn)

                if (currentQuery.isEmpty()) {
                    clearButton.visibility = View.GONE
                } else {
                    clearButton.visibility = View.VISIBLE
                }

                if (currentQuery.isEmpty()) {
                    filteredData.clear()
                    filteredData.addAll(tableData)
                    adapter.notifyDataSetChanged()
                } else {
                    filterData(currentQuery)
                }

                return true
            }
        })


        scanButton = view.findViewById(R.id.StartScanButton)
        scanButton.setOnClickListener {
            val action = DbFragmentDirections.actionDbFragmentToScanFragment(tableName)
            findNavController().navigate(action)
        }
    }

    private fun fetchTableData(tableName: String) {
        lifecycleScope.launch {
            try {
                val data = RetrofitClient.instance.getTableData(tableName)

                tableData.clear()
                tableData.addAll(data)

                filteredData.clear()
                filteredData.addAll(tableData)

                adapter = DataAdapter(tableName, filteredData) { tableName, id ->
                    deleteRow(tableName, id)
                }
                recyclerView.adapter = adapter

                filterData(currentQuery)
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка загрузки данных таблицы", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filterData(query: String) {
        if (::adapter.isInitialized) {
            filteredData.clear()

            for (row in tableData) {
                val rowText = row.entries.joinToString(", ") { "${it.key}: ${it.value}" }
                if (rowText.contains(query, ignoreCase = true)) {
                    filteredData.add(row)
                }
            }

            adapter.notifyDataSetChanged()
        } else {
            Log.w("DbFragment", "Adapter is not initialized yet.")
        }
    }

    private fun deleteRow(tableName: String, id: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.deleteItem(tableName, id)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show()
                    fetchTableData(tableName)
                } else {
                    Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error deleting item", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}

