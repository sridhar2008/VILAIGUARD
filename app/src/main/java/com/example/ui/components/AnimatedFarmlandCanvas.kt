package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.DarkEarthBrown
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.DeepPine
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.MossGreen
import com.example.ui.theme.OliveGreen
import com.example.ui.theme.PaintedIvory
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SoftMintTint
import com.example.ui.theme.SoftSage
import com.example.ui.theme.SproutGreen
import com.example.ui.theme.Terracotta
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Oil-paint inspired animated 3D-perspective Indian farmland environment combining the
 * sunlit paddy backdrop with real-time Compose Canvas brushwork:
 * - Warm golden morning sky & distant misty hills
 * - Painterly drifting clouds
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
    Image(
      painter = painterResource(id = R.drawable.img_hero_farmland_1795669314949),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )

    // Oil-paint deep forest & warm umber scrim for sharp typography legibility
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors =
                listOf(
                  DeepPine.copy(alpha = 0.78f),
                  DeepForestGreen.copy(alpha = 0.50f),
                  DeepPine.copy(alpha = 0.88f),
                )
            )
          )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      // 1. Warm sunrise ochre oil aura
      drawCircle(
        brush =
          Brush.radialGradient(
            colors = listOf(HarvestGold.copy(alpha = 0.42f), BurntOrange.copy(alpha = 0.14f), Color.Transparent),
            center = Offset(w * 0.18f, h * 0.22f),
            radius = w * 0.52f,
          ),
        radius = w * 0.52f,
        center = Offset(w * 0.18f, h * 0.22f),
      )

      // 2. Painterly drifting clouds
      for (c in 0..2) {
        val baseX = ((cloudPhase + c * 0.34f) % 1.25f) * (w * 1.2f) - w * 0.15f
        val baseY = h * (0.12f + c * 0.06f)
        drawOval(
          color = PaintedIvory.copy(alpha = 0.13f),
          topLeft = Offset(baseX, baseY),
          size = Size(w * 0.28f, h * 0.055f),
        )
        drawOval(
          color = SoftGold.copy(alpha = 0.10f),
          topLeft = Offset(baseX + w * 0.08f, baseY - h * 0.015f),
          size = Size(w * 0.18f, h * 0.045f),
        )
      }

      // 3. Distant olive-moss village hills silhouette
      val hillPath =
        Path().apply {
          moveTo(0f, h * 0.50f)
          quadraticTo(w * 0.22f, h * 0.40f, w * 0.48f, h * 0.48f)
          quadraticTo(w * 0.74f, h * 0.39f, w, h * 0.47f)
          lineTo(w, h * 0.56f)
          lineTo(0f, h * 0.56f)
          close()
        }
      drawPath(hillPath, color = OliveGreen.copy(alpha = 0.40f))

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
            colors = listOf(Color(0xFFA9D8DE).copy(alpha = 0.28f), HarvestGold.copy(alpha = 0.32f))
          ),
        style = Stroke(width = 14f, cap = StrokeCap.Round),
      )

      // 5. Perspective swaying paddy crop blades
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
            if (i % 3 == 0) HarvestGold.copy(alpha = 0.40f)
            else SproutGreen.copy(alpha = 0.34f + depth * 0.16f)
          drawLine(
            color = stalkColor,
            start = Offset(x, yBase),
            end = Offset(x + sway, yBase - stalkHeight),
            strokeWidth = 2.4f + depth * 1.4f,
            cap = StrokeCap.Round,
          )
        }
      }

      // 6. Walking farmer silhouette on the distant bund
      val farmerX = w * (0.15f + walkPhase * 0.68f)
      val farmerY = h * 0.58f + sin(walkPhase * 40f) * 2.5f
      drawCircle(color = HarvestGold.copy(alpha = 0.90f), radius = 6.8f, center = Offset(farmerX, farmerY - 22f))
      drawLine(
        color = PaintedIvory.copy(alpha = 0.88f),
        start = Offset(farmerX, farmerY - 15f),
        end = Offset(farmerX, farmerY),
        strokeWidth = 5.5f,
        cap = StrokeCap.Round,
      )
      val stride = sin(walkPhase * 56f) * 5f
      drawLine(
        color = DarkEarthBrown.copy(alpha = 0.90f),
        start = Offset(farmerX, farmerY),
        end = Offset(farmerX - stride, farmerY + 11f),
        strokeWidth = 3f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = DarkEarthBrown.copy(alpha = 0.90f),
        start = Offset(farmerX, farmerY),
        end = Offset(farmerX + stride, farmerY + 11f),
        strokeWidth = 3f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = HarvestGold.copy(alpha = 0.80f),
        start = Offset(farmerX + 7f, farmerY - 16f),
        end = Offset(farmerX + 9f, farmerY + 11f),
        strokeWidth = 2f,
      )

      // 7. Floating golden harvest dust particles
      for (p in 0..14) {
        val px = ((p * 0.17f + dustPhase * 0.45f) % 1f) * w
        val py = h * (0.20f + ((p * 0.23f + (1f - dustPhase) * 0.35f) % 0.72f))
        drawCircle(
          color = HarvestGold.copy(alpha = 0.42f),
          radius = 2.2f + (p % 3) * 1.1f,
          center = Offset(px, py),
        )
      }
    }
  }
}

