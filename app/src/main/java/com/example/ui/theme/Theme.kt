package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SalonColorScheme = lightColorScheme(
  primary = GoldAccent,
  onPrimary = Color.White,
  primaryContainer = GoldDust,
  onPrimaryContainer = ObsidianBlack,
  secondary = SoftCharcoal,
  onSecondary = Color.White,
  background = LuxuryBackground,
  onBackground = ObsidianBlack,
  surface = Color.White,
  onSurface = ObsidianBlack,
  surfaceVariant = OffWhiteSurface,
  onSurfaceVariant = SoftCharcoal,
  outline = GoldAccent,
  error = ErrorRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Always light theme for brand consistency
  dynamicColor: Boolean = false, // Locked to our brand palette
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = SalonColorScheme,
    typography = Typography,
    content = content
  )
}

