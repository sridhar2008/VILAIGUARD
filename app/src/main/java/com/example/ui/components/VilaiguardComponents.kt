package com.example.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.OrderStateMachine
import com.example.domain.model.OrderStatus
import com.example.domain.model.PromotionalBanner
import com.example.domain.model.UserRole
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.DarkEarthBrown
import com.example.ui.theme.DeepAmber
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.DeepPine
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.FreshLeafGreen
import com.example.ui.theme.GoldenHarvest
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.MossGreen
import com.example.ui.theme.OliveGreen
import com.example.ui.theme.PaintedIvory
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SoftMintTint
import com.example.ui.theme.SoftTerracotta
import com.example.ui.theme.Terracotta
import com.example.ui.theme.TerracottaAlert
import com.example.ui.theme.WarmCream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
  onShareApp: () -> Unit = {},
  onRequestSignOut: () -> Unit = {},
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
        VilaiguardBrandLogo(size = 34.dp, showText = true, lightText = false)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          IconButton(
            onClick = onShareApp,
            modifier = Modifier.size(42.dp).testTag("share_app_top_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share VILAIGUARD Link",
              tint = DeepForestGreen,
            )
          }
          IconButton(
            onClick = onOpenNotifications,
            modifier = Modifier.size(42.dp).testTag("notifications_button"),
          ) {
            BadgedBox(
              badge = {
                if (unreadNotifications > 0) {
                  Badge(containerColor = TerracottaAlert) {
                    Text(unreadNotifications.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = DeepForestGreen,
              )
            }
          }
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = SoftTerracotta,
            border = BorderStroke(1.dp, Terracotta.copy(alpha = 0.55f)),
            modifier =
              Modifier.testTag("topbar_logout_button")
                .clickable(onClick = onRequestSignOut),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Logout",
                tint = DarkEarthBrown,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "Logout",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = DarkEarthBrown,
              )
            }
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
            color = if (selected) DeepForestGreen else MaterialTheme.colorScheme.surfaceVariant,
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
                tint = if (selected) PaintedIvory else DeepForestGreen,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = role.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) PaintedIvory else DeepCharcoal,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun PromotionalBannerSlider(
  banners: List<PromotionalBanner>,
  onBannerClick: (PromotionalBanner) -> Unit,
  modifier: Modifier = Modifier,
) {
  val activeBanners =
    banners.filter { it.active }.sortedBy { it.priority }.ifEmpty {
      listOf(
        PromotionalBanner(
          id = "default_1",
          title = "Fresh Harvest, Direct From Farmers",
          subtitle = "Morning-picked farm crops delivered with full price transparency.",
          ctaLabel = "Explore Market",
          targetSection = "MARKET",
          themeStyle = "FOREST",
          scheduleNote = "Daily Harvest",
        ),
        PromotionalBanner(
          id = "default_2",
          title = "Support Local Farmers. Eat Fresh.",
          subtitle = "95% of your basket goes directly to verified rural agricultural custodians.",
          ctaLabel = "Browse Organic",
          targetSection = "ORGANIC",
          themeStyle = "OLIVE",
          scheduleNote = "Verified Growers",
        ),
      )
    }

  val pagerState = rememberPagerState(pageCount = { activeBanners.size })
  val scope = rememberCoroutineScope()

  // Smooth auto-sliding; pauses whenever the user actively drags
  LaunchedEffect(activeBanners.size, pagerState.isScrollInProgress) {
    if (activeBanners.size > 1 && !pagerState.isScrollInProgress) {
      delay(4500L)
      val next = (pagerState.currentPage + 1) % activeBanners.size
      pagerState.animateScrollToPage(next)
    }
  }

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 0.dp),
      pageSpacing = 12.dp,
    ) { page ->
      val banner = activeBanners[page]
      val bgColors =
        when (banner.themeStyle) {
          "HARVEST" -> listOf(Color(0xFF4A3514), DarkEarthBrown, HarvestGold.copy(alpha = 0.85f))
          "TERRACOTTA" -> listOf(Color(0xFF5A271B), Terracotta, BurntOrange)
          "OLIVE" -> listOf(DeepForestGreen, MossGreen, OliveGreen)
          else -> listOf(DeepPine, DeepForestGreen, MossGreen)
        }

      Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, HarvestGold.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = bgColors.first()),
        modifier =
          Modifier.fillMaxWidth()
            .clickable { onBannerClick(banner) }
            .testTag("promo_banner_card_${banner.id}"),
      ) {
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .background(Brush.linearGradient(bgColors))
              .padding(16.dp),
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Surface(shape = RoundedCornerShape(10.dp), color = HarvestGold) {
                Text(
                  text = banner.scheduleNote.ifBlank { "Featured Offer" },
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = DeepPine,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
              }
              Text(
                text = "${page + 1} / ${activeBanners.size}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = SoftGold,
              )
            }
            Text(
              text = banner.title,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = PaintedIvory,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = banner.subtitle,
              style = MaterialTheme.typography.bodyMedium,
              color = PaintedIvory.copy(alpha = 0.92f),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Button(
                onClick = { onBannerClick(banner) },
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGold, contentColor = DeepPine),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
              ) {
                Text(banner.ctaLabel.ifBlank { "Explore" }, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
              }

              if (activeBanners.size > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  IconButton(
                    onClick = {
                      val prev = if (pagerState.currentPage == 0) activeBanners.lastIndex else pagerState.currentPage - 1
                      scope.launch { pagerState.animateScrollToPage(prev) }
                    },
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(PaintedIvory.copy(alpha = 0.18f)),
                  ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Banner", tint = PaintedIvory, modifier = Modifier.size(16.dp))
                  }
                  IconButton(
                    onClick = {
                      val next = (pagerState.currentPage + 1) % activeBanners.size
                      scope.launch { pagerState.animateScrollToPage(next) }
                    },
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(PaintedIvory.copy(alpha = 0.18f)),
                  ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Banner", tint = PaintedIvory, modifier = Modifier.size(16.dp))
                  }
                }
              }
            }
          }
        }
      }
    }

    if (activeBanners.size > 1) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        activeBanners.forEachIndexed { idx, _ ->
          val active = idx == pagerState.currentPage
          Box(
            modifier =
              Modifier.padding(horizontal = 3.dp)
                .height(6.dp)
                .width(if (active) 20.dp else 6.dp)
                .clip(CircleShape)
                .background(if (active) DeepForestGreen else MossGreen.copy(alpha = 0.35f))
                .clickable { scope.launch { pagerState.animateScrollToPage(idx) } },
          )
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
  val readableAccent = if (accentColor == GoldenHarvest || accentColor == HarvestGold) DeepAmber else accentColor
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
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Box(
          modifier =
            Modifier.size(30.dp)
              .clip(CircleShape)
              .background(accentColor.copy(alpha = 0.16f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(icon, contentDescription = null, tint = readableAccent, modifier = Modifier.size(16.dp))
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
        fontWeight = FontWeight.SemiBold,
        color = readableAccent,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun StatusPill(text: String, tone: Color = FreshLeafGreen) {
  val isGold = tone == GoldenHarvest || tone == HarvestGold || tone == SoftGold
  val bgColor = if (isGold) HarvestGold.copy(alpha = 0.24f) else tone.copy(alpha = 0.15f)
  val borderColor = if (isGold) DeepAmber.copy(alpha = 0.45f) else tone.copy(alpha = 0.48f)
  val textColor = if (isGold) DeepAmber else tone
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = bgColor,
    border = BorderStroke(0.9.dp, borderColor),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = textColor,
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
            Text("Organic", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ForestGreen)
          }
        }
      }
    }
    Icon(
      imageVector = Icons.Default.Eco,
      contentDescription = null,
      tint = Color.White.copy(alpha = 0.28f),
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
              tint = if (active) DeepPine else Color.White,
              modifier = Modifier.size(14.dp),
            )
          }
        }
        Spacer(Modifier.width(10.dp))
        Text(
          text = step.label,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
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
      Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
            fontWeight = FontWeight.SemiBold,
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
  val context = LocalContext.current

  Card(
    colors = CardDefaults.cardColors(containerColor = SoftMintTint.copy(alpha = 0.65f)),
    border = BorderStroke(1.dp, FreshLeafGreen.copy(alpha = 0.45f)),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          "Farm-to-Customer Route",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = ForestGreen,
        )
        StatusPill(
          text = "Route Ready",
          tone = FreshLeafGreen,
        )
      }
      Spacer(Modifier.height(4.dp))
      Text(
        "Interactive route progress preview. Tap below for turn-by-turn Google Maps directions (no API key needed).",
        style = MaterialTheme.typography.labelSmall,
        color = DarkEarthBrown,
      )
      Spacer(Modifier.height(10.dp))
      Canvas(modifier = Modifier.fillMaxWidth().height(92.dp)) {
        val start = Offset(36f, size.height * 0.68f)
        val mid = Offset(size.width * 0.5f, size.height * 0.20f)
        val end = Offset(size.width - 36f, size.height * 0.64f)
        val path =
          androidx.compose.ui.graphics.Path().apply {
            moveTo(start.x, start.y)
            quadraticTo(mid.x, mid.y, end.x, end.y)
          }
        drawPath(
          path = path,
          color = ForestGreen.copy(alpha = 0.28f),
          style =
            androidx.compose.ui.graphics.drawscope.Stroke(
              width = 6f,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
            ),
        )
        val t = progressFraction.coerceIn(0f, 1f)
        if (t > 0f) {
          val steps = 20
          val completedPath =
            androidx.compose.ui.graphics.Path().apply {
              moveTo(start.x, start.y)
              for (i in 1..steps) {
                val u = t * (i / steps.toFloat())
                val px = (1 - u) * (1 - u) * start.x + 2 * (1 - u) * u * mid.x + u * u * end.x
                val py = (1 - u) * (1 - u) * start.y + 2 * (1 - u) * u * mid.y + u * u * end.y
                lineTo(px, py)
              }
            }
          drawPath(
            path = completedPath,
            color = FreshLeafGreen,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 7f),
          )
        }
        drawCircle(color = EarthBrown, radius = 14f, center = start)
        drawCircle(color = ForestGreen, radius = 14f, center = end)
        val cx = (1 - t) * (1 - t) * start.x + 2 * (1 - t) * t * mid.x + t * t * end.x
        val cy = (1 - t) * (1 - t) * start.y + 2 * (1 - t) * t * mid.y + t * t * end.y
        drawCircle(color = PaintedIvory, radius = 20f, center = Offset(cx, cy))
        drawCircle(color = GoldenHarvest, radius = 14f, center = Offset(cx, cy))
      }
      Spacer(Modifier.height(6.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Farm: $pickupLabel", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = EarthBrown)
        Text("Drop: $dropLabel", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = ForestGreen)
      }
      Spacer(Modifier.height(10.dp))
      OutlinedButton(
        onClick = {
          val origin = Uri.encode(pickupLabel)
          val dest = Uri.encode(dropLabel)
          val url = "https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$dest"
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
          runCatching { context.startActivity(intent) }
        },
        border = BorderStroke(1.dp, DeepForestGreen),
        modifier = Modifier.fillMaxWidth().testTag("open_maps_directions_button"),
      ) {
        Icon(Icons.Default.Directions, contentDescription = "Open Google Maps", tint = DeepForestGreen, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Open Google Maps Directions", color = DeepForestGreen, fontWeight = FontWeight.Bold)
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
