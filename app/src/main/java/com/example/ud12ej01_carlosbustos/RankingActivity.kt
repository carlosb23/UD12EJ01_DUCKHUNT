package com.example.ud12ej01_carlosbustos

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Typeface
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.ud12ej01_carlosbustos.databinding.ActivityRankingBinding
import com.google.firebase.firestore.FirebaseFirestore


class RankingActivity: AppCompatActivity() {

    private lateinit var binding: ActivityRankingBinding
    private lateinit var nick: String
    private var userScore: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val typeface = Typeface.createFromAsset(assets, "pixel.ttf")

        // Recuperar el nombre de usuario del intent y sus puntos
        val extras = intent.extras
        if (extras != null && extras.containsKey(Constantes.EXTRA_NICK) && extras.containsKey(Constantes.EXTRA_PUNTOS)) {
            val nick = extras.getString(Constantes.EXTRA_NICK)
            val puntos = extras.getLong(Constantes.EXTRA_PUNTOS)
            if (nick != null) {
                // Inicializar la propiedad nick
                this.nick = nick
            }
            // Asignar los puntos a la propiedad userScore
            userScore = puntos
        }
        binding.txtpuntos.typeface = typeface
        binding.txtTituloRanking.typeface = typeface
        binding.btnFinalizar.typeface = typeface
        binding.btnReiniciar.typeface = typeface
        binding.txtTopScores.typeface = typeface

        // Mostrar el usuario logeado
        binding.txtTituloRanking.text = "Ranking"
        binding.txtpuntos.text = "$nick ha cazado $userScore patos"

        // Cargar el puntaje del usuario desde Firebase Firestore
        cargarPuntajeUsuario()
        // Cargar las mejores 5 puntuaciones de los usuarios desde Firebase Firestore
        cargarMejoresPuntuaciones()

        binding.btnReiniciar.setOnClickListener{
            val intent = Intent(this, GameActivity::class.java).apply {
                // Pasar el nombre de usuario como extra
                putExtra(Constantes.EXTRA_NICK, nick)
            }
            // Iniciar la actividad
            startActivity(intent)
        }

        binding.btnFinalizar.setOnClickListener {
            val intent = Intent(this@RankingActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cargarPuntajeUsuario() {
        val db = FirebaseFirestore.getInstance()

        // Obtener el puntaje del usuario actual desde Firebase Firestore
        db.collection("puntuaciones")
            .document(nick)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val usuario = document.toObject(Usuario::class.java)
                    if (usuario != null) {
                        userScore = usuario.puntos
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting document", exception)
            }
    }

    private fun cargarMejoresPuntuaciones() {
        val db = FirebaseFirestore.getInstance()

        // Ordenar y recuperar las mejores 5 puntuaciones
        db.collection("puntuaciones")
            .orderBy("puntos", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                val topScores = StringBuilder()
                for ((index, document) in documents.withIndex()) {
                    val usuario = document.toObject(Usuario::class.java)
                    topScores.append("${index + 1}º - ${usuario.nombre} - ${usuario.puntos} patos\n\n\n")
                }
                binding.txtTopScores.text = topScores.toString()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }


}