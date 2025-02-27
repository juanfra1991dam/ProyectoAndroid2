package com.example.proyectoandroid2.items

// Clase Item para representar los datos de cada elemento en la lista del RecyclerView
data class Item(
    val posicion: Int,
    val numero: Int,
    val nombrePiloto: String,
    val nombreEquipo: String,
    val fabrica: String,
    val nacionalidad: String,
    val puntos: Int,
    var favorito: Boolean
)