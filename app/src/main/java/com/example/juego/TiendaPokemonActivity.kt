package com.example.juego

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

data class Pokemon(
    val nombre: String,
    val imagen: String,
    val costo: Int
)

class TiendaPokemonActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var contenedor: LinearLayout
    private lateinit var txtPuntaje: TextView

    private var jugadorId = 1
    private var puntajeActual = 0

    private val pokemones = listOf(
        Pokemon("Blastoise", "blastoise", 30),
        Pokemon("Charmander", "charmander", 12),
        Pokemon("Diglett", "diglett", 8),
        Pokemon("Jigglypuff", "jigglypuff", 14),
        Pokemon("Magikarp", "magikarp", 5),
        Pokemon("Nidoran", "nidoran", 10),
        Pokemon("Squirtle", "squirtle", 15),
        Pokemon("Vaporeon", "vaporeon", 25),
        Pokemon("Vulpix", "vulpix", 20)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tienda_pokemon)

        dbHelper = DBHelper(this)
        contenedor = findViewById(R.id.contenedorPokemon)
        txtPuntaje = findViewById(R.id.txtPuntajeTienda)

        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        jugadorId = prefs.getInt("jugador_id", 1)
        puntajeActual = prefs.getInt("puntaje", 0)

        actualizarPuntaje()
        mostrarPokemones()
    }

    private fun mostrarPokemones() {
        for (pokemon in pokemones) {
            val card = LinearLayout(this)
            card.orientation = LinearLayout.VERTICAL
            card.gravity = Gravity.CENTER
            card.setPadding(20, 20, 20, 20)

            val imagen = ImageView(this)
            val idImagen = resources.getIdentifier(pokemon.imagen, "drawable", packageName)
            imagen.setImageResource(idImagen)
            imagen.layoutParams = LinearLayout.LayoutParams(220, 220)

            val nombre = TextView(this)
            nombre.text = pokemon.nombre
            nombre.textSize = 20f
            nombre.gravity = Gravity.CENTER

            val costo = TextView(this)
            costo.text = "Costo: ${pokemon.costo} puntos"
            costo.textSize = 16f
            costo.gravity = Gravity.CENTER

            val boton = Button(this)

            if (dbHelper.estaComprado(jugadorId, pokemon.nombre)) {
                boton.text = "Comprado"
                boton.isEnabled = false
            } else {
                boton.text = "Comprar"
                boton.isEnabled = true
            }

            boton.setOnClickListener {
                comprarPokemon(pokemon, boton)
            }

            card.addView(imagen)
            card.addView(nombre)
            card.addView(costo)
            card.addView(boton)

            contenedor.addView(card)
        }
    }

    private fun comprarPokemon(pokemon: Pokemon, boton: Button) {
        if (dbHelper.estaComprado(jugadorId, pokemon.nombre)) {
            Toast.makeText(this, "Ya compraste este Pokémon", Toast.LENGTH_SHORT).show()
            boton.text = "Comprado"
            boton.isEnabled = false
            return
        }

        if (puntajeActual < pokemon.costo) {
            Toast.makeText(this, "No tienes puntaje suficiente", Toast.LENGTH_SHORT).show()
            return
        }

        val compraExitosa = dbHelper.comprarPokemon(
            jugadorId,
            pokemon.nombre,
            pokemon.imagen,
            pokemon.costo
        )

        if (compraExitosa) {
            puntajeActual -= pokemon.costo

            getSharedPreferences("sesion", MODE_PRIVATE)
                .edit()
                .putInt("puntaje", puntajeActual)
                .apply()

            actualizarPuntaje()

            boton.text = "Comprado"
            boton.isEnabled = false

            Toast.makeText(this, "Compraste a ${pokemon.nombre}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarPuntaje() {
        txtPuntaje.text = "Puntaje: $puntajeActual"
    }
}