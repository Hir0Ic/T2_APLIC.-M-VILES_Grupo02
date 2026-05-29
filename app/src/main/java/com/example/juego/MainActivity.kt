package com.example.juego

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.juego.data.GameStateManager
import com.example.juego.data.PokemonRepository
import com.example.juego.data.UserEntity
import com.example.juego.ui.TiendaPokemonActivity
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

enum class GameState { IDLE, PLAYING, ENDED }

class MainActivity : AppCompatActivity() {

    var sessionScore = 0
    var globalScore = 0
    var imageArray = ArrayList<ImageView>()
    var handler = Handler()
    var runnable = Runnable {  }

    private var indicePikachu = -1
    private var indiceMeowth = -1
    private var gameState = GameState.IDLE
    private var showDialogPending = false
    private var lastSessionScore = 0
    private var endDialogShown = false

    private lateinit var timeText: TextView
    private lateinit var scoreText: TextView
    private lateinit var tvWelcomeUser: TextView
    private lateinit var btnLogout: Button

    private lateinit var sessionManager: SessionManager
    private var activeUser: UserEntity? = null
    private var activePokemonImage: String = "pikachu"
    private var activePokemonName: String = "Pikachu"

    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            launchLoginActivity()
            return
        }

        setContentView(R.layout.activity_main)

        val userId = sessionManager.getUserId()

        lifecycleScope.launch {
            val user = PokemonRepository.getInstance(this@MainActivity).getUserById(userId)
            if (user == null) {
                sessionManager.logout()
                launchLoginActivity()
                return@launch
            }
            activeUser = user
            globalScore = user.globalScore

            val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
            activePokemonImage = prefs.getString("active_pokemon_image", "pikachu") ?: "pikachu"
            activePokemonName = prefs.getString("active_pokemon_name", "Pikachu") ?: "Pikachu"

            GameStateManager.sessionScore = 0
            sessionScore = 0

            runOnUiThread {
                timeText = findViewById<TextView>(R.id.timeText)
                scoreText = findViewById<TextView>(R.id.scoreText)
                tvWelcomeUser = findViewById<TextView>(R.id.tvWelcomeUser)
                btnLogout = findViewById<Button>(R.id.btnLogout)

                tvWelcomeUser.text = "¡Hola, ${user.username}!"
                updateScoreDisplay()

                setupGame()
                startGame()

                btnLogout.setOnClickListener {
                    stopGame()
                    lifecycleScope.launch {
                        PokemonRepository.getInstance(this@MainActivity)
                            .addToGlobalScore(sessionManager.getUserId(), sessionScore)
                    }
                    sessionManager.logout()
                    launchLoginActivity()
                }

                val btnTienda = findViewById<android.widget.Button>(R.id.btnTienda)
                btnTienda.setOnClickListener {
                    val intent = Intent(this@MainActivity, TiendaPokemonActivity::class.java)
                    startActivity(intent)
                }

                loadMostExpensivePokemon()
            }
        }
    }

    private fun setupGame() {
        val imageView: ImageView = findViewById(R.id.imageView)
        imageView.setImageResource(R.drawable.pikachu)
        val imageView2: ImageView = findViewById(R.id.imageView2)
        imageView2.setImageResource(R.drawable.pikachu)
        val imageView3: ImageView = findViewById(R.id.imageView3)
        imageView3.setImageResource(R.drawable.pikachu)
        val imageView4: ImageView = findViewById(R.id.imageView4)
        imageView4.setImageResource(R.drawable.pikachu)
        val imageView5: ImageView = findViewById(R.id.imageView5)
        imageView5.setImageResource(R.drawable.pikachu)
        val imageView6: ImageView = findViewById(R.id.imageView6)
        imageView6.setImageResource(R.drawable.pikachu)
        val imageView7: ImageView = findViewById(R.id.imageView7)
        imageView7.setImageResource(R.drawable.pikachu)
        val imageView8: ImageView = findViewById(R.id.imageView8)
        imageView8.setImageResource(R.drawable.pikachu)
        val imageView9: ImageView = findViewById(R.id.imageView9)
        imageView9.setImageResource(R.drawable.pikachu)

        imageArray.add(imageView)
        imageArray.add(imageView2)
        imageArray.add(imageView3)
        imageArray.add(imageView4)
        imageArray.add(imageView5)
        imageArray.add(imageView6)
        imageArray.add(imageView7)
        imageArray.add(imageView8)
        imageArray.add(imageView9)

        for (i in 0 until imageArray.size) {
            imageArray[i].setOnClickListener {
                if (gameState != GameState.PLAYING) return@setOnClickListener
                if (i == indicePikachu) {
                    sessionScore += 1
                } else if (i == indiceMeowth) {
                    sessionScore -= 2
                }
                GameStateManager.sessionScore = sessionScore
                updateScoreDisplay()
            }
        }
    }

    private fun startGame() {
        gameState = GameState.PLAYING
        showDialogPending = false
        endDialogShown = false
        lastSessionScore = 0
        sessionScore = 0
        GameStateManager.sessionScore = 0
        updateScoreDisplay()

        val activeResId = getPokemonDrawable(activePokemonImage)
        for (image in imageArray) {
            image.setImageResource(activeResId)
        }

        hideImages()

        countDownTimer = object : CountDownTimer(75000, 1000) {
            override fun onFinish() {
                gameState = GameState.ENDED
                timeText.text = "Tiempo: 00:00"
                handler.removeCallbacks(runnable)
                for (image in imageArray) {
                    image.visibility = View.INVISIBLE
                }
                lastSessionScore = sessionScore

                lifecycleScope.launch {
                    PokemonRepository.getInstance(this@MainActivity)
                        .addToGlobalScore(sessionManager.getUserId(), sessionScore)
                    globalScore += sessionScore
                    GameStateManager.sessionScore = 0
                    sessionScore = 0
                    runOnUiThread { updateScoreDisplay() }
                }
                showDialogPending = true
                tryShowEndGameDialog()
            }

            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                timeText.text = String.format("Tiempo: %02d:%02d", minutes, seconds)
            }
        }.start()
    }

    private fun stopGame() {
        gameState = GameState.IDLE
        countDownTimer?.cancel()
        handler.removeCallbacks(runnable)
        for (image in imageArray) {
            image.visibility = View.INVISIBLE
        }
    }

    private fun tryShowEndGameDialog() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            showDialogPending = false
            showEndGameDialog()
        }
    }

    private fun showEndGameDialog() {
        if (endDialogShown) return
        endDialogShown = true
        showDialogPending = false
        val (titulo, mensaje) = when {
            lastSessionScore >= 30 -> "¡GANASTE!" to "Felicidades, lograste $lastSessionScore puntos.\nPuntaje global: $globalScore"
            lastSessionScore <= 0 -> "PERDISTE" to "Tu puntaje es $lastSessionScore. Inténtalo de nuevo.\nPuntaje global: $globalScore"
            else -> "Juego Terminado" to "Obtuviste $lastSessionScore puntos. Necesitas 30 para ganar.\nPuntaje global: $globalScore"
        }

        val alert = AlertDialog.Builder(this@MainActivity)
        alert.setTitle(titulo)
        alert.setMessage(mensaje)
        alert.setCancelable(false)
        alert.setPositiveButton("Reiniciar juego") { _, _ ->
            lifecycleScope.launch {
                val user = PokemonRepository.getInstance(this@MainActivity)
                    .getUserById(sessionManager.getUserId())
                globalScore = user?.globalScore ?: globalScore
                runOnUiThread {
                    updateScoreDisplay()
                    startGame()
                }
            }
        }
        alert.setNeutralButton("Ir a la tienda") { _, _ ->
            val intent = Intent(this@MainActivity, TiendaPokemonActivity::class.java)
            startActivity(intent)
        }
        alert.setNegativeButton("Salir") { _, _ ->
            stopGame()
            finish()
        }
        alert.show()
    }

    fun hideImages() {
        runnable = object : Runnable {
            override fun run() {
                if (gameState != GameState.PLAYING) return

                for (image in imageArray) {
                    image.visibility = View.INVISIBLE
                }

                val random = Random()
                indicePikachu = random.nextInt(9)
                val activeResId = getPokemonDrawable(activePokemonImage)
                imageArray[indicePikachu].setImageResource(activeResId)
                imageArray[indicePikachu].visibility = View.VISIBLE

                do {
                    indiceMeowth = random.nextInt(9)
                } while (indiceMeowth == indicePikachu)

                imageArray[indiceMeowth].setImageResource(R.drawable.meowth)
                imageArray[indiceMeowth].visibility = View.VISIBLE

                handler.postDelayed(runnable, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun updateScoreDisplay() {
        scoreText.text = "Sesión: $sessionScore  |  Global: $globalScore"
    }

    private fun loadMostExpensivePokemon() {
        val ivPurchased = findViewById<ImageView>(R.id.ivPurchasedPokemon)
        val tvPurchased = findViewById<TextView>(R.id.tvPurchasedPokemon)
        val purchasedSection = findViewById<LinearLayout>(R.id.purchasedSection)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val userId = sessionManager.getUserId()
                if (userId == -1) return@repeatOnLifecycle
                PokemonRepository.getInstance(this@MainActivity)
                    .getMostExpensivePurchased(userId)
                    .collect { pokemon ->
                        if (pokemon != null) {
                            val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
                            if (!prefs.getBoolean("manual_pokemon_selection", false)) {
                                activePokemonImage = pokemon.imageResName
                                activePokemonName = pokemon.name
                                prefs.edit()
                                    .putString("active_pokemon_image", activePokemonImage)
                                    .putString("active_pokemon_name", activePokemonName)
                                    .apply()
                            }
                            val resId = getPokemonDrawable(activePokemonImage)
                            ivPurchased.setImageResource(resId)
                            tvPurchased.text = "Tu Pokémon: $activePokemonName"
                            purchasedSection.visibility = View.VISIBLE
                        } else {
                            activePokemonImage = "pikachu"
                            activePokemonName = "Pikachu"
                            getSharedPreferences("sesion", MODE_PRIVATE)
                                .edit()
                                .putBoolean("manual_pokemon_selection", false)
                                .apply()
                            purchasedSection.visibility = View.GONE
                        }
                    }
            }
        }
    }

    fun cambiarPokemon(unused: View) {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            val purchased = PokemonRepository.getInstance(this@MainActivity)
                .getPurchasedPokemons(userId)

            if (purchased.isEmpty()) return@launch

            val nombres = purchased.map { it.name }.toTypedArray()

            AlertDialog.Builder(this@MainActivity)
                .setTitle("Elegir Pokémon")
                .setItems(nombres) { _, which ->
                    val selected = purchased[which]
                    activePokemonImage = selected.imageResName
                    activePokemonName = selected.name
                    getSharedPreferences("sesion", MODE_PRIVATE)
                        .edit()
                        .putString("active_pokemon_image", activePokemonImage)
                        .putString("active_pokemon_name", activePokemonName)
                        .putBoolean("manual_pokemon_selection", true)
                        .apply()

                    val tvPurchased = findViewById<TextView>(R.id.tvPurchasedPokemon)
                    tvPurchased.text = "Tu Pokémon: $activePokemonName"

                    val ivPurchased = findViewById<ImageView>(R.id.ivPurchasedPokemon)
                    ivPurchased.setImageResource(getPokemonDrawable(activePokemonImage))
                }
                .show()
        }
    }

    private fun getPokemonDrawable(imageName: String): Int {
        return when (imageName) {
            "magikarp" -> R.drawable.magikarp
            "diglett" -> R.drawable.diglett
            "nidoran" -> R.drawable.nidoran
            "charmander" -> R.drawable.charmander
            "jigglypuff" -> R.drawable.jigglypuff
            "squirtle" -> R.drawable.squirtle
            "vulpix" -> R.drawable.vulpix
            "vaporeon" -> R.drawable.vaporeon
            "blastoise" -> R.drawable.blastoise
            "pikachu" -> R.drawable.pikachu
            "meowth" -> R.drawable.meowth
            else -> R.drawable.pikachu
        }
    }

    private fun launchLoginActivity() {
        handler.removeCallbacks(runnable)
        countDownTimer?.cancel()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (::sessionManager.isInitialized && sessionManager.isLoggedIn()) {
            lifecycleScope.launch {
                val user = PokemonRepository.getInstance(this@MainActivity)
                    .getUserById(sessionManager.getUserId())
                user?.let {
                    globalScore = it.globalScore
                    runOnUiThread { updateScoreDisplay() }
                }
            }
            if (showDialogPending && !endDialogShown) {
                showDialogPending = false
                showEndGameDialog()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        countDownTimer?.cancel()
    }
}
