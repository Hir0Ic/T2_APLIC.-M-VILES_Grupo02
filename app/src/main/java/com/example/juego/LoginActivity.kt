package com.example.juego

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.juego.data.PokemonRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etCredential: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilCredential: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvRegisterRedirect: TextView

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            launchMainActivity()
            return
        }

        setContentView(R.layout.activity_login)

        etCredential = findViewById(R.id.etCredential)
        etPassword = findViewById(R.id.etPassword)
        tilCredential = findViewById(R.id.tilCredential)
        tilPassword = findViewById(R.id.tilPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegisterRedirect = findViewById(R.id.tvRegisterRedirect)

        btnLogin.setOnClickListener {
            if (validarCampos()) {
                val credential = etCredential.text.toString().trim()
                val password = etPassword.text.toString().trim()

                lifecycleScope.launch {
                    val user = PokemonRepository.getInstance(this@LoginActivity)
                        .loginUser(credential, password)
                    if (user != null) {
                        sessionManager.saveSession(user.id)
                        Toast.makeText(this@LoginActivity, "¡Bienvenido, Entrenador!", Toast.LENGTH_SHORT).show()
                        launchMainActivity()
                    } else {
                        Toast.makeText(this@LoginActivity, "Usuario/Correo o Contraseña incorrectos", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        tvRegisterRedirect.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validarCampos(): Boolean {
        var valido = true
        val credential = etCredential.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (credential.isEmpty()) {
            tilCredential.error = "Ingresa tu usuario o correo electrónico"
            valido = false
        } else {
            tilCredential.error = null
        }

        if (password.isEmpty()) {
            tilPassword.error = "Ingresa tu contraseña"
            valido = false
        } else {
            tilPassword.error = null
        }

        return valido
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
