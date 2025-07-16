package com.example.movil_restaurante_app.models

// Modelo de producto para el menú
data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val tiempoEstimado: String = "",
    val categoria: String = "",
    val imagenBase64: String? = null,
    val rating: Double = 0.0
)