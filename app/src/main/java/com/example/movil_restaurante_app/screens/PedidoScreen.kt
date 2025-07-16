package com.example.movil_restaurante_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.app.Activity
import android.os.Build
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreen(navController: NavHostController, viewModel: ProductViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val carrito = viewModel.carrito
    var notas by remember { mutableStateOf("") }
    val subtotal = carrito.sumOf { it.producto.precio * it.cantidad }
    val impuestos = subtotal * 0.10
    val total = subtotal + impuestos
    val cantidadArticulos = carrito.sumOf { it.cantidad }

    // Respeta los insets del sistema pero usa el color predeterminado del sistema para la barra de navegación
    val view = LocalView.current
    LaunchedEffect(Unit) {
        val activity = view.context as? Activity
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = Color(0xFFFFF8F2)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // TopBar igual a la imagen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 24.dp, start = 8.dp, end = 0.dp, bottom = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar", tint = Color(0xFFB85C00))
                }
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        "Tu Pedido",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB85C00),
                        modifier = Modifier.padding(bottom = 0.dp)
                    )
                    Text(
                        "$cantidadArticulos artículos",
                        fontSize = 14.sp,
                        color = Color(0xFFB85C00),
                        modifier = Modifier.padding(top = 0.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Lista de productos en el carrito
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(carrito.size) { index ->
                     val item = carrito[index]
                    Box {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Imagen
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF8E3D0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(android.R.drawable.ic_menu_gallery),
                                        contentDescription = null,
                                        tint = Color(0xFFB85C00),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                // Contenido derecho
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            item.producto.nombre.ifBlank { "Producto" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF222222),
                                            modifier = Modifier.padding(end = 32.dp)
                                        )
                                        Text(
                                            item.producto.descripcion.ifBlank { "Sin descripción" },
                                            fontSize = 12.sp,
                                            color = Color(0xFF666666),
                                            maxLines = 2,
                                            modifier = Modifier.padding(top = 2.dp, bottom = 2.dp, end = 32.dp)
                                        )
                                    }
                                    // SOLO botones y costo centrados abajo
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                (if (item.cantidad > 0) item.cantidad else 1).toString(),
                                                modifier = Modifier.width(24.dp),
                                                fontSize = 15.sp,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
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
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            "$${"%.2f".format(item.producto.precio * item.cantidad)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFFFF9800),
                                            modifier = Modifier.padding(end = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                        // Ícono eliminar en la esquina superior derecha
                        IconButton(
                            onClick = { viewModel.carrito.removeAt(index) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset { IntOffset((-6).dp.roundToPx(), 10.dp.roundToPx()) }
                                .size(36.dp)
                                .background(Color.White, shape = CircleShape)
                                .zIndex(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFF9800), modifier = Modifier.size(28.dp))
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
}
