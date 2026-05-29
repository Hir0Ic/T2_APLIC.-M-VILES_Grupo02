package com.example.juego

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var score = 0
    var imageArray = ArrayList<ImageView>()
    var handler = Handler()
    var runnable = Runnable {  }

    private lateinit var timeText: TextView
    private lateinit var scoreText: TextView
    private lateinit var tvWelcomeUser: TextView
    private lateinit var btnLogout: Button

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private var activeUser: Usuario? = null

    // LÓGICA DE ALONSO: Indices dinámicos de posición simultánea
    private var indicePikachu = -1
    private var indiceMeowth = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        dbHelper = DatabaseHelper(this)

        // Validar sesión de tus compañeros
        if (!sessionManager.isLoggedIn()) {
            launchLoginActivity()
            return
        }

        setContentView(R.layout.activity_main)

        // Obtener datos del jugador en base de datos
        val userId = sessionManager.getUserId()
        activeUser = dbHelper.getUsuario(userId)

        if (activeUser == null) {
            sessionManager.logout()
            launchLoginActivity()
            return
        }

        score = activeUser!!.puntaje

        // Inicializar vistas
        timeText = findViewById<TextView>(R.id.timeText)
        scoreText = findViewById<TextView>(R.id.scoreText)
        tvWelcomeUser = findViewById<TextView>(R.id.tvWelcomeUser)
        btnLogout = findViewById<Button>(R.id.btnLogout)

        // Mostrar datos de bienvenida
        tvWelcomeUser.text = "¡Hola, ${activeUser!!.nombreUsuario}!"
        scoreText.text = "Puntaje: $score"

        val imageView: ImageView = findViewById(R.id.imageView)
        val imageView2: ImageView = findViewById(R.id.imageView2)
        val imageView3: ImageView = findViewById(R.id.imageView3)
        val imageView4: ImageView = findViewById(R.id.imageView4)
        val imageView5: ImageView = findViewById(R.id.imageView5)
        val imageView6: ImageView = findViewById(R.id.imageView6)
        val imageView7: ImageView = findViewById(R.id.imageView7)
        val imageView8: ImageView = findViewById(R.id.imageView8)
        val imageView9: ImageView = findViewById(R.id.imageView9)

        // ImageArray unificado
        imageArray.add(imageView)
        imageArray.add(imageView2)
        imageArray.add(imageView3)
        imageArray.add(imageView4)
        imageArray.add(imageView5)
        imageArray.add(imageView6)
        imageArray.add(imageView7)
        imageArray.add(imageView8)
        imageArray.add(imageView9)

        hideImages()

        // CountDown Timer original del grupo
        object : CountDownTimer(15500, 1000) {
            override fun onFinish() {
                timeText.text = "Tiempo: 0 seg"
                handler.removeCallbacks(runnable)
                for (image in imageArray) {
                    image.visibility = View.INVISIBLE
                }

                val alert = AlertDialog.Builder(this@MainActivity)
                alert.setTitle("Juego terminado")
                alert.setMessage("Reiniciar el juego?")
                alert.setPositiveButton("Si") { dialog, which ->
                    activeUser?.let {
                        dbHelper.actualizarPuntaje(it.id, score)
                    }
                    val intent = intent
                    finish()
                    startActivity(intent)
                }
                alert.setNegativeButton("No") { dialog, which ->
                    activeUser?.let {
                        dbHelper.actualizarPuntaje(it.id, score)
                    }
                    Toast.makeText(this@MainActivity, "Juego Terminado =/", Toast.LENGTH_LONG).show()
                }
                alert.show()
            }

            override fun onTick(millisUntilFinished: Long) {
                timeText.text = "Tiempo: " + millisUntilFinished / 1000 + " seg"
            }

        }.start()

        // Configurar botón de cerrar sesión
        btnLogout.setOnClickListener {
            activeUser?.let {
                dbHelper.actualizarPuntaje(it.id, score)
            }
            sessionManager.logout()
            launchLoginActivity()
        }
    }

    // LÓGICA DE ALONSO: Aparición aleatoria y paralela de Pikachu y Meowth cada segundo
    fun hideImages() {
        runnable = object : Runnable {
            override fun run() {
                for (image in imageArray) {
                    image.visibility = View.INVISIBLE
                }
                val random = Random()

                // 1. Mostrar Pikachu
                indicePikachu = random.nextInt(9)
                imageArray[indicePikachu].setImageResource(R.drawable.pikachu)
                imageArray[indicePikachu].visibility = View.VISIBLE

                // 2. Mostrar Meowth en una posición diferente
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

    // LÓGICA DE ALONSO: Función unificada conectada con el onClick del XML
    fun increaseScore(view: View) {
        // Encontrar qué ImageView disparó el clic mediante su ID
        val clickedIndex = imageArray.indexOf(view as ImageView)

        if (clickedIndex == indicePikachu) {
            score += 1 // Pikachu da +1 punto
        } else if (clickedIndex == indiceMeowth) {
            score -= 2 // Meowth quita -2 puntos
        }

        // Actualizar UI
        scoreText.text = "Puntaje: $score"

        // Guardar puntaje en tiempo real en SQLite (Backend de tus compañeros)
        activeUser?.let {
            dbHelper.actualizarPuntaje(it.id, score)
        }
    }

    private fun launchLoginActivity() {
        handler.removeCallbacks(runnable)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}