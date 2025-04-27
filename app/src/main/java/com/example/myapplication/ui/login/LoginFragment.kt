package com.example.myapplication.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.RetrofitClient
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var etLogin: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var sharedViewModel: SharedViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        etLogin = view.findViewById(R.id.etLogin)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)

        RetrofitClient.init(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnLogin.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val login = etLogin.text.toString()
        val password = etPassword.text.toString()

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.login(
                    LoginRequest(login, password)
                )
                sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
                sharedViewModel.setAdminStatus(login == "admin")
                RetrofitClient.saveToken(response.token)
                navigateToMain()
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Ошибка входа: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToMain() {
        activity?.runOnUiThread {
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        }
    }

    class SharedViewModel : ViewModel() {
        private val _isAdmin = MutableLiveData<Boolean>()
        val isAdmin: LiveData<Boolean> get() = _isAdmin

        fun setAdminStatus(isAdmin: Boolean) {
            _isAdmin.value = isAdmin
        }
    }
}