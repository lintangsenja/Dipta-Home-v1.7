package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
  primary = SageGreenPrimary,
  onPrimary = Color.White,
  primaryContainer = SageGreenPrimaryContainer,
  onPrimaryContainer = OnSageGreenPrimaryContainer,
  secondary = DustyRoseAccent,
  onSecondary = Color.White,
  secondaryContainer = DustyRoseAccentContainer,
  onSecondaryContainer = OnDustyRoseContainer,
  background = SoftCreamCanvas,
  onBackground = SoftTextDark,
  surface = SoftCreamSurface,
  onSurface = SoftTextDark,
  surfaceVariant = Color(0xFFF0F3EF),
  onSurfaceVariant = SoftTextMuted
)

private val DarkColorScheme = darkColorScheme(
  primary = DarkPastelPrimary,
  onPrimary = DarkPastelCanvas,
  primaryContainer = Color(0xFF384336),
  onPrimaryContainer = DarkPastelText,
  secondary = DarkPastelSecondary,
  onSecondary = DarkPastelCanvas,
  secondaryContainer = Color(0xFF4A3431),
  onSecondaryContainer = DarkPastelText,
  background = DarkPastelCanvas,
  onBackground = DarkPastelText,
  surface = DarkPastelSurface,
  onSurface = DarkPastelText,
  surfaceVariant = Color(0xFF323843),
  onSurfaceVariant = Color(0xFFA0AEC0)
)

@Composable
fun KeluargaTrackerTheme(
  darkTheme: Boolean = false, // Forced Light pastel theme for warm family aesthetic
  dynamicColor: Boolean = false, // Set to false to prioritize custom family pastel theme
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

