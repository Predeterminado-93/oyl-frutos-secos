package com.OYL.frutossecos.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.OYL.frutossecos.ui.theme.FrutosSecosTheme

/**
 * Pantalla de Login — Stub inicial.
 * Tu equipo implementará esta pantalla a continuación.
 */
@Composable
fun LoginScreen(
    onLoginExitoso: (String) -> Unit,
    onRegistrarse: () -> Unit,
    onVolver: () -> Unit
) {
    FrutosSecosTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Pantalla de Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { onLoginExitoso("CLIENTE") }) {
                Text("Entrar como Cliente (demo)")
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { onLoginExitoso("ADMIN") }) {
                Text("Entrar como Admin (demo)")
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onVolver) { Text("Volver") }
        }
    }
}
