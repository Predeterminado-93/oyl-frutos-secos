
package com.OYL.frutossecos.ui.screens.gestionproductos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GestionProductosScreen(
    onVolver: () -> Unit = {},
    viewModel: GestionProductosViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    val productos = estado.productos.filter {
        it.nombre.contains(estado.busqueda, ignoreCase = true) ||
                it.categoria.contains(estado.busqueda, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gestión de productos",
                style = MaterialTheme.typography.headlineSmall
            )
            TextButton(onClick = onVolver) {
                Text("Volver")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = estado.busqueda,
            onValueChange = viewModel::cambiarBusqueda,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar producto") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = viewModel::nuevoProducto,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nuevo producto")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "${productos.size} productos",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(productos, key = { it.id }) { producto ->
                ProductoCard(
                    producto = producto,
                    onEditar = { viewModel.editarProducto(producto) },
                    onCambiarEstado = { viewModel.cambiarEstado(producto) }
                )
            }
        }
    }

    if (estado.mostrarFormulario) {
        FormularioProducto(
            estado = estado,
            viewModel = viewModel
        )
    }

    estado.error?.let { mensaje ->
        AlertDialog(
            onDismissRequest = viewModel::limpiarError,
            title = { Text("Atención") },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = viewModel::limpiarError) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
private fun ProductoCard(
    producto: ProductoEntity,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Categoría: ${producto.categoria}")
            Text("Presentación: ${producto.peso}")
            Text("Precio: ${formatearPrecio(producto.precio)}")
            Text("Stock: ${producto.stock}")
            Text(
                text = if (producto.activo) "Activo" else "Desactivado",
                color = if (producto.activo) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEditar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar")
                }

                Button(
                    onClick = onCambiarEstado,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (producto.activo) "Desactivar" else "Activar")
                }
            }
        }
    }
}

@Composable
private fun FormularioProducto(
    estado: GestionProductosUiState,
    viewModel: GestionProductosViewModel
) {
    AlertDialog(
        onDismissRequest = viewModel::cerrarFormulario,
        title = {
            Text(
                if (estado.productoEditando == null) {
                    "Nuevo producto"
                } else {
                    "Editar producto"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = estado.nombre,
                    onValueChange = viewModel::cambiarNombre,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = estado.descripcion,
                    onValueChange = viewModel::cambiarDescripcion,
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.categoria,
                    onValueChange = viewModel::cambiarCategoria,
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = estado.peso,
                    onValueChange = viewModel::cambiarPeso,
                    label = { Text("Presentación") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = estado.precio,
                    onValueChange = viewModel::cambiarPrecio,
                    label = { Text("Precio en CLP") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = estado.stock,
                    onValueChange = viewModel::cambiarStock,
                    label = { Text("Stock") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = estado.fotoUrl,
                    onValueChange = viewModel::cambiarFotoUrl,
                    label = { Text("URL de fotografía (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = estado.origen,
                    onValueChange = viewModel::cambiarOrigen,
                    label = { Text("Origen") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Producto activo")
                    Switch(
                        checked = estado.activo,
                        onCheckedChange = viewModel::cambiarActivo
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = viewModel::guardarProducto,
                enabled = !estado.guardando
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator()
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::cerrarFormulario) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatearPrecio(precio: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))
        .format(precio)
}
