package com.example.movil_restaurante_app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
// import coil.compose.rememberAsyncImagePainter // Descomenta cuando la dependencia esté lista
import com.example.movil_restaurante_app.viewmodel.ProductViewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.RestaurantMenu
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.wrapContentHeight
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.RestaurantMenu
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import android.content.Context
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductMenuScreen(navController: NavHostController, viewModel: ProductViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val productos = viewModel.productos
    val cantidadCarrito = viewModel.cantidadEnCarrito()
    // Cambia la lista de categorías en la pantalla principal:
    val categorias = listOf("Plato Fuerte", "Comida USA", "Bebidas", "Postres")
    var categoriaSeleccionada by remember { mutableStateOf(categorias[0]) }
    val productosFiltrados = productos.filter { it.categoria == categoriaSeleccionada }

    // Llama a cargar productos si la lista está vacía (opcional, por seguridad)
    LaunchedEffect(Unit) {
        if (productos.isEmpty()) viewModel.cargarProductosDesdeFirestore()
    }

    // Easter Egg: triple tap
    var tapCount by remember { mutableStateOf(0) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var tapJob by remember { mutableStateOf<Job?>(null) }

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
        containerColor = Color(0xFFF6EDE0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // TopBar con Easter Egg
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Sabor Abierto",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB85C00),
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    tapCount++
                                    tapJob?.cancel()
                                    tapJob = coroutineScope.launch {
                                        delay(700)
                                        tapCount = 0
                                    }
                                    if (tapCount == 3) {
                                        tapCount = 0
                                        showPinDialog = true
                                    }
                                }
                            }
                    )
                    Text("Cocina en vivo", fontSize = 15.sp, color = Color(0xFFB85C00))
                }
                Box(modifier = Modifier.padding(end = 4.dp)) {
                    Row {
                        IconButton(onClick = { navController.navigate("admin_seguimiento") }) {
                            Icon(Icons.Filled.List, contentDescription = "Seguimiento", tint = Color(0xFFB85C00))
                        }
                        BadgedBox(badge = {
                            if (cantidadCarrito > 0) {
                                Badge(containerColor = Color(0xFFFF9800)) { Text(cantidadCarrito.toString()) }
                            }
                        }) {
                            IconButton(onClick = { navController.navigate("pedido") }) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito", tint = Color(0xFFB85C00))
                            }
                        }
                    }
                }
            }
            // aqui son los Tabs de categorías
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categorias.forEach { categoria ->
                    val selected = categoria == categoriaSeleccionada
                    Button(
                        onClick = { categoriaSeleccionada = categoria },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) Color(0xFFFF9800) else Color.White,
                            contentColor = if (selected) Color.White else Color(0xFFB85C00)
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(categoria)
                    }
                }
            }
            // Lista de productos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productosFiltrados.size) { index ->
                    val producto = productosFiltrados[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // aqui es el diseño de la imgaen
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8E3D0)),
                                contentAlignment = Alignment.Center
                            ) {
                                val bitmap = producto.imagenBase64?.let { base64ToBitmap(it) }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(android.R.drawable.ic_menu_gallery),
                                        contentDescription = null,
                                        tint = Color(0xFFB85C00),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            // aqui es toda la informacion del producto
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(producto.nombre, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF222222))
                                Text(producto.descripcion, fontSize = 13.sp, color = Color(0xFF666666), maxLines = 2)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                                    Text("${producto.rating}", fontSize = 13.sp, color = Color(0xFFB85C00), modifier = Modifier.padding(start = 2.dp, end = 8.dp))
                                    Text(producto.tiempoEstimado, fontSize = 13.sp, color = Color(0xFFB85C00))
                                }
                            }
                            // aqui se agrega el Precio y botón agregar
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "$${"%.2f".format(producto.precio)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFFFF9800)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.agregarAlCarrito(producto) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("+ Agregar", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        // Diálogo de PIN para cocineros
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false; pinInput = ""; pinError = false },
                title = { Text("Acceso Cocineros") },
                text = {
                    Column {
                        Text("Ingresa el PIN de cocinero")
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { pinInput = it; pinError = false },
                            label = { Text("PIN") },
                            isError = pinError,
                            singleLine = true
                        )
                        if (pinError) Text("PIN incorrecto", color = Color.Red, fontSize = 13.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (pinInput == "1234") {
                            showPinDialog = false
                            pinInput = ""
                            pinError = false
                            navController.navigate("cocina_admin")
                        } else {
                            pinError = true
                        }
                    }) { Text("Entrar") }
                },
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false; pinInput = ""; pinError = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

