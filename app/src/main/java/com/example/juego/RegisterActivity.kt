package com.example.juego

import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.juego.data.PokemonRepository
import com.example.juego.data.RegisterResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etRegUsername: TextInputEditText
    private lateinit var etRegEmail: TextInputEditText
    private lateinit var etRegPassword: TextInputEditText
    private lateinit var tilRegUsername: TextInputLayout
    private lateinit var tilRegEmail: TextInputLayout
    private lateinit var tilRegPassword: TextInputLayout
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLoginRedirect: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etRegUsername = findViewById(R.id.etRegUsername)
        etRegEmail = findViewById(R.id.etRegEmail)
        etRegPassword = findViewById(R.id.etRegPassword)
        tilRegUsername = findViewById(R.id.tilRegUsername)
        tilRegEmail = findViewById(R.id.tilRegEmail)
        tilRegPassword = findViewById(R.id.tilRegPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginRedirect = findViewById(R.id.tvLoginRedirect)

        btnRegister.setOnClickListener {
            if (validarCampos()) {
                val username = etRegUsername.text.toString().trim()
                val email = etRegEmail.text.toString().trim()
                val password = etRegPassword.text.toString().trim()

                lifecycleScope.launch {
                    val result = PokemonRepository.getInstance(this@RegisterActivity)
                        .registerUser(username, email, password)
                    when (result) {
                        is RegisterResult.Success -> {
                            Toast.makeText(this@RegisterActivity, "¡Registro Exitoso! Inicia sesión para continuar", Toast.LENGTH_LONG).show()
                            finish()
                        }
                        is RegisterResult.Duplicate -> {
                            Toast.makeText(this@RegisterActivity, "El nombre de usuario o correo ya se encuentra registrado", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        tvLoginRedirect.setOnClickListener {
            finish()
        }
    }

    private fun validarCampos(): Boolean {
        var valido = true
        val username = etRegUsername.text.toString().trim()
        val email = etRegEmail.text.toString().trim()
        val password = etRegPassword.text.toString().trim()

        if (username.isEmpty()) {
            tilRegUsername.error = "Ingresa un nombre de usuario"
            valido = false
        } else if (username.length < 3) {
            tilRegUsername.error = "Debe tener al menos 3 caracteres"
            valido = false
        } else {
            tilRegUsername.error = null
        }

        if (email.isEmpty()) {
            tilRegEmail.error = "Ingresa un correo electrónico"
            valido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilRegEmail.error = "Formato de correo inválido"
            valido = false
        } else {
            tilRegEmail.error = null
        }

        if (password.isEmpty()) {
            tilRegPassword.error = "Ingresa una contraseña"
            valido = false
        } else if (password.length < 6) {
            tilRegPassword.error = "La contraseña debe tener al menos 6 caracteres"
            valido = false
        } else {
            tilRegPassword.error = null
        }

        return valido
    }
}
