import com.example.movil_restaurante_app.models.Producto
data class ItemOrden(
    val producto: Producto = Producto(),
    val cantidad: Int = 0
)