// Placeholder para la pantalla de administración de cocina
@Composable
fun CocinaAdminScreen(navController: NavHostController = androidx.navigation.compose.rememberNavController(), viewModel: ProductViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = Color(0xFF181828)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Icono de cubiertos
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color.White, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.RestaurantMenu,
                        contentDescription = "Cubiertos",
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    "Cocinero",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Gestión de platillos y cocina",
                    fontSize = 15.sp,
                    color = Color(0xFFCCCCCC),
                    modifier = Modifier.padding(top = 2.dp, bottom = 32.dp)
                )
                Button(
                    onClick = { navController.navigate("agregar_platillo") },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Agregar Platillo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF181828))
                }
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = { navController.navigate("cocina") },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
                ) {
                    Text("Cocina", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// Pantalla para agregar platillo con campos y botón de imagen
@Composable
fun AgregarPlatilloScreen() {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    // Selector de categoría
    val categorias = listOf("Plato Fuerte", "Comida USA", "Bebidas", "Postres")
    var categoria by remember { mutableStateOf(categorias[0]) }
    var expanded by remember { mutableStateOf(false) }

    // Launcher para seleccionar imagen
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imagenUri = uri
        }
    }

    fun subirPlatillo() {
        if (nombre.isBlank() || descripcion.isBlank() || costo.isBlank() || imagenUri == null) {
            Toast.makeText(context, "Completa todos los campos y selecciona una imagen", Toast.LENGTH_SHORT).show()
            return
        }
        isUploading = true
        // Convertir la imagen a Base64
        val base64 = imagenUri?.let { uriToBase64(context, it) }
        val costoDouble = costo.toDoubleOrNull() ?: 0.0
        val platillo = hashMapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "costo" to costoDouble,
            "imagenBase64" to base64,
            "categoria" to categoria
        )
        FirebaseFirestore.getInstance().collection("platillos").add(platillo)
            .addOnSuccessListener {
                isUploading = false
                showSuccess = true
                nombre = ""
                descripcion = ""
                costo = ""

                imagenUri = null
            }
            .addOnFailureListener { e ->
                isUploading = false
                Toast.makeText(context, "Error al guardar en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = Color(0xFFF6EDE0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF8E3D0))
                        .clickable(enabled = !isUploading) { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imagenUri != null) {
                        androidx.compose.foundation.Image(
                            painter = rememberAsyncImagePainter(imagenUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.RestaurantMenu,
                            contentDescription = "Agregar imagen",
                            tint = Color(0xFFB85C00),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Card(
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre del platillo") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUploading
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Selector de categoría
                        Box {
                            OutlinedTextField(
                                value = categoria,
                                onValueChange = {},
                                label = { Text("Categoría") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = true },
                                enabled = false,
                                readOnly = true
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                categorias.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            categoria = cat
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUploading
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = costo,
                            onValueChange = { costo = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Costo") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isUploading
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { if (!isUploading) subirPlatillo() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isUploading
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Subiendo...", fontWeight = FontWeight.Bold)
                            } else {
                                Text("Agregar Platillo", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                if (showSuccess) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¡Platillo agregado exitosamente!", color = Color(0xFF43A047), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Placeholder para seguimiento admin (puedes personalizarlo luego)
@Composable
fun AdminSeguimientoScreen(viewModel: ProductViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    SeguimientoScreen(
        navController = androidx.navigation.compose.rememberNavController(),
        viewModel = viewModel,
        orderId = viewModel.ordenes.firstOrNull()?.id ?: ""
    )
}

fun uriToBase64(context: Context, uri: Uri): String? {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun base64ToBitmap(base64Str: String): Bitmap {
    val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
} 