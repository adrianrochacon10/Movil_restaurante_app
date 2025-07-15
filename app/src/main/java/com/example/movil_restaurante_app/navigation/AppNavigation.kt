package com.example.movil_restaurante_app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.movil_restaurante_app.screens.HomeScreen
import com.example.movil_restaurante_app.screens.PedidoScreen
import com.example.movil_restaurante_app.screens.ProductMenuScreen
//import com.example.movil_restaurante_app.screens.CheckScreen
import com.example.movil_restaurante_app.screens.SeguimientoScreen
import com.example.movil_restaurante_app.screens.CocinaScreen
import com.example.movil_restaurante_app.screens.CocinaAdminScreen
import com.example.movil_restaurante_app.screens.AgregarPlatilloScreen
import com.example.movil_restaurante_app.screens.AdminSeguimientoScreen
import com.example.movil_restaurante_app.viewmodel.ProductViewModel


sealed class Screen(val route: String) {
    object ProductMenu : Screen("product_menu")
    object Pedido : Screen("pedido")
    object Check : Screen("check")
    object Cocina : Screen("cocina")
    object Seguimiento : Screen("seguimiento/{orderId}")
}

@Composable
fun AppNavigation(productViewModel: ProductViewModel, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.ProductMenu.route) {
        composable(Screen.ProductMenu.route) { ProductMenuScreen(navController, productViewModel) }
        composable(Screen.Pedido.route) { PedidoScreen(navController, productViewModel) }
        // composable(Screen.Check.route) { CheckScreen(navController) }
        composable(Screen.Cocina.route) { CocinaScreen(navController, productViewModel) }
        composable(Screen.Seguimiento.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            SeguimientoScreen(navController, productViewModel, orderId)
        }
        // Nueva ruta para admin de cocina
        composable("cocina_admin") { CocinaAdminScreen(navController, productViewModel) }
        composable("agregar_platillo") { AgregarPlatilloScreen() }
        composable("admin_seguimiento") { AdminSeguimientoScreen(productViewModel) }
    }
}
