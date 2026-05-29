package com.example.juego

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.content.Intent
import android.os.Handler
import android.view.View
import android.widget.EditText
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        timeText= findViewById<TextView>(R.id.timeText)
        scoreText= findViewById<TextView>(R.id.scoreText)

        val imageView: ImageView = findViewById(R.id.imageView)
        imageView.setImageResource(R.drawable.pikachu);
        val imageView2: ImageView = findViewById(R.id.imageView2)
        imageView2.setImageResource(R.drawable.pikachu);
        val imageView3: ImageView = findViewById(R.id.imageView3)
        imageView3.setImageResource(R.drawable.pikachu);
        val imageView4: ImageView = findViewById(R.id.imageView4)
        imageView4.setImageResource(R.drawable.pikachu);
        val imageView5: ImageView = findViewById(R.id.imageView5)
        imageView5.setImageResource(R.drawable.pikachu);
        val imageView6: ImageView = findViewById(R.id.imageView6)
        imageView6.setImageResource(R.drawable.pikachu);
        val imageView7: ImageView = findViewById(R.id.imageView7)
        imageView7.setImageResource(R.drawable.pikachu);
        val imageView8: ImageView = findViewById(R.id.imageView8)
        imageView8.setImageResource(R.drawable.pikachu);
        val imageView9: ImageView = findViewById(R.id.imageView9)
        imageView9.setImageResource(R.drawable.pikachu);

        //ImageArray 
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
        actualizarPokemonPrincipal()

        //CountDown Timer
        object : CountDownTimer(15500,1000){
            override fun onFinish() {
                timeText.text = "Tiempo: 0 seg"
                handler.removeCallbacks(runnable)
                for (image in imageArray) {
                    image.visibility = View.INVISIBLE
                }

                val alert = AlertDialog.Builder(this@MainActivity)
                alert.setTitle("Juego terminado")
                alert.setMessage("Reiniciar el juego?")
                alert.setPositiveButton("Si") {dialog, which ->
                    val intent = intent
                    finish()
                    startActivity(intent)
                }
                alert.setNegativeButton("No") {dialog, which ->
                    Toast.makeText(this@MainActivity,"Juego Terminado =/",Toast.LENGTH_LONG).show()
                }
                alert.show()
            }

            override fun onTick(millisUntilFinished: Long) {
                timeText.text = "Tiempo: " + millisUntilFinished/1000 + " seg"
            }

        }.start()
    }

    fun hideImages() {
        runnable = object : Runnable {
            override fun run() {
                for (image in imageArray) {
                    image.visibility = View.INVISIBLE
                }
                val random = Random()
                val randomIndex = random.nextInt(9)
                imageArray[randomIndex].visibility = View.VISIBLE
                handler.postDelayed(runnable,1000)
            }
        }
        handler.post(runnable)
    }

    fun increaseScore(view: View){
        score = score + 1
        scoreText.text = "Puntaje: $score"

        getSharedPreferences("sesion", MODE_PRIVATE)
            .edit()
            .putInt("puntaje", score)
            .apply()
    }
    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        score = prefs.getInt("puntaje", score)

        if (::scoreText.isInitialized) {
            scoreText.text = "Puntaje: $score"
        }

        if (imageArray.isNotEmpty()) {
            actualizarPokemonPrincipal()
        }
    }
    private fun actualizarPokemonPrincipal() {
        val dbHelper = DBHelper(this)
        val jugadorId = getSharedPreferences("sesion", MODE_PRIVATE).getInt("jugador_id", 1)

        val imagenPokemon = dbHelper.obtenerPokemonMasCaro(jugadorId) ?: "pikachu"
        val idImagen = resources.getIdentifier(imagenPokemon, "drawable", packageName)

        for (image in imageArray) {
            image.setImageResource(idImagen)
        }
    }
    fun abrirTienda(view: View) {
        getSharedPreferences("sesion", MODE_PRIVATE)
            .edit()
            .putInt("puntaje", score)
            .putInt("jugador_id", 1)
            .apply()

        val intent = Intent(this, TiendaPokemonActivity::class.java)
        startActivity(intent)
    }
}
