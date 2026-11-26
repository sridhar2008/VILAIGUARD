package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val VilaiguardLightColorScheme =
  lightColorScheme(
    primary = DeepForestGreen,
    onPrimary = PaintedIvory,
    primaryContainer = SoftMintTint,
    onPrimaryContainer = DeepForestGreen,
    secondary = MossGreen,
    onSecondary = PaintedIvory,
    secondaryContainer = SoftGold,
    onSecondaryContainer = DarkEarthBrown,
    tertiary = BurntOrange,
    onTertiary = PaintedIvory,
    tertiaryContainer = SoftParchment,
    onTertiaryContainer = DarkEarthBrown,
    background = WarmCanvasCream,
    onBackground = DeepCharcoal,
    surface = PaintedIvory,
    onSurface = DeepCharcoal,
    surfaceVariant = SoftParchment,
    onSurfaceVariant = DarkEarthBrown,
    outline = SubtleBorder,
    error = Terracotta,
    onError = PaintedIvory,
    errorContainer = SoftTerracotta,
    onErrorContainer = DarkEarthBrown,
  )

private val VilaiguardDarkColorScheme =
  darkColorScheme(
    primary = SoftSage,
    onPrimary = DeepPine,
    primaryContainer = DeepForestGreen,
    onPrimaryContainer = PaintedIvory,
    secondary = HarvestGold,
    onSecondary = DeepCharcoal,
    secondaryContainer = DarkEarthBrown,
    onSecondaryContainer = SoftGold,
    tertiary = BurntOrange,
    onTertiary = PaintedIvory,
    background = DarkFieldBg,
    onBackground = DarkTextPrimary,
    surface = DarkCardSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCardElevated,
    onSurfaceVariant = DarkTextMuted,
    outline = MossGreen,
    error = BurntOrange,
    onError = PaintedIvory,
  )

val VilaiguardShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun VilaiguardTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) VilaiguardDarkColorScheme else VilaiguardLightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = VilaiguardTypography,
    shapes = VilaiguardShapes,
    content = content,
  )
}
