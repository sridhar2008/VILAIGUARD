package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.OrderStateMachine
import com.example.domain.model.OrderStatus
import com.example.domain.model.UserRole
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.FreshLeafGreen
import com.example.ui.theme.GoldenHarvest
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SoftMintTint
import com.example.ui.theme.SoftTerracotta
import com.example.ui.theme.TerracottaAlert
import com.example.ui.theme.WarmCream
import java.text.NumberFormat
import java.util.Locale

fun formatInr(amount: Double): String {
  val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
  formatter.maximumFractionDigits = 0
  return formatter.format(amount)
}

@Composable
fun VilaiguardBrandLogo(
  size: Dp = 44.dp,
  showText: Boolean = true,
  lightText: Boolean = false,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Image(
      painter = painterResource(id = R.drawable.img_vilaiguard_logo_1795669313950),
      contentDescription = "VILAIGUARD Farmer Emblem",
      contentScale = ContentScale.Crop,
      modifier =
        Modifier.size(size)
          .clip(CircleShape)
          .border(1.5.dp, GoldenHarvest, CircleShape),
    )
    if (showText) {
      Spacer(Modifier.width(10.dp))
      Column {
        Text(
          text = "VILAIGUARD",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (lightText) WarmCream else ForestGreen,
        )
        Text(
          text = "From Our Fields to Your Family",
          style = MaterialTheme.typography.labelSmall,
          color = if (lightText) SoftGold else EarthBrown,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
fun RoleSwitcherBar(
  activeRole: UserRole,
  unreadNotifications: Int,
  onSelectRole: (UserRole) -> Unit,
  onOpenNotifications: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    shadowElevation = 2.dp,
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        VilaiguardBrandLogo(size = 36.dp, showText = true, lightText = false)
        IconButton(
          onClick = onOpenNotifications,
          modifier = Modifier.testTag("notifications_button"),
        ) {
          BadgedBox(
            badge = {
              if (unreadNotifications > 0) {
                Badge(containerColor = TerracottaAlert) {
                  Text(unreadNotifications.toString(), color = Color.White)
                }
              }
            }
          ) {
            Icon(
              imageVector = Icons.Default.Notifications,
              contentDescription = "Notifications",
              tint = ForestGreen,
            )
          }
        }
      }
      Spacer(Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        UserRole.entries.forEach { role ->
          val selected = role == activeRole
          val icon =
            when (role) {
              UserRole.CUSTOMER -> Icons.Default.ShoppingBag
              UserRole.FARMER -> Icons.Default.Agriculture
              UserRole.LOGISTICS -> Icons.Default.LocalShipping
              UserRole.ADMIN -> Icons.Default.AdminPanelSettings
            }
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (selected) ForestGreen else MaterialTheme.colorScheme.surfaceVariant,
            border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier =
              Modifier.testTag("role_chip_${role.name.lowercase()}")
                .clickable { onSelectRole(role) },
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = icon,
                contentDescription = role.label,
                tint = if (selected) Color.White else ForestGreen,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = role.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun MetricStatCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color = FreshLeafGreen,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Box(
          modifier =
            Modifier.size(30.dp)
              .clip(CircleShape)
              .background(accentColor.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
        }
      }
      Spacer(Modifier.height(8.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = accentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun StatusPill(text: String, tone: Color = FreshLeafGreen) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = tone.copy(alpha = 0.14f),
    border = BorderStroke(0.8.dp, tone.copy(alpha = 0.45f)),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.SemiBold,
      color = tone,
      modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
    )
  }
}

@Composable
fun ProductArtworkBanner(
  imageKey: String,
  organic: Boolean,
  modifier: Modifier = Modifier,
) {
  val palette =
    when (imageKey.lowercase()) {
      "tomatoes" -> listOf(Color(0xFFC44536), Color(0xFFE07A5F))
      "okra" -> listOf(Color(0xFF2D6A4F), Color(0xFF52B788))
      "mango" -> listOf(Color(0xFFD99B26), Color(0xFFF4C95D))
      "basmati" -> listOf(Color(0xFF8B5E3C), Color(0xFFD4A373))
      "pulses" -> listOf(Color(0xFFA47148), Color(0xFFDDB892))
      "dairy" -> listOf(Color(0xFF40916C), Color(0xFFB7E4C7))
      "seeds" -> listOf(Color(0xFF5C4033), Color(0xFF9C6644))
      "spices" -> listOf(Color(0xFFB5651D), Color(0xFFE09F3E))
      else -> listOf(ForestGreen, FreshLeafGreen)
    }

  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(12.dp))
        .background(Brush.linearGradient(palette))
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_produce_showcase_1795669315463),
      contentDescription = "Farm produce preview",
      contentScale = ContentScale.Crop,
      alpha = 0.34f,
      modifier = Modifier.matchParentSize(),
    )
    Row(
      modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      if (organic) {
        Surface(shape = RoundedCornerShape(8.dp), color = SoftMintTint) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Default.Eco, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
            Text("Organic", style = MaterialTheme.typography.labelSmall, color = ForestGreen)
          }
        }
      }
    }
    Icon(
      imageVector = Icons.Default.Eco,
      contentDescription = null,
      tint = Color.White.copy(alpha = 0.25f),
      modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(28.dp),
    )
  }
}

