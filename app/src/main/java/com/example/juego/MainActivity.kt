package com.example.juego

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
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

    // Listas para saber en tiempo real quién es quién en el tablero
    private var indicePikachu = -1
    private var indiceMeowth = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timeText = findViewById<TextView>(R.id.timeText)
        scoreText = findViewById<TextView>(R.id.scoreText)

        // Inicializar los 9 ImageViews de la cuadrícula
        val imageView: ImageView = findViewById(R.id.imageView)
        val imageView2: ImageView = findViewById(R.id.imageView2)
        val imageView3: ImageView = findViewById(R.id.imageView3)
        val imageView4: ImageView = findViewById(R.id.imageView4)
        val imageView5: ImageView = findViewById(R.id.imageView5)
        val imageView6: ImageView = findViewById(R.id.imageView6)
        val imageView7: ImageView = findViewById(R.id.imageView7)
        val imageView8: ImageView = findViewById(R.id.imageView8)
        val imageView9: ImageView = findViewById(R.id.imageView9)

        imageArray.add(imageView)
        imageArray.add(imageView2)
        imageArray.add(imageView3)
        imageArray.add(imageView4)
        imageArray.add(imageView5)
        imageArray.add(imageView6)
        imageArray.add(imageView7)
        imageArray.add(imageView8)
        imageArray.add(imageView9)

        // Configurar los eventos de clic asignando el comportamiento según el personaje visible
        for (i in 0 until imageArray.size) {
            imageArray[i].setOnClickListener {
                if (i == indicePikachu) {
                    // Si el usuario acierta a Pikachu: suma +1
                    score += 1
                } else if (i == indiceMeowth) {
                    // Si el usuario se equivoca y toca a Meowth: resta -2
                    score -= 2
                }
                scoreText.text = "Puntaje: $score"
            }
        }

        hideImages()

        // Temporizador del juego (15 segundos)
        object : CountDownTimer(15500, 1000) {
            override fun onFinish() {
                timeText.text = "Tiempo: 0 seg"
                handler.removeCallbacks(runnable)

                // Ocultar todo al terminar
                for (image in imageArray) {
                    image.visibility = View.INVISIBLE
                }

                val alert = AlertDialog.Builder(this@MainActivity)
                alert.setTitle("Juego terminado")
                alert.setMessage("Reiniciar el juego?")
                alert.setPositiveButton("Si") { dialog, which ->
                    val intent = intent
                    finish()
                    startActivity(intent)
                }
                alert.setNegativeButton("No") { dialog, which ->
                    Toast.makeText(this@MainActivity, "Juego Terminado =/", Toast.LENGTH_LONG).show()
                }
                alert.show()
            }

            override fun onTick(millisUntilFinished: Long) {
                timeText.text = "Tiempo: " + millisUntilFinished / 1000 + " seg"
            }

        }.start()
    }

    // LÓGICA PRINCIPAL: Hace aparecer a Pikachu y Meowth en paralelo en casillas aleatorias
    fun hideImages() {
        runnable = object : Runnable {
            override fun run() {
                // 1. Ocultar todas las imágenes primero
                for (image in imageArray) {
                    image.visibility = View.INVISIBLE
                }

                val random = Random()

                // 2. Elegir la posición al azar de Pikachu
                indicePikachu = random.nextInt(9)
                imageArray[indicePikachu].setImageResource(R.drawable.pikachu)
                imageArray[indicePikachu].visibility = View.VISIBLE

                // 3. Elegir una posición diferente al azar para Meowth (evitando que pisen la misma casilla)
                do {
                    indiceMeowth = random.nextInt(9)
                } while (indiceMeowth == indicePikachu)

                imageArray[indiceMeowth].setImageResource(R.drawable.meowth)
                imageArray[indiceMeowth].visibility = View.VISIBLE

                // Repetir el proceso cada 1 segundo (1000ms)
                handler.postDelayed(runnable, 1000)
            }
        }
        handler.post(runnable)
    }
}