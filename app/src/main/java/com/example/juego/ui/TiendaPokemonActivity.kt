package com.example.juego.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.juego.R
import com.example.juego.data.GameStateManager
import com.example.juego.data.PokemonStoreViewModel
import com.example.juego.data.PurchaseResult
import kotlinx.coroutines.launch

class TiendaPokemonActivity : AppCompatActivity() {

    private lateinit var viewModel: PokemonStoreViewModel
    private lateinit var adapter: PokemonStoreAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tienda_pokemon)

        viewModel = ViewModelProvider(this).get(PokemonStoreViewModel::class.java)

        val tvStoreScore = findViewById<android.widget.TextView>(R.id.tvStoreScore)
        val rvStore = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvPokemonStore)

        rvStore.layoutManager = GridLayoutManager(this, 3)

        adapter = PokemonStoreAdapter { pokemon ->
            lifecycleScope.launch {
                val success = viewModel.buyPokemon(pokemon)
                if (success) {
                    viewModel.deductScore(pokemon.cost)
                    tvStoreScore.text = "Puntaje disponible: ${GameStateManager.score}"
                    Toast.makeText(
                        this@TiendaPokemonActivity,
                        "${pokemon.name} comprado!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        rvStore.adapter = adapter

        tvStoreScore.text = "Puntaje disponible: ${GameStateManager.score}"

        viewModel.allPokemon.observe(this) { pokemonList ->
            val purchasedIds = viewModel.purchasedPokemonIds.value?.toSet() ?: emptySet()
            adapter.updateData(pokemonList, purchasedIds, GameStateManager.score)
        }

        viewModel.purchasedPokemonIds.observe(this) { purchasedIds ->
            viewModel.allPokemon.value?.let { pokemonList ->
                adapter.updateData(pokemonList, purchasedIds.toSet(), GameStateManager.score)
            }
        }

        viewModel.buyResult.observe(this) { result ->
            when (result) {
                is PurchaseResult.NotEnoughPoints ->
                    Toast.makeText(this, "No tienes suficientes puntos", Toast.LENGTH_SHORT).show()
                is PurchaseResult.AlreadyPurchased ->
                    Toast.makeText(this, "Ya tienes este Pokémon", Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }
}