@Composable
fun OrderTimelineStepper(currentStatusRaw: String) {
  val status = OrderStatus.fromRaw(currentStatusRaw)
  if (status == OrderStatus.CANCELLED) {
    Surface(
      color = SoftTerracotta,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text = "Order Cancelled — Inventory restored to farmer catalog.",
        color = TerracottaAlert,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(12.dp),
      )
    }
    return
  }

  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    OrderStateMachine.timelineSteps.forEach { step ->
      val completed = status.stepIndex >= step.stepIndex
      val active = status == step
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier =
            Modifier.size(20.dp)
              .clip(CircleShape)
              .background(
                when {
                  active -> GoldenHarvest
                  completed -> FreshLeafGreen
                  else -> MaterialTheme.colorScheme.outline
                }
              ),
          contentAlignment = Alignment.Center,
        ) {
          if (completed) {
            Icon(
              Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(14.dp),
            )
          }
        }
        Spacer(Modifier.width(10.dp))
        Text(
          text = step.label,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
          color =
            when {
              active -> ForestGreen
              completed -> MaterialTheme.colorScheme.onSurface
              else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
      }
    }
  }
}

@Composable
fun SimpleBarChart(
  title: String,
  subtitle: String,
  bars: List<Pair<String, Double>>,
  barColor: Color = FreshLeafGreen,
) {
  val maxVal = (bars.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Spacer(Modifier.height(14.dp))
      Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
        val count = bars.size.coerceAtLeast(1)
        val slotW = size.width / count
        val barW = (slotW * 0.56f).coerceAtMost(48f)
        bars.forEachIndexed { idx, (_, value) ->
          val ratio = (value / maxVal).toFloat().coerceIn(0.05f, 1f)
          val barH = size.height * 0.85f * ratio
          val left = idx * slotW + (slotW - barW) / 2f
          val top = size.height - barH
          drawRoundRect(
            color = barColor,
            topLeft = Offset(left, top),
            size = Size(barW, barH),
            cornerRadius = CornerRadius(10f, 10f),
          )
        }
      }
      Spacer(Modifier.height(6.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        bars.forEach { (label, _) ->
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
          )
        }
      }
    }
  }
}

@Composable
fun DemoRouteMapCard(
  pickupLabel: String,
  dropLabel: String,
  progressFraction: Float,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = SoftMintTint.copy(alpha = 0.55f)),
    border = BorderStroke(1.dp, FreshLeafGreen.copy(alpha = 0.4f)),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Demo Route Preview", style = MaterialTheme.typography.titleMedium, color = ForestGreen)
        StatusPill("Simulated GPS", GoldenHarvest)
      }
      Spacer(Modifier.height(10.dp))
      Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
        val start = Offset(36f, size.height * 0.65f)
        val mid = Offset(size.width * 0.5f, size.height * 0.22f)
        val end = Offset(size.width - 36f, size.height * 0.62f)
        val path =
          androidx.compose.ui.graphics.Path().apply {
            moveTo(start.x, start.y)
            quadraticTo(mid.x, mid.y, end.x, end.y)
          }
        drawPath(
          path = path,
          color = ForestGreen.copy(alpha = 0.35f),
          style =
            androidx.compose.ui.graphics.drawscope.Stroke(
              width = 6f,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
            ),
        )
        // Farm pin
        drawCircle(color = EarthBrown, radius = 14f, center = start)
        // Customer pin
        drawCircle(color = ForestGreen, radius = 14f, center = end)
        // Courier indicator along curve
        val t = progressFraction.coerceIn(0f, 1f)
        val cx = (1 - t) * (1 - t) * start.x + 2 * (1 - t) * t * mid.x + t * t * end.x
        val cy = (1 - t) * (1 - t) * start.y + 2 * (1 - t) * t * mid.y + t * t * end.y
        drawCircle(color = GoldenHarvest, radius = 16f, center = Offset(cx, cy))
      }
      Spacer(Modifier.height(6.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Farm: $pickupLabel", style = MaterialTheme.typography.labelSmall, color = EarthBrown)
        Text("Drop: $dropLabel", style = MaterialTheme.typography.labelSmall, color = ForestGreen)
      }
    }
  }
}

@Composable
fun EmptyStateCard(title: String, message: String, icon: ImageVector = Icons.Default.Person) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(icon, contentDescription = null, tint = FreshLeafGreen, modifier = Modifier.size(40.dp))
      Spacer(Modifier.height(10.dp))
      Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Spacer(Modifier.height(4.dp))
      Text(
        message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
