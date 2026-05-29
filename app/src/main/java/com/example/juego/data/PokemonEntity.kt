package com.example.juego.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon_catalog")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageResName: String,
    val cost: Int
)
