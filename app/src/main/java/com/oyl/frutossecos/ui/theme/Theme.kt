package com.OYL.frutossecos.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary         = AmberGold,
    onPrimary       = BrownDark,
    primaryContainer= BrownMedium,
    onPrimaryContainer = GoldenLight,
    secondary       = WarmTan,
    onSecondary     = BrownDark,
    secondaryContainer = SurfaceVariant,
    onSecondaryContainer = WarmTan,
    tertiary        = OliveGreen,
    background      = SurfaceDark,
    onBackground    = CreamWhite,
    surface         = SurfaceDark,
    onSurface       = CreamWhite,
    surfaceVariant  = SurfaceVariant,
    onSurfaceVariant= WarmTan,
    error           = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary         = BrownMedium,
    onPrimary       = CreamWhite,
    primaryContainer= WarmTan,
    onPrimaryContainer = BrownDark,
    secondary       = AmberGold,
    onSecondary     = BrownDark,
    secondaryContainer = GoldenLight,
    onSecondaryContainer = BrownDark,
    tertiary        = OliveGreen,
    background      = CreamWhite,
    onBackground    = BrownDark,
    surface         = CreamWhite,
    onSurface       = BrownDark,
    surfaceVariant  = WarmTan,
    onSurfaceVariant= BrownMedium,
    error           = ErrorRed
)

@Composable
fun FrutosSecosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
