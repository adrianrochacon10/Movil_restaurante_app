package com.example.movil_restaurante_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.movil_restaurante_app.viewmodel.ProductViewModel
import com.example.movil_restaurante_app.models.EstadoOrden
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.example.movil_restaurante_app.models.Orden
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.remember

@Composable
fun SeguimientoScreen(
    navController: NavHostController,
    viewModel: ProductViewModel,
    orderId: String
) {
    var orden by remember { mutableStateOf<Orden?>(null) }
    var loading by remember { mutableStateOf(true) }
    val localOrden = viewModel.ordenes.find { it.id == orderId }

    LaunchedEffect(orderId, localOrden) {
        if (localOrden != null) {
            orden = localOrden
            loading = false
        } else {
            loading = true
            FirebaseFirestore.getInstance().collection("ordenes").document(orderId).get()
                .addOnSuccessListener { doc ->
                    val remoteOrden = doc.toObject(Orden::class.java)
                    orden = remoteOrden
                    loading = false
                }
                .addOnFailureListener {
                    orden = null
                    loading = false
                }
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFF9800))
        }
        return
    }
    orden?.let { o ->
        val estados = listOf(
            EstadoOrden.RECIBIDO,
            EstadoOrden.PREPARANDO,
            EstadoOrden.COCINANDO,
            EstadoOrden.LISTO
        )
        val estadoActual = estados.indexOfFirst { it.name == o.estado }.coerceAtLeast(0)
        val colorActivo = Color(0xFFFF9800)
        val colorInactivo = Color(0xFFBDBDBD)

        val view = LocalView.current
        LaunchedEffect(Unit) {
            val activity = view.context as? Activity
            activity?.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = Color(0xFFFFF8F2)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text("Seguimiento de Pedido", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colorActivo)
                Text("Mesa: ${o.mesa.ifBlank { "Sin asignar" }}", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colorActivo)
                Text(o.id, fontSize = 15.sp, color = colorActivo)
                Spacer(modifier = Modifier.height(12.dp))
                // Tiempo estimado
                val mostrarTiempo = o.estado != EstadoOrden.LISTO.name && o.estado != EstadoOrden.ENTREGADO.name
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = colorActivo, modifier = Modifier.size(32.dp))
                        Text("Tiempo Estimado", color = colorActivo)
                        Text(
                            if (mostrarTiempo) "${o.tiempoRestante} min" else "0 min",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = colorActivo
                        )
                        Text(
                            if (mostrarTiempo) "Tu pedido estará listo pronto" else "¡Tu pedido está listo para entregar!",
                            color = colorInactivo,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Barra de progreso de estados
                Column(Modifier.fillMaxWidth()) {
                    estados.forEachIndexed { idx, estado ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isActive = idx <= estadoActual
                            val icon = when (estado) {
                                EstadoOrden.RECIBIDO -> Icons.Filled.CheckCircle
                                EstadoOrden.PREPARANDO -> Icons.Filled.Info
                                EstadoOrden.COCINANDO -> Icons.Filled.List
                                EstadoOrden.LISTO -> Icons.Filled.CheckCircle
                                else -> Icons.Filled.CheckCircle
                            }
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (isActive) colorActivo else colorInactivo,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    when (estado) {
                                        EstadoOrden.RECIBIDO -> "Pedido Recibido"
                                        EstadoOrden.PREPARANDO -> "En Preparación"
                                        EstadoOrden.COCINANDO -> "En Cocción"
                                        EstadoOrden.LISTO -> "Listo para Entregar"
                                        else -> ""
                                    },
                                    color = if (isActive) colorActivo else colorInactivo,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                                if (estado.name == o.estado && mostrarTiempo) {
                                    Text(
                                        "Aproximadamente ${o.tiempoRestante} min restantes",
                                        color = colorActivo,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        if (idx < estados.size - 1) {
                            Box(
                                Modifier
                                    .height(16.dp)
                                    .width(2.dp)
                                    .background(if (idx < estadoActual) colorActivo else colorInactivo)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Detalles del pedido
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Detalles del Pedido", fontWeight = FontWeight.Bold, color = colorActivo)
                        o.productos.forEach {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${it.producto.nombre} x${it.cantidad}")
                                Text("$${"%.2f".format(it.producto.precio * it.cantidad)}")
                            }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total (inc. impuestos)", fontWeight = FontWeight.Bold)
                            Text("$${"%.2f".format(o.total)}", fontWeight = FontWeight.Bold, color = colorActivo)
                        }
                    }
                }
            }
        }
    }
    // Solo mostrar el mensaje si la orden realmente no existe
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Puedes comentar o eliminar la siguiente línea si no quieres mostrar nada:
        // Text("Pedido no encontrado o aún no se ha registrado", color = Color.Red)
    }
    return
} 