/**
 * Customer Marketplace interactive animated 3D-shaded produce showcase header.
 */
@Composable
fun AnimatedProduceShowcaseCard(
  activeCount: Int,
  organicCount: Int,
  modifier: Modifier = Modifier,
) {
  val transition = rememberInfiniteTransition(label = "produce_showcase")
  val bob by transition.animateFloat(
    initialValue = 0f,
    targetValue = (2f * PI).toFloat(),
    animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
    label = "bob",
  )

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(1.dp, HarvestGold.copy(alpha = 0.55f)),
    colors = CardDefaults.cardColors(containerColor = DeepForestGreen),
  ) {
    Box(modifier = Modifier.fillMaxWidth().height(152.dp)) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(
          brush =
            Brush.linearGradient(
              colors = listOf(DeepForestGreen, MossGreen, OliveGreen.copy(alpha = 0.90f)),
              start = Offset.Zero,
              end = Offset(w, h),
            )
        )
        // Painted golden sun & rolling hill ribbon
        drawCircle(
          brush = Brush.radialGradient(listOf(HarvestGold.copy(alpha = 0.35f), Color.Transparent)),
          radius = w * 0.35f,
          center = Offset(w * 0.85f, h * 0.22f),
        )
        val terrain =
          Path().apply {
            moveTo(0f, h * 0.76f)
            quadraticTo(w * 0.35f, h * 0.62f, w * 0.72f, h * 0.74f)
            quadraticTo(w * 0.88f, h * 0.80f, w, h * 0.70f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
          }
        drawPath(terrain, color = DeepPine.copy(alpha = 0.45f))

        // 3D-shaded floating produce spheres on the right side
        val items =
          listOf(
            Triple(Offset(w * 0.78f, h * 0.42f + sin(bob) * 6f), 26f, Terracotta),
            Triple(Offset(w * 0.88f, h * 0.58f + cos(bob + 1f) * 5f), 22f, HarvestGold),
            Triple(Offset(w * 0.70f, h * 0.64f + sin(bob + 2.1f) * 5f), 19f, SproutGreen),
          )
        items.forEach { (center, radius, color) ->
          drawCircle(color = DeepPine.copy(alpha = 0.35f), radius = radius, center = center + Offset(3f, 5f))
          drawCircle(
            brush =
              Brush.radialGradient(
                colors = listOf(PaintedIvory.copy(alpha = 0.75f), color, DarkEarthBrown.copy(alpha = 0.75f)),
                center = center - Offset(radius * 0.35f, radius * 0.35f),
                radius = radius * 1.4f,
              ),
            radius = radius,
            center = center,
          )
        }
      }

      Column(
        modifier = Modifier.fillMaxSize().padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = HarvestGold,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Default.Eco, contentDescription = null, tint = DeepPine, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
              "Direct Morning Harvest",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = DeepPine,
            )
          }
        }
        Column {
          Text(
            "Farm-to-Family Freshness",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PaintedIvory,
          )
          Text(
            "$activeCount live lots • $organicCount certified organic harvests today",
            style = MaterialTheme.typography.bodyMedium,
            color = SoftGold,
          )
        }
      }
    }
  }
}

