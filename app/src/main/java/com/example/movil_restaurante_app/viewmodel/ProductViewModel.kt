package com.example.movil_restaurante_app.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.movil_restaurante_app.models.Producto
import com.example.movil_restaurante_app.models.ItemOrden
import com.example.movil_restaurante_app.models.Orden
import com.example.movil_restaurante_app.models.Cliente
import com.example.movil_restaurante_app.models.EstadoOrden
import java.util.UUID
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.firestore.FirebaseFirestore

class ProductViewModel : ViewModel() {

    var productos = mutableStateListOf<Producto>()
        private set

    init {
        cargarProductosDesdeFirestore()
    }

    fun cargarProductosDesdeFirestore() {
        FirebaseFirestore.getInstance().collection("platillos").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            productos.clear()
            for (doc in snapshot.documents) {
                val id = doc.id
                val nombre = doc.getString("nombre") ?: ""
                val descripcion = doc.getString("descripcion") ?: ""
                val costo = when (val raw = doc.get("costo")) {
                    is Number -> raw.toDouble()
                    is String -> raw.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                val tiempoEstimado = doc.getString("tiempoEstimado") ?: ""
                val categoria = doc.getString("categoria") ?: "Platos Principales"
                val imagenBase64 = doc.getString("imagenBase64")
                val rating = doc.getDouble("rating") ?: 0.0
                productos.add(
                    Producto(
                        id = id,
                        nombre = nombre,
                        descripcion = descripcion,
                        precio = costo,
                        tiempoEstimado = tiempoEstimado,
                        categoria = categoria,
                        imagenBase64 = imagenBase64,
                        rating = rating
                    )
                )
            }
        }
    }

    // Carrito de compras (lista mutable de ItemOrden)
    var carrito = mutableStateListOf<ItemOrden>()
        private set

    // Lista de órdenes confirmadas
    var ordenes = mutableStateListOf<Orden>()
        private set

    init {
        cargarOrdenesDesdeFirestore()
    }

