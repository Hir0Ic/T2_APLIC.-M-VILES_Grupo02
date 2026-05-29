package com.example.juego.data

import android.content.Context

// --- REPOSITORY (Singleton) ---
// Capa intermedia entre ViewModel y DAO.
// Centraliza la lógica de negocio: compras, auth, puntaje global.

class PokemonRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).pokemonDao()

    fun getAllPokemon() = dao.getAllPokemon()

    fun getPurchasedPokemonIds(userId: Int) = dao.getPurchasedPokemonIds(userId)

    fun getMostExpensivePurchased(userId: Int) = dao.getMostExpensivePurchased(userId)

    // --- buyPokemon(): LÓGICA CENTRAL DE COMPRA ---
    // 1. Verifica si el Pokémon ya fue comprado (evita duplicados).
    // 2. Verifica si el usuario tiene puntaje suficiente.
    // 3. Si pasa ambas validaciones, inserta el registro en la tabla purchases.
    // 4. Retorna un PurchaseResult sellado: Success / NotEnoughPoints / AlreadyPurchased.
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

    // --- registerUser(): REGISTRO ---
    // Verifica duplicados por username o email.
    // Si existe -> RegisterResult.Duplicate. Si no -> inserta y retorna Success.
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

    // --- loginUser(): INICIO DE SESIÓN ---
    // Busca por username O email + contraseña.
    // Retorna UserEntity? -> null si credenciales inválidas.
    suspend fun loginUser(credential: String, password: String): UserEntity? {
        return dao.loginUser(credential.trim(), password)
    }

    // --- getUserById(): OBTENER USUARIO ---
    // Carga datos completos del usuario (incluyendo globalScore).
    suspend fun getUserById(userId: Int): UserEntity? {
        return dao.getUserById(userId)
    }

    // --- addToGlobalScore(): SUMAR PUNTAJE GLOBAL ---
    // Recibe userId y amount (puede ser positivo o negativo).
    // Ejecuta: UPDATE users SET globalScore = globalScore + amount
    // amount > 0: suma puntaje de sesión al global.
    // amount < 0: descuenta compra de la tienda del global.
    suspend fun addToGlobalScore(userId: Int, amount: Int) {
        if (amount != 0) {
            dao.addToGlobalScore(userId, amount)
        }
    }

    suspend fun getPurchasedPokemons(userId: Int): List<PokemonEntity> {
        return dao.getPurchasedPokemons(userId)
    }

    companion object {
        @Volatile
        private var INSTANCE: PokemonRepository? = null

        // Singleton: único punto de acceso al repositorio en toda la app.
        fun getInstance(context: Context): PokemonRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PokemonRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

// --- RESULTADOS SELLADOS ---
// PurchaseResult: Success, NotEnoughPoints, AlreadyPurchased.
// RegisterResult: Success, Duplicate.
sealed class PurchaseResult {
    object Success : PurchaseResult()
    object NotEnoughPoints : PurchaseResult()
    object AlreadyPurchased : PurchaseResult()
}

sealed class RegisterResult {
    object Success : RegisterResult()
    object Duplicate : RegisterResult()
}