/**
 * Farmer Dashboard 3D soil-cutaway & swaying canopy card inspired by agricultural oil-paint & telemetry UIs.
 */
@Composable
fun AnimatedHarvestFieldBanner(
  farmName: String,
  activeProductsCount: Int,
  pendingOrdersCount: Int,
  modifier: Modifier = Modifier,
) {
  val transition = rememberInfiniteTransition(label = "farmer_field_banner")
  val swayPhase by transition.animateFloat(
    initialValue = 0f,
    targetValue = (2f * PI).toFloat(),
    animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing), RepeatMode.Restart),
    label = "sway",
  )

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(1.dp, BurntOrange.copy(alpha = 0.45f)),
    colors = CardDefaults.cardColors(containerColor = DarkEarthBrown),
  ) {
    Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        // Oil-paint earthy terracotta-to-umber background
        drawRect(
          brush = Brush.verticalGradient(listOf(Color(0xFF9C5939), DarkEarthBrown, Color(0xFF2D2018)))
        )
        // Isometric 3D soil cutaway block on the right
        val blockLeft = w * 0.62f
        val blockRight = w * 0.94f
        val blockMid = w * 0.78f
        val topGrass =
          Path().apply {
            moveTo(blockMid, h * 0.24f)
            lineTo(blockRight, h * 0.40f)
            lineTo(blockMid, h * 0.56f)
            lineTo(blockLeft, h * 0.40f)
            close()
          }
        drawPath(topGrass, brush = Brush.linearGradient(listOf(MossGreen, SproutGreen)))

        val soilLeftFace =
          Path().apply {
            moveTo(blockLeft, h * 0.40f)
            lineTo(blockMid, h * 0.56f)
            lineTo(blockMid, h * 0.84f)
            lineTo(blockLeft, h * 0.68f)
            close()
          }
        drawPath(soilLeftFace, color = BurntOrange)

        val soilRightFace =
          Path().apply {
            moveTo(blockMid, h * 0.56f)
            lineTo(blockRight, h * 0.40f)
            lineTo(blockRight, h * 0.68f)
            lineTo(blockMid, h * 0.84f)
            close()
          }
        drawPath(soilRightFace, color = Color(0xFF5D3A28))

        // Swaying mini crops atop the 3D field plot
        for (i in 0..5) {
          val px = w * (0.71f + i * 0.032f)
          val py = h * (0.37f + (i % 2) * 0.04f)
          val sway = sin(swayPhase + i * 0.8f) * 4.5f
          drawLine(
            color = SoftGold,
            start = Offset(px, py),
            end = Offset(px + sway, py - 16f),
            strokeWidth = 3f,
            cap = StrokeCap.Round,
          )
        }
      }

      Column(
        modifier = Modifier.fillMaxSize().padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Surface(shape = RoundedCornerShape(10.dp), color = HarvestGold) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Default.Spa, contentDescription = null, tint = DeepPine, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
              "Field Soil & Yield: Optimal",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = DeepPine,
            )
          }
        }
        Column {
          Text(
            farmName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PaintedIvory,
          )
          Text(
            "$activeProductsCount crop lots live • $pendingOrdersCount orders awaiting action",
            style = MaterialTheme.typography.bodyMedium,
            color = SoftGold,
          )
        }
      }
    }
  }
}

/**
 * Logistics Dashboard animated cold-chain carrier moving smoothly along a painted highway corridor.
 */
