package com.example.juego.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [PokemonEntity::class, PurchaseEntity::class, UserEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pokemonDao(): PokemonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pokemon_game.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(SeedCallback())
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private class SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.pokemonDao().insertAllPokemon(POKEMON_SEED_DATA)
                    }
                }
            }
        }

        val POKEMON_SEED_DATA = listOf(
            PokemonEntity(1, "Bulbasaur", "ic_pokemon_bulbasaur", 5),
            PokemonEntity(2, "Charmander", "ic_pokemon_charmander", 8),
            PokemonEntity(3, "Squirtle", "ic_pokemon_squirtle", 8),
            PokemonEntity(4, "Pikachu", "pikachu", 10),
            PokemonEntity(5, "Meowth", "meowth", 6),
            PokemonEntity(6, "Eevee", "ic_pokemon_eevee", 12),
            PokemonEntity(7, "Snorlax", "ic_pokemon_snorlax", 20),
            PokemonEntity(8, "Gengar", "ic_pokemon_gengar", 25),
            PokemonEntity(9, "Mewtwo", "ic_pokemon_mewtwo", 50)
        )
    }
}
