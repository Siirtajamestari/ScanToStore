package com.example.myapplication
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class AdminFragment : Fragment() {
    private lateinit var loginEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var createUserButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_admin, container, false)

        loginEditText = view.findViewById(R.id.loginEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        createUserButton = view.findViewById(R.id.createUserButton)

        createUserButton.setOnClickListener {
            val login = loginEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (login.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(login, password)
            }
        }

        return view
    }

    private fun registerUser(login: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.registerUser(UserCredentials(login, password))
                if (response.isSuccessful) {
                    Toast.makeText(context, "User created!", Toast.LENGTH_SHORT).show()
                    loginEditText.text.clear()
                    passwordEditText.text.clear()
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Toast.makeText(context, "Failed: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
