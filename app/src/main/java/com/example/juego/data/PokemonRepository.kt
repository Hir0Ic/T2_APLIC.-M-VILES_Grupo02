package com.example.juego.data

import android.content.Context

class PokemonRepository(context: Context) {

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

    suspend fun registerUser(username: String, email: String, password: String): RegisterResult {
        val existing = dao.getUserByUsernameOrEmail(username.trim(), email.trim().lowercase())
        if (existing != null) return RegisterResult.Duplicate
        val user = UserEntity(
            username = username.trim(),
            email = email.trim().lowercase(),
            password = password
        )
        dao.insertUser(user)
        return RegisterResult.Success
    }

    suspend fun loginUser(credential: String, password: String): UserEntity? {
        return dao.loginUser(credential.trim(), password)
    }

    suspend fun getUserById(userId: Int): UserEntity? {
        return dao.getUserById(userId)
    }

    suspend fun updateUserScore(userId: Int, score: Int) {
        dao.updateUserScore(userId, score)
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

sealed class RegisterResult {
    object Success : RegisterResult()
    object Duplicate : RegisterResult()
}
