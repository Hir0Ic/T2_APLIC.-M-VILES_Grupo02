package com.example.juego

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "pokemon_auth.db"
        private const val DATABASE_VERSION = 1

        // Tabla Usuarios
        const val TABLE_USUARIOS = "usuarios"
        const val COL_USER_ID = "id"
        const val COL_USER_NAME = "nombre_usuario"
        const val COL_USER_EMAIL = "correo"
        const val COL_USER_PASSWORD = "contraseña"
        const val COL_USER_SCORE = "puntaje"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla de usuarios
        val createUsuariosTable = ("CREATE TABLE $TABLE_USUARIOS (" +
                "$COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COL_USER_NAME TEXT UNIQUE NOT NULL," +
                "$COL_USER_EMAIL TEXT UNIQUE NOT NULL," +
                "$COL_USER_PASSWORD TEXT NOT NULL," +
                "$COL_USER_SCORE INTEGER DEFAULT 0)")
        db.execSQL(createUsuariosTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        onCreate(db)
    }

    // --- MÉTODOS DE USUARIO / AUTENTICACIÓN ---

    fun registrarUsuario(nombre: String, correo: String, contrasena: String): Long {
        val db = this.writableDatabase

        // Verificar si ya existe el nombre de usuario o correo
        if (verificarDuplicado(nombre, correo)) {
            return -1 // Código de error por duplicado
        }

        val values = ContentValues().apply {
            put(COL_USER_NAME, nombre.trim())
            put(COL_USER_EMAIL, correo.trim().lowercase())
            put(COL_USER_PASSWORD, contrasena)
            put(COL_USER_SCORE, 0)
        }

        return db.insert(TABLE_USUARIOS, null, values)
    }

    fun verificarDuplicado(nombre: String, correo: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT $COL_USER_ID FROM $TABLE_USUARIOS WHERE $COL_USER_NAME = ? OR $COL_USER_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(nombre.trim(), correo.trim().lowercase()))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun loginUsuario(credencial: String, contrasena: String): Int {
        val db = this.readableDatabase
        val query = "SELECT $COL_USER_ID FROM $TABLE_USUARIOS WHERE ($COL_USER_NAME = ? OR $COL_USER_EMAIL = ?) AND $COL_USER_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(credencial.trim(), credencial.trim().lowercase(), contrasena))
        var userId = -1
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0)
        }
        cursor.close()
        return userId
    }

    fun getUsuario(userId: Int): Usuario? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USUARIOS WHERE $COL_USER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = Usuario(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                nombreUsuario = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)),
                correo = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                puntaje = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_SCORE))
            )
        }
        cursor.close()
        return usuario
    }

    fun actualizarPuntaje(userId: Int, nuevoPuntaje: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_SCORE, nuevoPuntaje)
        }
        db.update(TABLE_USUARIOS, values, "$COL_USER_ID = ?", arrayOf(userId.toString()))
    }
}

// Clase de datos auxiliar
data class Usuario(
    val id: Int,
    val nombreUsuario: String,
    val correo: String,
    var puntaje: Int
)
