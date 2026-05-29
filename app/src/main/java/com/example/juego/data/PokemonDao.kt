package com.example.juego.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllPokemon(pokemonList: List<PokemonEntity>)

    @Query("SELECT * FROM pokemon_catalog ORDER BY id")
    fun getAllPokemon(): Flow<List<PokemonEntity>>

    @Query("SELECT * FROM pokemon_catalog WHERE id = :pokemonId")
    suspend fun getPokemonById(pokemonId: Int): PokemonEntity?

    @Query("SELECT pokemonId FROM purchases WHERE userId = :userId")
    fun getPurchasedPokemonIds(userId: Int): Flow<List<Int>>

    @Query("""
        SELECT p.* FROM pokemon_catalog p 
        INNER JOIN purchases pu ON p.id = pu.pokemonId 
        WHERE pu.userId = :userId 
        ORDER BY p.cost DESC LIMIT 1
    """)
    fun getMostExpensivePurchased(userId: Int): Flow<PokemonEntity?>

    @Query("SELECT COUNT(*) FROM purchases WHERE userId = :userId AND pokemonId = :pokemonId")
    suspend fun isPokemonPurchased(userId: Int, pokemonId: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPurchase(purchase: PurchaseEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :username OR email = :email LIMIT 1")
    suspend fun getUserByUsernameOrEmail(username: String, email: String): UserEntity?

    @Query("""
        SELECT * FROM users 
        WHERE (username = :credential OR email = :credential) 
        AND password = :password LIMIT 1
    """)
    suspend fun loginUser(credential: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): UserEntity?

    @Query("UPDATE users SET globalScore = globalScore + :amount WHERE id = :userId")
    suspend fun addToGlobalScore(userId: Int, amount: Int)

    @Query("SELECT p.* FROM pokemon_catalog p INNER JOIN purchases pu ON p.id = pu.pokemonId WHERE pu.userId = :userId ORDER BY p.cost DESC")
    suspend fun getPurchasedPokemons(userId: Int): List<PokemonEntity>
}