@Composable
fun AnimatedDeliveryTruckBanner(
  activeRuns: Int,
  completedRuns: Int,
  modifier: Modifier = Modifier,
) {
  val transition = rememberInfiniteTransition(label = "logistics_truck")
  val truckProgress by transition.animateFloat(
    initialValue = 0.08f,
    targetValue = 0.92f,
    animationSpec = infiniteRepeatable(
      animation = tween(5500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "truck_x",
  )

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(1.dp, MossGreen),
    colors = CardDefaults.cardColors(containerColor = DeepForestGreen),
  ) {
    Box(modifier = Modifier.fillMaxWidth().height(138.dp)) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(Brush.horizontalGradient(listOf(DeepPine, DeepForestGreen, MossGreen)))
        // Road ribbon
        val roadY = h * 0.78f
        drawLine(
          color = SoftGold.copy(alpha = 0.45f),
          start = Offset(24f, roadY),
          end = Offset(w - 24f, roadY),
          strokeWidth = 6f,
          cap = StrokeCap.Round,
        )
        // Animated cold-chain carrier block
        val tx = w * truckProgress
        drawRoundRect(
          color = HarvestGold,
          topLeft = Offset(tx - 26f, roadY - 26f),
          size = Size(42f, 20f),
          cornerRadius = CornerRadius(6f, 6f),
        )
        drawRoundRect(
          color = PaintedIvory,
          topLeft = Offset(tx + 14f, roadY - 20f),
          size = Size(14f, 14f),
          cornerRadius = CornerRadius(4f, 4f),
        )
        drawCircle(color = DeepPine, radius = 5f, center = Offset(tx - 15f, roadY - 4f))
        drawCircle(color = DeepPine, radius = 5f, center = Offset(tx + 17f, roadY - 4f))
      }

      Row(
        modifier = Modifier.fillMaxSize().padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column {
          Text(
            "Cold-Chain Corridor Live",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PaintedIvory,
          )
          Text(
            "$activeRuns active dispatches • $completedRuns completed handoffs",
            style = MaterialTheme.typography.bodyMedium,
            color = SoftGold,
          )
        }
        Box(
          modifier = Modifier.size(40.dp).clip(CircleShape).background(HarvestGold),
          contentAlignment = Alignment.Center,
        ) {
          Icon(Icons.Default.LocalShipping, contentDescription = null, tint = DeepPine)
        }
      }
    }
  }
}

/**
 * Admin Ecosystem animated globe & harvest trade-arc header.
 */
@Composable
fun AnimatedAgriGlobeBanner(
  totalOrders: Int,
  farmersCount: Int,
  bannersCount: Int,
  modifier: Modifier = Modifier,
) {
  val transition = rememberInfiniteTransition(label = "admin_globe")
  val spin by transition.animateFloat(
    initialValue = 0f,
    targetValue = (2f * PI).toFloat(),
    animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
    label = "spin",
  )

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(1.dp, HarvestGold.copy(alpha = 0.5f)),
    colors = CardDefaults.cardColors(containerColor = DeepPine),
  ) {
    Box(modifier = Modifier.fillMaxWidth().height(144.dp)) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(Brush.linearGradient(listOf(DeepPine, DeepForestGreen, Color(0xFF284F3C))))

        // Animated orbital globe on right
        val center = Offset(w * 0.83f, h * 0.50f)
        val radius = 48f
        drawCircle(
          brush = Brush.radialGradient(listOf(MossGreen.copy(alpha = 0.8f), DeepPine)),
          radius = radius,
          center = center,
        )
        drawCircle(
          color = HarvestGold.copy(alpha = 0.65f),
          radius = radius,
          center = center,
          style = Stroke(width = 2.5f),
        )
        // Rotating meridians
        for (m in 0..2) {
          val offset = cos(spin + m * 1.05f) * radius
          drawOval(
            color = SoftSage.copy(alpha = 0.45f),
            topLeft = Offset(center.x - kotlin.math.abs(offset), center.y - radius),
            size = Size((kotlin.math.abs(offset) * 2f).coerceAtLeast(4f), radius * 2f),
            style = Stroke(width = 1.6f),
          )
        }
        // Pulsing harvest node
        val nodeX = center.x + cos(spin) * radius * 0.72f
        val nodeY = center.y + sin(spin * 2f) * radius * 0.38f
        drawCircle(color = HarvestGold, radius = 5.5f, center = Offset(nodeX, nodeY))
      }

      Column(
        modifier = Modifier.fillMaxSize().padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Surface(shape = RoundedCornerShape(10.dp), color = SoftMintTint) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Default.Public, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
              "Ecosystem Network Active",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = DeepForestGreen,
            )
          }
        }
        Column {
          Text(
            "VILAIGUARD Command Hub",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PaintedIvory,
          )
          Text(
            "$farmersCount verified farms • $totalOrders orders • $bannersCount promo banners",
            style = MaterialTheme.typography.bodyMedium,
            color = SoftGold,
          )
        }
      }
    }
  }
}
