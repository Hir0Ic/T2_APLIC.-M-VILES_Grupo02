package com.example.juego.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PokemonEntity::class, PurchaseEntity::class, UserEntity::class],
    version = 3,
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
                POKEMON_SEED_DATA.forEach { pokemon ->
                    db.execSQL(
                        "INSERT OR IGNORE INTO pokemon_catalog (id, name, imageResName, cost) VALUES (?, ?, ?, ?)",
                        arrayOf(pokemon.id.toString(), pokemon.name, pokemon.imageResName, pokemon.cost.toString())
                    )
                }
            }
        }

        val POKEMON_SEED_DATA = listOf(
            PokemonEntity(1, "Magikarp", "magikarp", 5),
            PokemonEntity(2, "Diglett", "diglett", 8),
            PokemonEntity(3, "Nidoran", "nidoran", 10),
            PokemonEntity(4, "Charmander", "charmander", 12),
            PokemonEntity(5, "Jigglypuff", "jigglypuff", 14),
            PokemonEntity(6, "Squirtle", "squirtle", 15),
            PokemonEntity(7, "Vulpix", "vulpix", 20),
            PokemonEntity(8, "Vaporeon", "vaporeon", 25),
            PokemonEntity(9, "Blastoise", "blastoise", 30)
        )
    }
}
