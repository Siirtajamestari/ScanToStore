package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.login.LoginFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TablesAdapter
    private lateinit var adminButton: FloatingActionButton
    private lateinit var aboutButton: Button
    private lateinit var sharedViewModel: LoginFragment.SharedViewModel
    private val tables = mutableListOf<String>()
    private var tohide: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedViewModel = ViewModelProvider(requireActivity())[LoginFragment.SharedViewModel::class.java]
        sharedViewModel.isAdmin.observe(viewLifecycleOwner, Observer { isAdmin ->
            if (!isAdmin) {
                tohide = true
                Log.d("1", "SUCKER")
            }
        })
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.table_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adminButton = view.findViewById(R.id.addNewItemButton)
        aboutButton = view.findViewById(R.id.button_about)

        sharedViewModel = ViewModelProvider(requireActivity())[LoginFragment.SharedViewModel::class.java]

        sharedViewModel.isAdmin.observe(viewLifecycleOwner, Observer { isAdmin ->
            if (!isAdmin) {
                adminButton.visibility = View.GONE
            } else {
                adminButton.visibility = View.VISIBLE
            }
        })

        loadTables()

        aboutButton.setOnClickListener {
            navigateToAbout()
        }

        adminButton.setOnClickListener {
            navigateToAdmin()
        }
    }


    private fun loadTables() {
        lifecycleScope.launch {
            try {
                val serverTables = RetrofitClient.instance.getTables()
                tables.clear()
                tables.addAll(serverTables)
                Log.d("MainFragment", "Server returned tables: $serverTables")

                adapter = TablesAdapter(tables) { tableName ->
                    Log.d("MainFragment", "Navigating to table: '$tableName'")
                    navigateToTable(tableName)
                }
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка загрузки таблиц", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToTable(tableName: String) {
        if (tableName.isBlank() || tableName == "\" \"" || tableName.contains("String")) {
            Log.e("MainFragment", "Invalid table name: '$tableName'")
            Toast.makeText(requireContext(), "Некорректное имя таблицы", Toast.LENGTH_SHORT).show()
            return
        }

        val action = MainFragmentDirections.actionMainFragmentToDbFragment(tableName)
        findNavController().navigate(action)
    }

    private fun navigateToAbout(){
        val action = MainFragmentDirections.actionMainFragmentToAboutFragment()
        findNavController().navigate(action)
    }
    private fun navigateToAdmin(){
        val action = MainFragmentDirections.actionMainFragmentToAdminFragment()
        findNavController().navigate(action)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}