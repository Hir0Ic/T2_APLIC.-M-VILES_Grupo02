package com.example.juego.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PokemonStoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PokemonRepository.getInstance(application)
    private val sessionManager = com.example.juego.SessionManager(application)

    val currentUserId: Int
        get() = sessionManager.getUserId()

    var globalScore: Int = 0
        private set

    val allPokemon: LiveData<List<PokemonEntity>> by lazy {
        repository.getAllPokemon().asLiveData(viewModelScope.coroutineContext)
    }

    val purchasedPokemonIds: LiveData<List<Int>> by lazy {
        repository.getPurchasedPokemonIds(currentUserId)
            .asLiveData(viewModelScope.coroutineContext)
    }

    val mostExpensivePokemon: LiveData<PokemonEntity?> by lazy {
        repository.getMostExpensivePurchased(currentUserId)
            .asLiveData(viewModelScope.coroutineContext)
    }

    private val _buyResult = MutableLiveData<PurchaseResult>()
    val buyResult: LiveData<PurchaseResult> = _buyResult

    init {
        viewModelScope.launch {
            val user = repository.getUserById(currentUserId)
            globalScore = user?.globalScore ?: 0
        }
    }

    suspend fun refreshGlobalScore() {
        val user = repository.getUserById(currentUserId)
        globalScore = user?.globalScore ?: globalScore
    }

    suspend fun buyPokemon(pokemon: PokemonEntity): Boolean {
        val result = repository.buyPokemon(currentUserId, pokemon, globalScore)
        _buyResult.postValue(result)
        return result is PurchaseResult.Success
    }

    fun deductScore(amount: Int) {
        globalScore -= amount
        viewModelScope.launch {
            repository.addToGlobalScore(currentUserId, -amount)
        }
    }
}
