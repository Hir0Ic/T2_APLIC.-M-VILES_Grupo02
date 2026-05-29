package com.example.juego.data

// --- GAME STATE MANAGER ---
// Objeto singleton compartido entre MainActivity y TiendaPokemonActivity.
// sessionScore: puntaje de la partida ACTUAL (se resetea a 0 al iniciar un nuevo juego).
// La tienda lee este valor para saber cuánto puede gastar el usuario durante la partida.
// Cuando el juego termina, sessionScore se suma a globalScore en la DB y se resetea.
object GameStateManager {
    var sessionScore: Int = 0
}
