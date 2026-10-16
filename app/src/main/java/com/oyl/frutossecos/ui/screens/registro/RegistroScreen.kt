package com.OYL.frutossecos.ui.screens.registro

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.OYL.frutossecos.data.local.entities.UsuarioEntity
import com.OYL.frutossecos.ui.theme.*

/**
 * Pantalla de Registro de Cliente
 *
 * Rol: Exclusivamente Cliente.
 * Formulario para registrar al cliente con nombre, alias, contraseña y datos de contacto.
 * Valida campos obligatorios y almacena en SQLite (Room) la información local para
 * identificar su perfil y permitir múltiples clientes sin mezclar datos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    onRegistroExitoso: (UsuarioEntity) -> Unit,
    onIrALogin: () -> Unit,
    onVolver: () -> Unit,
    viewModel: RegistroViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registro de Cliente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = BrownDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamWhite
                )
            )
        },
        containerColor = CreamWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Cabecera e Identidad ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(AmberGold, BrownMedium)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = CreamWhite,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Crea tu Cuenta de Cliente",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BrownDark
            )

            Text(
                text = "Registra tus datos para realizar pedidos de frutos secos y semillas",
                style = MaterialTheme.typography.bodyMedium,
                color = BrownMedium.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(20.dp))

            // ── Mensaje de Error (si existe) ───────────────────────────────
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ErrorRed.copy(alpha = 0.12f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRed,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.limpiarError() },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = ErrorRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ── SECCIÓN 1: Credenciales de Cuenta ──────────────────────────
            Text(
                text = "Datos de Acceso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrownDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Nombre Completo
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = { viewModel.onNombreChanged(it) },
                label = { Text("Nombre completo *") },
                placeholder = { Text("Ej: Juan Pérez") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = BrownMedium)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Alias
            OutlinedTextField(
                value = uiState.alias,
                onValueChange = { viewModel.onAliasChanged(it) },
                label = { Text("Alias o Nombre de usuario *") },
                placeholder = { Text("Ej: juanito (mínimo 3 caracteres)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = BrownMedium)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Contraseña
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = { Text("Contraseña *") },
                placeholder = { Text("Mínimo 4 caracteres") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BrownMedium)
                },
                trailingIcon = {
                    IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (uiState.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = BrownMedium
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Confirmar Contraseña
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                label = { Text("Confirmar contraseña *") },
                placeholder = { Text("Repite tu contraseña") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = BrownMedium)
                },
                trailingIcon = {
                    IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                        Icon(
                            imageVector = if (uiState.confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = BrownMedium
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (uiState.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // ── SECCIÓN 2: Datos de Contacto ───────────────────────────────
            Text(
                text = "Datos de Contacto y Entrega",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrownDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Correo
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChanged(it) },
                label = { Text("Correo electrónico *") },
                placeholder = { Text("ejemplo@correo.cl") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = BrownMedium)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Teléfono
            OutlinedTextField(
                value = uiState.telefono,
                onValueChange = { viewModel.onTelefonoChanged(it) },
                label = { Text("Teléfono de contacto *") },
                placeholder = { Text("+56 9 1234 5678") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = BrownMedium)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Dirección
            OutlinedTextField(
                value = uiState.direccion,
                onValueChange = { viewModel.onDireccionChanged(it) },
                label = { Text("Dirección de entrega *") },
                placeholder = { Text("Calle, número, comuna o ciudad") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = BrownMedium)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.registrar { usuario ->
                            onRegistroExitoso(usuario)
                        }
                    }
                ),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // ── Botón: Registrar Cliente ───────────────────────────────────
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.registrar { usuario ->
                        onRegistroExitoso(usuario)
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberGold,
                    contentColor = BrownDark,
                    disabledContainerColor = WarmTan.copy(alpha = 0.5f)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = BrownDark,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Crear Cuenta de Cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Enlace: Ya tengo cuenta ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrownMedium
                )
                TextButton(onClick = onIrALogin) {
                    Text(
                        text = "Inicia sesión aquí",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = BrownDark,
    unfocusedTextColor = BrownDark,
    focusedBorderColor = AmberGold,
    unfocusedBorderColor = WarmTan,
    focusedLabelColor = BrownMedium,
    unfocusedLabelColor = BrownMedium.copy(alpha = 0.7f),
    cursorColor = AmberGold,
    focusedPlaceholderColor = BrownMedium.copy(alpha = 0.5f),
    unfocusedPlaceholderColor = BrownMedium.copy(alpha = 0.5f)
)
