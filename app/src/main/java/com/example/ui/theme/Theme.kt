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
    primary = ForestGreen,
    onPrimary = PureWhite,
    primaryContainer = SoftMintTint,
    onPrimaryContainer = DeepPine,
    secondary = FreshLeafGreen,
    onSecondary = PureWhite,
    secondaryContainer = SoftGold,
    onSecondaryContainer = DeepAmber,
    tertiary = EarthBrown,
    onTertiary = PureWhite,
    tertiaryContainer = SoftParchment,
    onTertiaryContainer = EarthBrown,
    background = WarmCream,
    onBackground = DarkCharcoal,
    surface = PureWhite,
    onSurface = DarkCharcoal,
    surfaceVariant = SoftParchment,
    onSurfaceVariant = MutedSlate,
    outline = SubtleBorder,
    error = TerracottaAlert,
    onError = PureWhite,
    errorContainer = SoftTerracotta,
    onErrorContainer = TerracottaAlert,
  )

private val VilaiguardDarkColorScheme =
  darkColorScheme(
    primary = SproutGreen,
    onPrimary = DeepPine,
    primaryContainer = ForestGreen,
    onPrimaryContainer = SoftMintTint,
    secondary = GoldenHarvest,
    onSecondary = DarkCharcoal,
    secondaryContainer = DeepAmber,
    onSecondaryContainer = SoftGold,
    tertiary = WarmSoil,
    onTertiary = PureWhite,
    background = DarkFieldBg,
    onBackground = DarkTextPrimary,
    surface = DarkCardSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCardElevated,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkCardElevated,
    error = TerracottaAlert,
    onError = PureWhite,
  )

val VilaiguardShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
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
