package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CryptoColorScheme = darkColorScheme(
  primary = CryptoGreen,
  onPrimary = Color.Black,
  primaryContainer = CryptoGreenSurface,
  onPrimaryContainer = CryptoGreenGlow,
  secondary = CryptoCyan,
  onSecondary = Color.Black,
  secondaryContainer = CryptoCardDark,
  onSecondaryContainer = CryptoCyan,
  tertiary = CryptoYellow,
  onTertiary = Color.Black,
  background = CryptoBgDark,
  onBackground = TextPrimary,
  surface = CryptoSurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = CryptoCardDark,
  onSurfaceVariant = TextSecondary,
  outline = CryptoCardBorder,
  outlineVariant = GridDivider,
  error = CryptoRed,
  onError = Color.White,
  errorContainer = CryptoRedSurface,
  onErrorContainer = CryptoRedGlow
)

@Composable
fun BTCProTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = CryptoColorScheme,
    typography = Typography,
    content = content
  )
}
