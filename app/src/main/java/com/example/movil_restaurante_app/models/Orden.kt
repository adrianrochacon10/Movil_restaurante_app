package com.example.movil_restaurante_app.models

import java.util.Date
import com.google.firebase.Timestamp

// Estados posibles de una orden
enum class EstadoOrden {
    RECIBIDO, PREPARANDO, COCINANDO, LISTO, ENTREGADO, RECHAZADO
}

// Modelo de orden

data class Orden(
    val id: String = "",
    val cliente: Cliente = Cliente(),
    val mesa: String = "",
    val productos: List<ItemOrden> = listOf(),
    val estado: String = "RECIBIDO", // Usa String para Firestore
    val notas: String = "",
    val total: Double = 0.0,
    val tiempoRestante: Int = 0,
    val hora: Timestamp? = null // Usa Timestamp para Firestore
)

// Relación producto-cantidad

data class ItemOrden(
    val producto: Producto,
    val cantidad: Int
) 