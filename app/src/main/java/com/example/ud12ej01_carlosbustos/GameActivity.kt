package com.example.ud12ej01_carlosbustos

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.ud12ej01_carlosbustos.databinding.ActivityGameBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Random

class GameActivity : AppCompatActivity() {
    private lateinit var nick:String
    private var cazados:Long=0
    private var anchoPantalla=0
    private var altoPantalla=0
    private var gameOver=false
    private var aleatorio: Random = Random()
    private lateinit var intentLaunch: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intentLaunch=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
            if (result.resultCode == RESULT_OK){
                val accion = result.data?.extras?.getString("accion")
                when (accion) {
                    "Salir" -> finish()
                    "Reiniciar" -> {
                        nick = result.data?.extras?.getString("nick")!!
                        cazados = 0
                        binding.tvCounter.text = "0"
                        gameOver = false
                        initCuentaAtras()
                        moverPato()
                    }
                }
            }
        }
        initPantalla()
        inicializarComponentesVisuales()
        eventos()
        moverPato()
        initCuentaAtras()
    }

    private fun inicializarComponentesVisuales() {
        val typeface = Typeface.createFromAsset(assets, "pixel.ttf")
        binding.tvCounter.typeface = typeface
        binding.tvTimer.typeface = typeface
        binding.tvNick.typeface = typeface
        val extras = intent.extras
        nick = extras!!.getString(Constantes.EXTRA_NICK)!!
        binding.tvNick.text = nick
    }

    private fun eventos() {
        binding.ivPato.setOnClickListener {
            if (!gameOver) {
                cazados++
                binding.tvCounter.text=cazados.toString()
                binding.ivPato.setImageResource(R.drawable.duck_clicked)
                Handler().postDelayed({
                    binding.ivPato.setImageResource(R.drawable.duck)
                    moverPato()
                }, 500)
            }
        }
    }

    private fun initPantalla() {
        val dm1 = resources.displayMetrics
        anchoPantalla=dm1.widthPixels
        altoPantalla=dm1.heightPixels
    }

    private fun moverPato(){
        val maximoX = anchoPantalla - binding.ivPato.width*2
        val maximoY = altoPantalla - binding.ivPato.height*2
        //Generamos un número aleatorio para la coordenada X y otro para la Y
        val randomX = aleatorio.nextInt(maximoX+1 )
        val randomY = aleatorio.nextInt(maximoY+1)
        //Utilizamos los números aleatorios para mover el pato
        binding.ivPato.x = randomX.toFloat()
        binding.ivPato.y = randomY.toFloat()
    }

    private fun initCuentaAtras(){
        object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val segundosRestantes = millisUntilFinished / 1000
                val texto = "${segundosRestantes}s"
                binding.tvTimer.text = texto
            }
            override fun onFinish() {
                binding.tvTimer.text = getString(R.string.ceroseg)
                gameOver = true

                // Guardar el puntaje del jugador en Cloud Firestore
                val db = FirebaseFirestore.getInstance()
                val nombre = binding.tvNick.text.toString()
                val puntos = cazados

                // Crea un nuevo documento con el nick del jugador como identificador
                db.collection("puntuajes")
                    .document(nick)
                    .set(Usuario(nombre, puntos))
                    .addOnSuccessListener {
                        // Iniciar la actividad de Ranking
                        val intent = Intent(this@GameActivity, RankingActivity::class.java)
                        intent.putExtra(Constantes.EXTRA_NICK, nick) // Aquí pasamos el nick como extra
                        intent.putExtra(Constantes.EXTRA_PUNTOS, cazados) //Aqui pasamos los puntos de ese usuario
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error writing document", e)
                    }
            }
        }.start()
    }



}

