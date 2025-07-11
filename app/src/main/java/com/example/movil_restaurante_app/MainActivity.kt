package com.example.movil_restaurante_app
import com.example.movil_restaurante_app.navigation.AppNavigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.movil_restaurante_app.ui.theme.Movil_restaurante_appTheme
import androidx.activity.viewModels
import com.example.movil_restaurante_app.viewmodel.ProductViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val productViewModel: ProductViewModel by viewModels()
        setContent {
            Movil_restaurante_appTheme {
                AppNavigation(productViewModel)
            }
        }
    }
}