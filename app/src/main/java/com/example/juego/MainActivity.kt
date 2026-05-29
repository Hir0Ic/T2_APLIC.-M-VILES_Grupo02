package com.example.juego

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.juego.data.GameStateManager
import com.example.juego.data.PokemonRepository
import com.example.juego.ui.TiendaPokemonActivity
import kotlinx.coroutines.launch
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

        val btnTienda = findViewById<android.widget.Button>(R.id.btnTienda)
        btnTienda.setOnClickListener {
            val intent = Intent(this, TiendaPokemonActivity::class.java)
            startActivity(intent)
        }

        loadMostExpensivePokemon()
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
        GameStateManager.score = score
    }

    private fun loadMostExpensivePokemon() {
        val ivPurchased = findViewById<ImageView>(R.id.ivPurchasedPokemon)
        val tvPurchased = findViewById<TextView>(R.id.tvPurchasedPokemon)
        val purchasedSection = findViewById<LinearLayout>(R.id.purchasedSection)
        val sessionManager = SessionManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val userId = sessionManager.getUserId()
                if (userId == -1) return@repeatOnLifecycle
                PokemonRepository.getInstance(this@MainActivity)
                    .getMostExpensivePurchased(userId)
                    .collect { pokemon ->
                        if (pokemon != null) {
                            val resId = resources.getIdentifier(
                                pokemon.imageResName, "drawable", packageName
                            )
                            if (resId != 0) ivPurchased.setImageResource(resId)
                            tvPurchased.text = "Tu Pokémon: ${pokemon.name}"
                            purchasedSection.visibility = View.VISIBLE
                        } else {
                            purchasedSection.visibility = View.GONE
                        }
                    }
            }
        }
    }
}
