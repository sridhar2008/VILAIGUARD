package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.firestore.onGoogleSignInClicked
import com.example.domain.model.UserRole
import com.example.ui.components.AnimatedFarmlandHeroBackground
import com.example.ui.components.StatusPill
import com.example.ui.components.VilaiguardBrandLogo
import com.example.ui.theme.DeepPine
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.FreshLeafGreen
import com.example.ui.theme.GoldenHarvest
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SoftMintTint
import com.example.ui.theme.WarmCream

@Composable
fun LandingScreen(
  onSignedInWithIntent: (UserRole) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var isLoading by remember { mutableStateOf(false) }
  var authError by remember { mutableStateOf<String?>(null) }
  var pendingIntentRole by remember { mutableStateOf(UserRole.CUSTOMER) }
  var expandedFaq by remember { mutableIntStateOf(0) }

  fun launchGoogleSignIn(desiredRole: UserRole) {
    pendingIntentRole = desiredRole
    authError = null
    isLoading = true
    onGoogleSignInClicked(
      context = context as Activity,
      credentialManager = credentialManager,
      onAuthSuccess = {
        isLoading = false
        onSignedInWithIntent(pendingIntentRole)
      },
      onAuthError = { msg ->
        isLoading = false
        authError = msg
      },
      scope = scope,
      onAuthCancelled = { isLoading = false },
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize().background(WarmCream).navigationBarsPadding(),
    contentPadding = PaddingValues(bottom = 32.dp),
  ) {
    // 1. HERO SECTION WITH ANIMATED 3D-PERSPECTIVE FARMLAND
    item {
      Box(modifier = Modifier.fillMaxWidth().height(540.dp)) {
        AnimatedFarmlandHeroBackground(modifier = Modifier.fillMaxSize())
        Column(
          modifier =
            Modifier.fillMaxSize()
              .statusBarsPadding()
              .padding(horizontal = 20.dp, vertical = 16.dp),
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            VilaiguardBrandLogo(size = 42.dp, showText = true, lightText = true)
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color.White.copy(alpha = 0.16f),
              border = BorderStroke(1.dp, GoldenHarvest.copy(alpha = 0.6f)),
              modifier =
                Modifier.testTag("hero_login_chip")
                  .clickable(enabled = !isLoading) { launchGoogleSignIn(UserRole.CUSTOMER) },
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(Icons.Default.Login, contentDescription = "Login", tint = SoftGold, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Login / Register", style = MaterialTheme.typography.labelMedium, color = Color.White)
              }
            }
          }

          Column(modifier = Modifier.widthIn(max = 600.dp)) {
            StatusPill("Direct Farm-to-Family Network", GoldenHarvest)
            Spacer(Modifier.height(10.dp))
            Text(
              text = stringResource(R.string.hero_headline),
              style = MaterialTheme.typography.displayLarge,
              color = WarmCream,
            )
            Spacer(Modifier.height(10.dp))
            Text(
              text = stringResource(R.string.hero_subheadline),
              style = MaterialTheme.typography.bodyLarge,
              color = Color.White.copy(alpha = 0.90f),
            )
            Spacer(Modifier.height(18.dp))

            if (authError != null) {
              Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
              ) {
                Text(
                  text = authError ?: "",
                  color = MaterialTheme.colorScheme.onErrorContainer,
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(10.dp),
                )
              }
            }

            // Primary Google Sign-In CTA
            Button(
              onClick = { launchGoogleSignIn(UserRole.CUSTOMER) },
              enabled = !isLoading,
              colors = ButtonDefaults.buttonColors(containerColor = GoldenHarvest, contentColor = DeepPine),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth().height(50.dp).testTag("google_sign_in_button"),
            ) {
              if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DeepPine, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Connecting to VILAIGUARD...", fontWeight = FontWeight.Bold)
              } else {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign in with Google • Explore Products", fontWeight = FontWeight.Bold)
              }
            }

            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedButton(
                onClick = { launchGoogleSignIn(UserRole.FARMER) },
                enabled = !isLoading,
                border = BorderStroke(1.dp, SoftMintTint),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmCream),
                modifier = Modifier.weight(1f).testTag("join_farmer_button"),
              ) {
                Icon(Icons.Default.Agriculture, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Join as Farmer", maxLines = 1)
              }
              OutlinedButton(
                onClick = { launchGoogleSignIn(UserRole.LOGISTICS) },
                enabled = !isLoading,
                border = BorderStroke(1.dp, SoftGold),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmCream),
                modifier = Modifier.weight(1f).testTag("join_logistics_button"),
              ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Deliver", maxLines = 1)
              }
            }
          }
        }
      }
    }

    // 2. ABOUT VILAIGUARD
    item {
      SectionContainer(title = "About VILAIGUARD", subtitle = "Eliminating supply-chain middlemen across rural India") {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              "Traditional agricultural supply chains pass produce through 4–6 brokers before reaching families or restaurants. Farmers earn a fraction of retail value while buyers pay inflated prices for older stock.",
              style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(8.dp))
            Text(
              "VILAIGUARD connects verified growers, households, restaurants, and cold-chain delivery partners on one transparent platform.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }

    // 3. HOW THE PLATFORM WORKS
    item {
      SectionContainer(title = "How the Platform Works", subtitle = "Four connected roles, one real-time order pipeline") {
        val steps =
          listOf(
            Triple("1. Farmer Lists Harvest", "Growers publish fresh crops, organic tags, stock & harvest dates.", Icons.Default.Agriculture),
            Triple("2. Customer Orders Direct", "Families & restaurants shop direct with transparent farm pricing.", Icons.Default.ShoppingBag),
            Triple("3. Admin Dispatches Fleet", "Verified delivery partners are assigned as soon as crates are ready.", Icons.Default.Handshake),
            Triple("4. Farm-to-Door Delivery", "OTP-verified pickup & delivery with live status tracking.", Icons.Default.LocalShipping),
          )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          steps.forEach { (heading, desc, icon) ->
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
              Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier.size(40.dp).clip(CircleShape).background(SoftMintTint),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(icon, contentDescription = null, tint = ForestGreen)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                  Text(heading, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
          }
        }
      }
    }

    // 4. FRESH PRODUCTS PREVIEW
    item {
      SectionContainer(title = "Fresh Harvest Preview", subtitle = "Direct from Nashik, Ludhiana, Wayanad & Mandya") {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column {
            Image(
              painter = painterResource(id = R.drawable.img_produce_showcase_1795669315463),
              contentDescription = "Freshly harvested Indian produce",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxWidth().height(180.dp),
            )
            Column(modifier = Modifier.padding(16.dp)) {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill("Vegetables • ₹48/kg", FreshLeafGreen)
                StatusPill("Spices • ₹210", GoldenHarvest)
                StatusPill("Grains • ₹135/kg", EarthBrown)
              }
              Spacer(Modifier.height(10.dp))
              Text(
                "Sign in to browse 8 farm categories, inspect harvest dates, filter by district, and place live orders.",
                style = MaterialTheme.typography.bodyMedium,
              )
              Spacer(Modifier.height(10.dp))
              Button(
                onClick = { launchGoogleSignIn(UserRole.CUSTOMER) },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
              ) {
                Text("Open Live Marketplace")
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }

    // 5. FEATURED FARMERS
    item {
      SectionContainer(title = "Featured Growers", subtitle = "Verified agricultural custodians") {
        val growers =
          listOf(
            Triple("Ramesh Patel", "Sahyadri Organic Acres • Nashik, MH", "Tomatoes, Okra & Grapes"),
            Triple("Gurpreet Singh Gill", "Sutlej Golden Harvest • Ludhiana, PB", "Aged Basmati & Toor Dal"),
            Triple("Kavitha Nair", "Malabar Spice Canopy • Wayanad, KL", "Turmeric, Pepper & Forest Honey"),
          )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          growers.forEach { (name, place, crops) ->
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
              Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = "Verified Farmer", tint = FreshLeafGreen, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  Text(place, style = MaterialTheme.typography.labelSmall, color = EarthBrown)
                  Text(crops, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
          }
        }
      }
    }

    // 6 & 7. BENEFITS FOR FARMERS & CUSTOMERS + LOGISTICS
    item {
      SectionContainer(title = "Why VILAIGUARD", subtitle = "Built for growers, families, restaurants & carriers") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          BenefitCard(
            title = "For Farmers",
            points = listOf("Set your own harvest prices & inventory", "Direct orders with transparent 5% platform fee", "Instant earnings & low-stock alerts"),
            accent = ForestGreen,
          )
          BenefitCard(
            title = "For Customers & Restaurants",
            points = listOf("Morning-harvested produce with exact harvest dates", "Verified organic badges & bulk restaurant packs", "8-step real-time farm-to-door tracking"),
            accent = FreshLeafGreen,
          )
          BenefitCard(
            title = "For Delivery Partners",
            points = listOf("Clear pickup & drop tasks with farm instructions", "OTP-secured handoff & delivery proof logs", "Route map previews & performance stats"),
            accent = EarthBrown,
          )
        }
      }
    }

    // 9. PLATFORM STATISTICS
    item {
      SectionContainer(title = "Ecosystem Impact (Demo Metrics)", subtitle = "Connecting rural harvests to urban kitchens") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          StatTile("450+", "Verified Farms", Modifier.weight(1f))
          StatTile("28%", "Higher Farmer Pay", Modifier.weight(1f))
          StatTile("<18h", "Harvest to Home", Modifier.weight(1f))
        }
      }
    }

    // 10. TESTIMONIALS
    item {
      SectionContainer(title = "Community Voices", subtitle = "Growers & buyers on VILAIGUARD") {
        Card(
          colors = CardDefaults.cardColors(containerColor = SoftGold.copy(alpha = 0.45f)),
          border = BorderStroke(1.dp, GoldenHarvest.copy(alpha = 0.5f)),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              repeat(5) {
                Icon(Icons.Default.Star, contentDescription = null, tint = GoldenHarvest, modifier = Modifier.size(16.dp))
              }
              Spacer(Modifier.width(8.dp))
              Text("Ramesh Patel • Nashik", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(
              "\"Before VILAIGUARD, mandi agents took 35% of my tomato crop value. Now local families and Pune restaurants order straight from my field.\"",
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      }
    }

    // 11. FAQ ACCORDION
    item {
      val faqs =
        listOf(
          "How are farmers verified?" to "Administrators review farm land details, FPO/organic certificates, and bank details before granting a Verified badge.",
          "How does order fulfillment work across roles?" to "Customers place stock-checked orders -> Farmers accept & mark Ready for Pickup -> Admin dispatches Logistics -> Delivery Partner confirms pickup & OTP delivery.",
          "Are payments real in this demonstration build?" to "Checkout uses a clearly labeled Demo Payment Mode (Simulated UPI / Card / COD) so all order workflows run end-to-end without charging real funds.",
          "Can I test all four roles?" to "Yes! Sign in with Google to initialize your live Firestore workspace and switch seamlessly between Customer, Farmer, Delivery Partner, and Admin dashboards.",
        )
      SectionContainer(title = "Frequently Asked Questions", subtitle = "Everything you need to know") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          faqs.forEachIndexed { index, (q, a) ->
            val open = expandedFaq == index
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              modifier = Modifier.clickable { expandedFaq = if (open) -1 else index },
            ) {
              Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(q, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                  Icon(
                    if (open) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle answer",
                    tint = ForestGreen,
                  )
                }
                AnimatedVisibility(visible = open) {
                  Text(
                    text = a,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                  )
                }
              }
            }
          }
        }
      }
    }

    // 12. FOOTER
    item {
      HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        VilaiguardBrandLogo(size = 38.dp, showText = true, lightText = false)
        Spacer(Modifier.height(8.dp))
        Text(
          "VILAIGUARD Direct Agricultural Marketplace • Powered by Cloud Firestore",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun SectionContainer(
  title: String,
  subtitle: String,
  content: @Composable () -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
    Text(title, style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = EarthBrown)
    Spacer(Modifier.height(12.dp))
    content()
  }
}

@Composable
private fun BenefitCard(title: String, points: List<String>, accent: Color) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
      Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
      Spacer(Modifier.height(6.dp))
      points.forEach { p ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(8.dp))
          Text(p, style = MaterialTheme.typography.bodyMedium)
        }
      }
    }
  }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = ForestGreen),
  ) {
    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
      Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = GoldenHarvest)
      Text(label, style = MaterialTheme.typography.labelSmall, color = WarmCream)
    }
  }
}
