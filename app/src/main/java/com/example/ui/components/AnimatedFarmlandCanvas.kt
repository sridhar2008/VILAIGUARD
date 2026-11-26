package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.DeepPine
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.FreshLeafGreen
import com.example.ui.theme.GoldenHarvest
import com.example.ui.theme.SproutGreen
import kotlin.math.PI
import kotlin.math.sin

/**
 * Animated 3D-perspective Indian farmland environment combining the photorealistic
 * sunlit paddy backdrop with real-time Compose Canvas layers:
 * - Warm golden morning sky & distant misty hills
 * - Drifting clouds
 * - Perspective paddy field terraces & shimmering irrigation stream
 * - Wind-swaying crop stalks in 3D depth rows
 * - A farmer walking smoothly along the earthen bund
 * - Subtle floating golden harvest dust particles
 */
@Composable
fun AnimatedFarmlandHeroBackground(modifier: Modifier = Modifier) {
  val transition = rememberInfiniteTransition(label = "farmland_env")
  val cloudPhase by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Restart),
    label = "cloud_phase",
  )
  val windPhase by transition.animateFloat(
    initialValue = 0f,
    targetValue = (2f * PI).toFloat(),
    animationSpec = infiniteRepeatable(tween(3800, easing = LinearEasing), RepeatMode.Restart),
    label = "wind_phase",
  )
  val walkPhase by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Restart),
    label = "farmer_walk",
  )
  val dustPhase by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
    label = "dust_particles",
  )

  Box(modifier = modifier) {
    // High-resolution farmland backdrop
    Image(
      painter = painterResource(id = R.drawable.img_hero_farmland_1795669314949),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )

    // Atmospheric morning-gold + deep forest scrim for crisp typography readability
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors =
                listOf(
                  DeepPine.copy(alpha = 0.72f),
                  ForestGreen.copy(alpha = 0.45f),
                  DeepPine.copy(alpha = 0.84f),
                )
            )
          )
    )

    // Live animated 3D-perspective farmland overlay
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      // 1. Warm sunrise glow
      drawCircle(
        brush =
          Brush.radialGradient(
            colors = listOf(GoldenHarvest.copy(alpha = 0.34f), Color.Transparent),
            center = Offset(w * 0.18f, h * 0.22f),
            radius = w * 0.48f,
          ),
        radius = w * 0.48f,
        center = Offset(w * 0.18f, h * 0.22f),
      )

      // 2. Drifting translucent clouds
      for (c in 0..2) {
        val baseX = ((cloudPhase + c * 0.34f) % 1.25f) * (w * 1.2f) - w * 0.15f
        val baseY = h * (0.12f + c * 0.06f)
        drawOval(
          color = Color.White.copy(alpha = 0.12f),
          topLeft = Offset(baseX, baseY),
          size = androidx.compose.ui.geometry.Size(w * 0.28f, h * 0.055f),
        )
        drawOval(
          color = Color.White.copy(alpha = 0.09f),
          topLeft = Offset(baseX + w * 0.08f, baseY - h * 0.015f),
          size = androidx.compose.ui.geometry.Size(w * 0.18f, h * 0.045f),
        )
      }

      // 3. Distant village hills silhouette
      val hillPath =
        Path().apply {
          moveTo(0f, h * 0.50f)
          quadraticTo(w * 0.22f, h * 0.40f, w * 0.48f, h * 0.48f)
          quadraticTo(w * 0.74f, h * 0.39f, w, h * 0.47f)
          lineTo(w, h * 0.56f)
          lineTo(0f, h * 0.56f)
          close()
        }
      drawPath(hillPath, color = ForestGreen.copy(alpha = 0.36f))

      // 4. Shimmering irrigation canal perspective curve
      val canalPath =
        Path().apply {
          moveTo(w * 0.56f, h * 0.52f)
          cubicTo(w * 0.42f, h * 0.68f, w * 0.78f, h * 0.80f, w * 0.64f, h)
        }
      drawPath(
        path = canalPath,
        brush =
          Brush.verticalGradient(
            colors = listOf(Color(0xFF9CE3F0).copy(alpha = 0.26f), GoldenHarvest.copy(alpha = 0.30f))
          ),
        style = Stroke(width = 14f, cap = StrokeCap.Round),
      )

      // 5. Perspective swaying paddy crop blades (layered depth rows)
      val rows = 3
      for (row in 0 until rows) {
        val depth = (row + 1) / rows.toFloat()
        val yBase = h * (0.68f + row * 0.105f)
        val stalkHeight = h * (0.045f + depth * 0.032f)
        val count = 18
        for (i in 0 until count) {
          val x = (w / (count - 1)) * i + (row * 13f)
          val sway = sin(windPhase + i * 0.45f + row) * (6f + depth * 7f)
          val stalkColor =
            if (i % 3 == 0) GoldenHarvest.copy(alpha = 0.35f)
            else SproutGreen.copy(alpha = 0.32f + depth * 0.14f)
          drawLine(
            color = stalkColor,
            start = Offset(x, yBase),
            end = Offset(x + sway, yBase - stalkHeight),
            strokeWidth = 2.2f + depth * 1.4f,
            cap = StrokeCap.Round,
          )
        }
      }

      // 6. Walking farmer silhouette on the distant bund
      val farmerX = w * (0.15f + walkPhase * 0.68f)
      val farmerY = h * 0.58f + sin(walkPhase * 40f) * 2.5f
      // Turban
      drawCircle(color = GoldenHarvest.copy(alpha = 0.85f), radius = 6.5f, center = Offset(farmerX, farmerY - 22f))
      // Body
      drawLine(
        color = Color(0xFFFAF7F0).copy(alpha = 0.80f),
        start = Offset(farmerX, farmerY - 15f),
        end = Offset(farmerX, farmerY),
        strokeWidth = 5.5f,
        cap = StrokeCap.Round,
      )
      // Stride legs
      val stride = sin(walkPhase * 56f) * 5f
      drawLine(
        color = EarthBrown.copy(alpha = 0.85f),
        start = Offset(farmerX, farmerY),
        end = Offset(farmerX - stride, farmerY + 11f),
        strokeWidth = 3f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = EarthBrown.copy(alpha = 0.85f),
        start = Offset(farmerX, farmerY),
        end = Offset(farmerX + stride, farmerY + 11f),
        strokeWidth = 3f,
        cap = StrokeCap.Round,
      )
      // Walking staff / tool
      drawLine(
        color = GoldenHarvest.copy(alpha = 0.75f),
        start = Offset(farmerX + 7f, farmerY - 16f),
        end = Offset(farmerX + 9f, farmerY + 11f),
        strokeWidth = 2f,
      )

      // 7. Floating golden dust particles
      for (p in 0..14) {
        val px = ((p * 0.17f + dustPhase * 0.45f) % 1f) * w
        val py = h * (0.20f + ((p * 0.23f + (1f - dustPhase) * 0.35f) % 0.72f))
        drawCircle(
          color = GoldenHarvest.copy(alpha = 0.36f),
          radius = 2.2f + (p % 3) * 1.1f,
          center = Offset(px, py),
        )
      }
    }
  }
}
