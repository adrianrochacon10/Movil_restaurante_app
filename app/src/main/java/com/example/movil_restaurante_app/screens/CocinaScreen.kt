package com.example.movil_restaurante_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocinaScreen(navController: NavHostController, viewModel: ProductViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
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
            Text("Panel de Cocina", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB85C00), modifier = Modifier.padding(8.dp))
            val ordenes = viewModel.ordenes.filter { it.estado != EstadoOrden.ENTREGADO.name && it.estado != EstadoOrden.RECHAZADO.name }
            val estados = listOf(
                EstadoOrden.RECIBIDO,
                EstadoOrden.PREPARANDO,
                EstadoOrden.COCINANDO,
                EstadoOrden.LISTO
            )
            estados.forEach { estado ->
                val ordenesPorEstado = ordenes.filter { it.estado == estado.name }
                if (ordenesPorEstado.isNotEmpty()) {
                    Text(
                        text = when (estado) {
                            EstadoOrden.RECIBIDO -> "Recibido"
                            EstadoOrden.PREPARANDO -> "Preparando"
                            EstadoOrden.COCINANDO -> "Cocinando"
                            EstadoOrden.LISTO -> "Listo"
                            else -> ""
                        },
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB85C00),
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 220.dp)
                    ) {
                        items(ordenesPorEstado.size) { idx ->
                            val orden = ordenesPorEstado[idx]
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (estado) {
                                        EstadoOrden.RECIBIDO -> Color(0xFFE3F2FD)
                                        EstadoOrden.PREPARANDO -> Color(0xFFFFF9C4)
                                        EstadoOrden.COCINANDO -> Color(0xFFFFE0B2)
                                        EstadoOrden.LISTO -> Color(0xFFC8E6C9)
                                        else -> Color.White
                                    }
                                ),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(orden.id, fontWeight = FontWeight.Bold)
                                            Text("Mesa: ${orden.mesa}", fontSize = 13.sp, color = Color(0xFFB85C00))
                                        }
                                        orden.hora?.let {
                                            val sdf = java.text.SimpleDateFormat("HH:mm")
                                            val horaStr = sdf.format(it.toDate())
                                            Text(horaStr, fontSize = 13.sp)
                                        }
                                    }
                                    Text("Productos: " + orden.productos.joinToString { it.producto.nombre + " x${it.cantidad}" }, fontSize = 13.sp)
                                    if (!orden.notas.isNullOrBlank()) {
                                        Text("Notas: ${orden.notas}", fontSize = 12.sp, color = Color(0xFFB85C00))
                                    }
                                    Text("Total: $${"%.2f".format(orden.total)}", fontWeight = FontWeight.Bold, color = Color(0xFFB85C00))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        if (estado != EstadoOrden.RECIBIDO) {
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.cambiarEstadoOrden(orden.id, -1)
                                                },
                                                modifier = Modifier.padding(end = 8.dp)
                                            ) { Text("Regresar") }
                                        }
                                        if (estado != EstadoOrden.LISTO) {
                                            Button(
                                                onClick = {
                                                    viewModel.cambiarEstadoOrden(orden.id, 1)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB85C00))
                                            ) { Text("Avanzar", color = Color.White) }
                                        }
                                    }
                                    Button(
                                        onClick = { navController.navigate("seguimiento/${orden.id}") },
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) { Text("Ver Seguimiento") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 