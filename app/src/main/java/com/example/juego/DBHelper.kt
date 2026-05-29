package com.example.juego

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "pokemon_clicker.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pokemon_comprados (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                jugador_id INTEGER NOT NULL,
                nombre TEXT NOT NULL,
                imagen TEXT NOT NULL,
                costo INTEGER NOT NULL,
                UNIQUE(jugador_id, nombre)
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS pokemon_comprados")
        onCreate(db)
    }



    fun comprarPokemon(jugadorId: Int, nombre: String, imagen: String, costo: Int): Boolean {
        val db = writableDatabase

        if (estaComprado(jugadorId, nombre)) {
            return false
        }

        val valores = ContentValues()
        valores.put("jugador_id", jugadorId)
        valores.put("nombre", nombre)
        valores.put("imagen", imagen)
        valores.put("costo", costo)

        val resultado = db.insert("pokemon_comprados", null, valores)
        return resultado != -1L
    }

    fun estaComprado(jugadorId: Int, nombre: String): Boolean {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT id FROM pokemon_comprados WHERE jugador_id = ? AND nombre = ?",
            arrayOf(jugadorId.toString(), nombre)
        )

        val existe = cursor.count > 0
        cursor.close()

        return existe
    }

    fun obtenerPokemonMasCaro(jugadorId: Int): String? {
        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT imagen 
        FROM pokemon_comprados 
        WHERE jugador_id = ? 
        ORDER BY costo DESC 
        LIMIT 1
        """.trimIndent(),
            arrayOf(jugadorId.toString())
        )

        var imagen: String? = null

        if (cursor.moveToFirst()) {
            imagen = cursor.getString(0)
        }

        cursor.close()
        return imagen
    }
}