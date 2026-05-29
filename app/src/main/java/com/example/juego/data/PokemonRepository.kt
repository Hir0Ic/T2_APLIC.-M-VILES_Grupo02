package com.example.juego.data

import android.content.Context

class PokemonRepository(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).pokemonDao()

    fun getAllPokemon() = dao.getAllPokemon()

    fun getPurchasedPokemonIds(userId: Int) = dao.getPurchasedPokemonIds(userId)

    fun getMostExpensivePurchased(userId: Int) = dao.getMostExpensivePurchased(userId)

    suspend fun buyPokemon(userId: Int, pokemon: PokemonEntity, currentScore: Int): PurchaseResult {
        val alreadyPurchased = dao.isPokemonPurchased(userId, pokemon.id) > 0
        if (alreadyPurchased) return PurchaseResult.AlreadyPurchased
        if (currentScore < pokemon.cost) return PurchaseResult.NotEnoughPoints
        dao.insertPurchase(PurchaseEntity(userId = userId, pokemonId = pokemon.id))
        return PurchaseResult.Success
    }

    suspend fun isPurchased(userId: Int, pokemonId: Int): Boolean {
        return dao.isPokemonPurchased(userId, pokemonId) > 0
    }

    companion object {
        @Volatile
        private var INSTANCE: PokemonRepository? = null

        fun getInstance(context: Context): PokemonRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PokemonRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

sealed class PurchaseResult {
    object Success : PurchaseResult()
    object NotEnoughPoints : PurchaseResult()
    object AlreadyPurchased : PurchaseResult()
}
