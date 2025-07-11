package com.example.movil_restaurante_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.movil_restaurante_app.viewmodel.ProductViewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreen(navController: NavHostController, viewModel: ProductViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val carrito = viewModel.carrito
    var notas by remember { mutableStateOf("") }
    val subtotal = carrito.sumOf { it.producto.precio * it.cantidad }
    val impuestos = subtotal * 0.10
    val total = subtotal + impuestos
    val cantidadArticulos = carrito.sumOf { it.cantidad }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F2))
            .padding(0.dp)
    ) {
        // TopBar y cantidad de artículos
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
        ) {
            Text("Tu Pedido", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB85C00))
            Text("$cantidadArticulos artículos", fontSize = 15.sp, color = Color(0xFFB85C00))
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Lista de productos en el carrito
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(carrito.size) { index ->
                val item = carrito[index]
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Imagen
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8E3D0)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Puedes usar un drawable local si no tienes imagenUrl
                            Icon(
                                painter = painterResource(android.R.drawable.ic_menu_gallery),
                                contentDescription = null,
                                tint = Color(0xFFB85C00),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        // Info producto
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.producto.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "$${"%.2f".format(item.producto.precio * item.cantidad)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFFFF9800)
                                )
                            }
                            Text(item.producto.descripcion, fontSize = 12.sp, color = Color(
                                0xFF000000
                            )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Botón -
                                IconButton(
                                    onClick = {
                                        if (item.cantidad > 1) {
                                            viewModel.carrito[index] = item.copy(cantidad = item.cantidad - 1)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFFF8E3D0), shape = CircleShape)
                                ) {
                                    Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                                }
                                Text(
                                    item.cantidad.toString(),
                                    modifier = Modifier.width(24.dp),
                                    fontSize = 15.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                // Botón +
                                IconButton(
                                    onClick = {
                                        viewModel.carrito[index] = item.copy(cantidad = item.cantidad + 1)
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFFFF9800), shape = CircleShape)
                                ) {
                                    Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        // Eliminar
                        IconButton(onClick = { viewModel.carrito.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFF9800))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Notas/alergias
        OutlinedTextField(
            value = notas,
            onValueChange = { notas = it },
            label = { Text("Alergias, preferencias de cocción, etc.") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = false,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(12.dp))
        // Resumen del pedido
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal")
                    Text("$${"%.2f".format(subtotal)}")
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Impuestos (10%)")
                    Text("$${"%.2f".format(impuestos)}")
                }
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", fontWeight = FontWeight.Bold)
                    Text("$${"%.2f".format(total)}", fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Botón de confirmar pedido
        Button(
            onClick = {
                val orden = viewModel.confirmarPedido(notas)
                orden?.let {
                    navController.navigate("seguimiento/${it.id}")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB85C00)),
            enabled = carrito.isNotEmpty()
        ) {
            Text("Confirmar Pedido • $${"%.2f".format(total)}", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
