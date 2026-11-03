package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = DarkTealPrimary,
  onPrimary = Color.Black,
  primaryContainer = DarkTealContainer,
  onPrimaryContainer = Color.White,
  secondary = AmberSecondary,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF4E342E),
  onSecondaryContainer = Color.White,
  background = DarkBackground,
  onBackground = Color(0xFFECEFF1),
  surface = DarkSurface,
  onSurface = Color(0xFFECEFF1),
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = Color(0xFFB0BEC5),
  error = RedExpense,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = TealPrimary,
  onPrimary = Color.White,
  primaryContainer = TealContainer,
  onPrimaryContainer = OnTealContainer,
  secondary = AmberSecondary,
  onSecondary = Color.White,
  secondaryContainer = AmberContainer,
  onSecondaryContainer = Color(0xFFE65100),
  background = SlateBackground,
  onBackground = TextPrimary,
  surface = SlateSurface,
  onSurface = TextPrimary,
  surfaceVariant = SlateSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  error = RedExpense,
  onError = Color.White
)

@Composable
fun SmartMessTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
