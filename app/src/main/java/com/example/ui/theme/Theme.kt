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

private val DarkColorScheme =
  darkColorScheme(
    primary = DuoGreenLight,
    secondary = DuoBlue,
    tertiary = DuoOrange,
    background = Color(0xFF131F24),
    surface = Color(0xFF1F2F36),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFFECEFF1),
    onSurface = Color(0xFFECEFF1),
    error = DuoRed,
    onError = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DuoGreen,
    secondary = DuoBlue,
    tertiary = DuoOrange,
    background = DuoBackground,
    surface = DuoSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DuoGrayDark,
    onSurface = DuoGrayDark,
    error = DuoRed,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Always use LightColorScheme to ensure 100% authentic Duolingo Light Mode
  val colorScheme = LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
