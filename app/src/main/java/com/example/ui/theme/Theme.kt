package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
  primary = BrandNavy,
  onPrimary = PureWhite,
  primaryContainer = BrandBlueLight,
  onPrimaryContainer = BrandNavyDark,
  secondary = BrandRoyalBlue,
  onSecondary = PureWhite,
  secondaryContainer = BrandBlueLight,
  onSecondaryContainer = BrandNavyDark,
  tertiary = BrandEmeraldGreen,
  onTertiary = PureWhite,
  tertiaryContainer = BrandGreenLight,
  onTertiaryContainer = BrandGreenDark,
  background = Slate50,
  onBackground = Slate900,
  surface = PureWhite,
  onSurface = Slate900,
  surfaceVariant = Slate100,
  onSurfaceVariant = Slate700,
  outline = Slate300,
  outlineVariant = Slate200,
  error = ErrorRed,
  onError = PureWhite,
  errorContainer = ErrorRedLight,
  onErrorContainer = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
  primary = BrandBlueLight,
  onPrimary = BrandNavyDark,
  primaryContainer = BrandBlueContainer,
  onPrimaryContainer = PureWhite,
  secondary = BrandRoyalBlue,
  onSecondary = PureWhite,
  secondaryContainer = BrandBlueContainer,
  onSecondaryContainer = PureWhite,
  tertiary = BrandEmeraldGreen,
  onTertiary = PureWhite,
  tertiaryContainer = BrandGreenDark,
  onTertiaryContainer = BrandGreenLight,
  background = Slate900,
  onBackground = Slate50,
  surface = Slate800,
  onSurface = Slate50,
  surfaceVariant = Slate700,
  onSurfaceVariant = Slate200,
  outline = Slate500,
  outlineVariant = Slate600,
  error = ErrorRed,
  onError = PureWhite
)

@Composable
fun ShehiFootwearsTheme(
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
