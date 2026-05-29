package com.example.juego.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// --- VIEW MODEL DE LA TIENDA ---
// Extiende AndroidViewModel para acceder al Application context.
// Gestiona el estado de la tienda: catálogo, compras, puntaje global.
// Expone LiveData que el Activity observa para actualizar la UI automáticamente.

class PokemonStoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PokemonRepository.getInstance(application)
    private val sessionManager = com.example.juego.SessionManager(application)

    // userId del usuario logueado (vía SessionManager de Kevin).
    val currentUserId: Int
        get() = sessionManager.getUserId()

    // globalScore: puntaje acumulado que se carga desde Room.
    // Se usa para verificar si alcanza para comprar y se descuenta tras cada compra.
    var globalScore: Int = 0
        private set

    // LiveData que observan cambios en la DB automáticamente (Room Flow -> LiveData).
    val allPokemon: LiveData<List<PokemonEntity>> by lazy {
        repository.getAllPokemon().asLiveData(viewModelScope.coroutineContext)
    }

    // IDs de Pokémon ya comprados por el usuario.
    val purchasedPokemonIds: LiveData<List<Int>> by lazy {
        repository.getPurchasedPokemonIds(currentUserId)
            .asLiveData(viewModelScope.coroutineContext)
    }

    // Pokémon más caro adquirido (para mostrar en la sección del juego).
    val mostExpensivePokemon: LiveData<PokemonEntity?> by lazy {
        repository.getMostExpensivePurchased(currentUserId)
            .asLiveData(viewModelScope.coroutineContext)
    }

    // Resultado de la última compra (para mostrar Toast al usuario).
    private val _buyResult = MutableLiveData<PurchaseResult>()
    val buyResult: LiveData<PurchaseResult> = _buyResult

    // init: carga globalScore desde la DB al crear el ViewModel.
    init {
        viewModelScope.launch {
            val user = repository.getUserById(currentUserId)
            globalScore = user?.globalScore ?: 0
        }
    }

    // --- refreshGlobalScore(): Suspend function ---
    // Recarga el globalScore desde Room.
    // El Activity la llama con lifecycleScope.launch y espera el resultado.
    suspend fun refreshGlobalScore() {
        val user = repository.getUserById(currentUserId)
        globalScore = user?.globalScore ?: globalScore
    }

    // --- buyPokemon(): Intento de compra ---
    // Delega en repository.buyPokemon() que verifica puntaje y duplicados.
    // Retorna true si la compra fue exitosa.
    suspend fun buyPokemon(pokemon: PokemonEntity): Boolean {
        val result = repository.buyPokemon(currentUserId, pokemon, globalScore)
        _buyResult.postValue(result)
        return result is PurchaseResult.Success
    }

    // --- deductScore(): Descontar puntaje tras compra exitosa ---
    // Resta del globalScore en memoria y persiste el cambio en Room (amount negativo).
    fun deductScore(amount: Int) {
        globalScore -= amount
        viewModelScope.launch {
            repository.addToGlobalScore(currentUserId, -amount)
        }
    }
}
