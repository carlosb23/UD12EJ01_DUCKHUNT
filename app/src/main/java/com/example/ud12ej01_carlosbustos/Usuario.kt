package com.example.ud12ej01_carlosbustos

class Usuario {

    var nombre: String = ""
    var puntos: Long = 0

    constructor() // Constructor sin argumentos requerido por Firebase Firestore

    constructor(nombre: String, puntos: Long) {
        this.nombre = nombre
        this.puntos = puntos
    }
}