    fun cargarOrdenesDesdeFirestore() {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("ordenes")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                ordenes.clear()
                for (doc in snapshot.documents) {
                    try {
                        val data = doc.data ?: continue
                        val productosList = (data["productos"] as? List<Map<String, Any?>>)?.map { itemMap ->
                            val productoMap = itemMap["producto"] as? Map<String, Any?> ?: emptyMap()
                            ItemOrden(
                                producto = Producto(
                                    id = productoMap["id"] as? String ?: "",
                                    nombre = productoMap["nombre"] as? String ?: "",
                                    descripcion = productoMap["descripcion"] as? String ?: "",
                                    precio = (productoMap["precio"] as? Number)?.toDouble() ?: 0.0,
                                    tiempoEstimado = productoMap["tiempoEstimado"] as? String ?: "",
                                    categoria = productoMap["categoria"] as? String ?: "",
                                    imagenBase64 = productoMap["imagenBase64"] as? String,
                                    rating = (productoMap["rating"] as? Number)?.toDouble() ?: 0.0
                                ),
                                cantidad = (itemMap["cantidad"] as? Number)?.toInt() ?: 0
                            )
                        } ?: listOf()
                        val clienteMap = data["cliente"] as? Map<String, Any?> ?: emptyMap()
                        val orden = Orden(
                            id = data["id"] as? String ?: "",
                            cliente = Cliente(
                                nombre = clienteMap["nombre"] as? String ?: "",
                                mesa = clienteMap["mesa"] as? String ?: "",
                                alergias = clienteMap["alergias"] as? String ?: ""
                            ),
                            mesa = data["mesa"] as? String ?: "",
                            productos = productosList,
                            estado = data["estado"] as? String ?: "RECIBIDO",
                            notas = data["notas"] as? String ?: "",
                            total = (data["total"] as? Number)?.toDouble() ?: 0.0,
                            tiempoRestante = (data["tiempoRestante"] as? Number)?.toInt() ?: 0,
                            hora = data["hora"] as? com.google.firebase.Timestamp
                        )
                        ordenes.add(orden)
                    } catch (e: Exception) {
                        // Ignora órdenes corruptas
                    }
                }
            }
    }

    // Mesa asociada al cliente (por QR)
    var mesa: String? by mutableStateOf(null)
        private set

    fun updateMesa(mesa: String) {
        this.mesa = mesa
    }

    // Agregar producto al carrito
    fun agregarAlCarrito(producto: Producto) {
        val existente = carrito.find { it.producto.id == producto.id }
        if (existente != null) {
            val index = carrito.indexOf(existente)
            carrito[index] = existente.copy(cantidad = existente.cantidad + 1)
        } else {
            carrito.add(ItemOrden(producto, 1))
        }
    }

    // Confirmar pedido: crear orden, guardar y limpiar carrito
    fun confirmarPedido(notas: String = ""): Orden? {
        if (carrito.isEmpty()) return null
        val orden = Orden(
            id = "ORD-${System.currentTimeMillis()}",
            cliente = Cliente(nombre = "", mesa = mesa ?: "", alergias = notas),
            mesa = mesa ?: "",
            productos = carrito.toList(),
            estado = EstadoOrden.RECIBIDO.name,
            notas = notas,
            total = carrito.sumOf { it.producto.precio * it.cantidad },
            tiempoRestante = 20, // Simulado
            hora = com.google.firebase.Timestamp.now()
        )
        ordenes.add(0, orden)
        carrito.clear()
        // Serializar manualmente la orden para Firestore
        val ordenMap = hashMapOf(
            "id" to orden.id,
            "cliente" to hashMapOf(
                "nombre" to orden.cliente.nombre,
                "mesa" to orden.cliente.mesa,
                "alergias" to orden.cliente.alergias
            ),
            "mesa" to orden.mesa,
            "productos" to orden.productos.map { item ->
                hashMapOf(
                    "producto" to hashMapOf(
                        "id" to item.producto.id,
                        "nombre" to item.producto.nombre,
                        "descripcion" to item.producto.descripcion,
                        "precio" to item.producto.precio,
                        "tiempoEstimado" to item.producto.tiempoEstimado,
                        "categoria" to item.producto.categoria,
                        "imagenBase64" to item.producto.imagenBase64,
                        "rating" to item.producto.rating
                    ),
                    "cantidad" to item.cantidad
                )
            },
            "estado" to orden.estado,
            "notas" to orden.notas,
            "total" to orden.total,
            "tiempoRestante" to orden.tiempoRestante,
            "hora" to orden.hora
        )
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("ordenes")
            .document(orden.id)
            .set(ordenMap)
        return orden
    }

    // Obtener cantidad total de productos en el carrito
    fun cantidadEnCarrito(): Int = carrito.sumOf { it.cantidad }

    // Elimina la simulación automática de avance de estados
    // fun simularAvanceEstados(orderId: String) { ... } // Elimina o comenta esta función

    // Cambia el estado de una orden y actualiza en Firestore, restando 5 minutos al tiempo estimado
    fun cambiarEstadoOrden(orderId: String, avance: Int) {
        val estados = listOf(
            EstadoOrden.RECIBIDO,
            EstadoOrden.PREPARANDO,
            EstadoOrden.COCINANDO,
            EstadoOrden.LISTO
        )
        val idx = ordenes.indexOfFirst { it.id == orderId }
        if (idx != -1) {
            val actual = ordenes[idx]
            val actualIndex = estados.indexOfFirst { it.name == actual.estado }
            val nuevoIndex = (actualIndex + avance).coerceIn(0, estados.size - 1)
            val nuevoEstado = estados[nuevoIndex]
            // Solo resta 5 minutos si no está en LISTO
            val nuevoTiempo = if (nuevoEstado != EstadoOrden.LISTO) {
                maxOf((actual.tiempoRestante - 5), 0)
            } else {
                0
            }
            val nuevaOrden = actual.copy(estado = nuevoEstado.name, tiempoRestante = nuevoTiempo)
            ordenes[idx] = nuevaOrden
            // Actualiza en Firestore
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("ordenes")
                .document(orderId)
                .set(nuevaOrden)
        }
    }
} 