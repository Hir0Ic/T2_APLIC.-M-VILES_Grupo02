package com.example.juego

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var etCredential: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilCredential: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvRegisterRedirect: TextView

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        dbHelper = DatabaseHelper(this)

        // Si ya hay una sesión activa, redireccionar al juego
        if (sessionManager.isLoggedIn()) {
            launchMainActivity()
            return
        }

        setContentView(R.layout.activity_login)

        // Inicializar vistas
        etCredential = findViewById(R.id.etCredential)
        etPassword = findViewById(R.id.etPassword)
        tilCredential = findViewById(R.id.tilCredential)
        tilPassword = findViewById(R.id.tilPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegisterRedirect = findViewById(R.id.tvRegisterRedirect)

        // Acción al hacer clic en Ingresar
        btnLogin.setOnClickListener {
            if (validarCampos()) {
                val credential = etCredential.text.toString().trim()
                val password = etPassword.text.toString().trim()

                val userId = dbHelper.loginUsuario(credential, password)
                if (userId != -1) {
                    sessionManager.saveSession(userId)
                    Toast.makeText(this, "¡Bienvenido, Entrenador!", Toast.LENGTH_SHORT).show()
                    launchMainActivity()
                } else {
                    Toast.makeText(this, "Usuario/Correo o Contraseña incorrectos", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Acción para ir al registro
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
