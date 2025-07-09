package com.example.movil_restaurante_app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.movil_restaurante_app.screens.HomeScreen
import com.example.movil_restaurante_app.screens.PedidoScreen
//import com.example.movil_restaurante_app.screens.CheckScreen


@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("pedido") { PedidoScreen() }
  //      composable("checarPedido") { CheckScreen() }
    }
}
