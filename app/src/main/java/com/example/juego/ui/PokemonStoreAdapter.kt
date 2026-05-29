package com.example.juego.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.juego.R
import com.example.juego.data.PokemonEntity

class PokemonStoreAdapter(
    private val onBuyClick: (PokemonEntity) -> Unit
) : RecyclerView.Adapter<PokemonStoreAdapter.ViewHolder>() {

    private var pokemonList = listOf<PokemonEntity>()
    private var purchasedIds = setOf<Int>()
    private var currentScore = 0

    fun updateData(pokemon: List<PokemonEntity>, purchased: Set<Int>, score: Int) {
        pokemonList = pokemon
        purchasedIds = purchased
        currentScore = score
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon_store, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pokemonList[position])
    }

    override fun getItemCount(): Int = pokemonList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPokemon = itemView.findViewById<android.widget.ImageView>(R.id.ivPokemon)
        private val tvName = itemView.findViewById<android.widget.TextView>(R.id.tvPokemonName)
        private val tvCost = itemView.findViewById<android.widget.TextView>(R.id.tvPokemonCost)
        private val btnBuy = itemView.findViewById<android.widget.Button>(R.id.btnBuy)

        fun bind(pokemon: PokemonEntity) {
            val context = itemView.context
            val resId = context.resources.getIdentifier(
                pokemon.imageResName, "drawable", context.packageName
            )
            if (resId != 0) {
                ivPokemon.setImageResource(resId)
            }

            tvName.text = pokemon.name
            tvCost.text = "Costo: ${pokemon.cost} pts"

            val isPurchased = purchasedIds.contains(pokemon.id)
            val canAfford = currentScore >= pokemon.cost

            when {
                isPurchased -> {
                    btnBuy.text = "Comprado"
                    btnBuy.isEnabled = false
                    btnBuy.setBackgroundColor(context.getColor(android.R.color.darker_gray))
                }
                !canAfford -> {
                    btnBuy.text = "Sin puntos"
                    btnBuy.isEnabled = false
                    btnBuy.setBackgroundColor(context.getColor(android.R.color.darker_gray))
                }
                else -> {
                    btnBuy.text = "Comprar"
                    btnBuy.isEnabled = true
                    btnBuy.setBackgroundColor(context.getColor(R.color.green_primary))
                    btnBuy.setOnClickListener { onBuyClick(pokemon) }
                }
            }
        }
    }
}
