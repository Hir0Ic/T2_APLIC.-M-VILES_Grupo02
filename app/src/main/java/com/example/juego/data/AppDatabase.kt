package com.example.juego.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

// --- CLASE AppDatabase ---
// RoomDatabase: abstracción sobre SQLite que maneja toda la persistencia.
// Define 3 tablas: pokemon_catalog, purchases, users.
// Versión 3 -> fallbackToDestructiveMigration() recrea la DB si cambia el esquema.

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

        // --- SINGLETON (Double-Checked Locking) ---
        // getInstance(): único punto de acceso a la DB.
        // Si INSTANCE es null, sincroniza para crear una sola instancia.
        // .fallbackToDestructiveMigration(): si la versión cambia, borra y recrea.
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

        // --- SEED CALLBACK ---
        // SeedCallback.onCreate(): se ejecuta UNA SOLA VEZ cuando se crea la DB.
        // Inserta los 9 Pokémon del catálogo usando raw SQL (porque INSTANCE aún es null).
        // "INSERT OR IGNORE" evita duplicados si el seed se ejecuta de nuevo.
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

        // --- DATOS INICIALES ---
        // POKEMON_SEED_DATA: catálogo de 9 Pokémon con nombre, imagen y costo.
        // Costos progresivos: Magikarp(5) → Blastoise(30).
        // imageResName = nombre del archivo PNG en res/drawable/ (sin extensión).
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
