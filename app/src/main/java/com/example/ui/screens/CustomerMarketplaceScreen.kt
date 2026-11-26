package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.model.CartItem
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.model.Product
import com.example.domain.model.ProductCategory
import com.example.domain.model.SavedAddress
import com.example.domain.model.UiState
import com.example.domain.model.UserProfile
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import com.example.ui.components.AnimatedProduceShowcaseCard
import com.example.ui.components.DemoRouteMapCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.OrderTimelineStepper
import com.example.ui.components.ProductArtworkBanner
import com.example.ui.components.PromotionalBannerSlider
import com.example.ui.components.ShareLinkHelper
import com.example.ui.components.StatusPill
import com.example.ui.components.VilaiguardDeepLink
import com.example.ui.components.formatInr
import com.example.ui.theme.DeepAmber
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.DeepPine
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.FreshLeafGreen
import com.example.ui.theme.GoldenHarvest
import com.example.ui.theme.PaintedIvory
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SoftMintTint
import com.example.ui.theme.SoftSage
import com.example.ui.theme.TerracottaAlert
import com.example.ui.viewmodel.VilaiguardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMarketplaceScreen(
  viewModel: VilaiguardViewModel,
  profile: UserProfile?,
  pendingDeepLink: VilaiguardDeepLink? = null,
  onConsumeDeepLink: () -> Unit = {},
  onEditRoleProfile: () -> Unit,
  onSignOut: () -> Unit,
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) }
  var selectedProduct by remember { mutableStateOf<Product?>(null) }
  var selectedOrder by remember { mutableStateOf<Order?>(null) }
  var showAddAddressDialog by remember { mutableStateOf(false) }
  var showSupportDialog by remember { mutableStateOf(false) }

  val productsState by viewModel.products.collectAsStateWithLifecycle()
  val filteredProductsState by viewModel.filteredProducts.collectAsStateWithLifecycle()
  val filters by viewModel.filters.collectAsStateWithLifecycle()
  val cartState by viewModel.cartItems.collectAsStateWithLifecycle()
  val ordersState by viewModel.orders.collectAsStateWithLifecycle()
  val addressesState by viewModel.addresses.collectAsStateWithLifecycle()
  val wishlistState by viewModel.wishlist.collectAsStateWithLifecycle()
  val reviewsState by viewModel.reviews.collectAsStateWithLifecycle()
  val ticketsState by viewModel.supportTickets.collectAsStateWithLifecycle()
  val bannersState by viewModel.banners.collectAsStateWithLifecycle()
  val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

  val cartItems = (cartState as? UiState.Success)?.data.orEmpty()
  val wishlistIds = (wishlistState as? UiState.Success)?.data.orEmpty().map { it.productId }.toSet()
  val addresses = (addressesState as? UiState.Success)?.data.orEmpty()
  val allOrders = (ordersState as? UiState.Success)?.data.orEmpty()
  val allProducts = (productsState as? UiState.Success)?.data.orEmpty()
  val banners = (bannersState as? UiState.Success)?.data.orEmpty()

  LaunchedEffect(pendingDeepLink, allProducts, allOrders) {
    when (pendingDeepLink) {
      is VilaiguardDeepLink.OpenProduct -> {
        val target = allProducts.firstOrNull { it.id == pendingDeepLink.productId }
        if (target != null) {
          selectedTab = 0
          selectedProduct = target
          onConsumeDeepLink()
        }
      }
      is VilaiguardDeepLink.OpenOrder -> {
        val target =
          allOrders.firstOrNull {
            it.orderNumber.equals(pendingDeepLink.orderNumberOrId, ignoreCase = true) ||
              it.id == pendingDeepLink.orderNumberOrId
          }
        if (target != null) {
          selectedTab = 2
          selectedOrder = target
          onConsumeDeepLink()
        }
      }
      else -> Unit
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useRail = maxWidth >= 720.dp
    Row(modifier = Modifier.fillMaxSize()) {
      if (useRail) {
        NavigationRail(
          containerColor = DeepForestGreen,
          contentColor = PaintedIvory,
          modifier = Modifier.fillMaxHeight(),
        ) {
          val railColors =
            NavigationRailItemDefaults.colors(
              selectedIconColor = DeepPine,
              selectedTextColor = SoftGold,
              indicatorColor = GoldenHarvest,
              unselectedIconColor = SoftSage,
              unselectedTextColor = SoftSage,
            )
          NavigationRailItem(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            icon = { Icon(Icons.Default.Home, contentDescription = "Market") },
            label = { Text("Market") },
            colors = railColors,
          )
          NavigationRailItem(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            icon = {
              BadgedBox(
                badge = {
                  if (cartItems.isNotEmpty()) {
                    Badge(containerColor = GoldenHarvest) { Text(cartItems.size.toString(), color = DeepPine) }
                  }
                }
              ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
              }
            },
            label = { Text("Cart") },
            colors = railColors,
          )
          NavigationRailItem(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Orders") },
            label = { Text("Orders") },
            colors = railColors,
          )
          NavigationRailItem(
            selected = selectedTab == 3,
            onClick = { selectedTab = 3 },
            icon = { Icon(Icons.Default.Person, contentDescription = "Account") },
            label = { Text("Account") },
            colors = railColors,
          )
        }
      }

      Scaffold(
        modifier = Modifier.weight(1f),
        bottomBar = {
          if (!useRail) {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
              NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.Home, contentDescription = "Market") },
                label = { Text("Market") },
              )
              NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = {
                  BadgedBox(
                    badge = {
                      if (cartItems.isNotEmpty()) {
                        Badge(containerColor = ForestGreen) { Text(cartItems.size.toString(), color = PaintedIvory) }
                      }
                    }
                  ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                  }
                },
                label = { Text("Cart") },
              )
              NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Orders") },
                label = { Text("Orders") },
              )
              NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                icon = { Icon(Icons.Default.Person, contentDescription = "Account") },
                label = { Text("Account") },
              )
            }
          }
        }
      ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          when (selectedTab) {
            0 -> {
              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                item {
                  AnimatedProduceShowcaseCard(
                    activeCount = allProducts.size,
                    organicCount = allProducts.count { it.organic },
                  )
                }
                item {
                  PromotionalBannerSlider(
                    banners = banners,
                    onBannerClick = { banner ->
                      when (banner.targetSection) {
                        "ORGANIC" -> if (!filters.organicOnly) viewModel.toggleOrganicOnly()
                        "ORDERS" -> selectedTab = 2
                        "BULK" -> viewModel.selectCategory(ProductCategory.GRAINS)
                        else -> viewModel.selectCategory(null)
                      }
                    },
                  )
                }
                item {
                  OutlinedTextField(
                    value = filters.searchQuery,
                    onValueChange = viewModel::updateSearch,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Search crops, farmers, or districts...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("marketplace_search_input"),
                  )
                }
            item {
              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                FilterChip(
                  selected = filters.category == null,
                  onClick = { viewModel.selectCategory(null) },
                  label = { Text("All Produce") },
                )
                FilterChip(
                  selected = filters.organicOnly,
                  onClick = viewModel::toggleOrganicOnly,
                  leadingIcon = { Icon(Icons.Default.Eco, contentDescription = null, modifier = Modifier.size(16.dp)) },
                  label = { Text("Organic") },
                )
                ProductCategory.entries.forEach { cat ->
                  FilterChip(
                    selected = filters.category == cat,
                    onClick = { viewModel.selectCategory(if (filters.category == cat) null else cat) },
                    label = { Text(cat.label) },
                  )
                }
              }
            }
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text("Max Price: ${formatInr(filters.maxPrice)}", style = MaterialTheme.typography.labelMedium)
                Slider(
                  value = filters.maxPrice.toFloat(),
                  onValueChange = { viewModel.updateMaxPrice(it.toDouble()) },
                  valueRange = 50f..1500f,
                  modifier = Modifier.width(200.dp),
                )
              }
            }

            when (val state = filteredProductsState) {
              is UiState.Loading ->
                item {
                  Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ForestGreen)
                  }
                }
              is UiState.Error ->
                item { EmptyStateCard("Could not load produce", state.message) }
              is UiState.Success -> {
                if (state.data.isEmpty()) {
                  item { EmptyStateCard("No matching produce", "Try clearing your search or price filter.") }
                } else {
                  items(state.data, key = { it.id }) { product ->
                    val saved = wishlistIds.contains(product.id)
                    ProductMarketplaceCard(
                      product = product,
                      wishlisted = saved,
                      onOpenDetails = { selectedProduct = product },
                      onToggleWishlist = { viewModel.toggleWishlist(product, saved) },
                      onShareProduct = { ShareLinkHelper.shareProduct(context, product) },
                      onAddToCart = { viewModel.addToCart(product, 1L) },
                    )
                  }
                }
              }
            }
          }
        }

        1 -> {
          CartAndCheckoutTab(
            cartItems = cartItems,
            addresses = addresses,
            isBusy = isBusy,
            onUpdateQty = viewModel::updateCartQty,
            onAddAddress = { showAddAddressDialog = true },
            onPlaceOrder = { addr, payMethod, note, discount ->
              viewModel.placeOrder(cartItems, addr, payMethod, note, discount) {
                selectedTab = 2
              }
            },
          )
        }

        2 -> {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            item {
              Text("My Orders & Live Tracking", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
              Text(
                "Track every step from harvest packing to farm-to-door OTP delivery.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            if (allOrders.isEmpty()) {
              item { EmptyStateCard("No orders yet", "Browse fresh produce in Market and place your first order.") }
            } else {
              items(allOrders, key = { it.id }) { order ->
                CustomerOrderCard(order = order, onClick = { selectedOrder = order })
              }
            }
          }
        }

        3 -> {
          val tickets = (ticketsState as? UiState.Success)?.data.orEmpty()
          val savedWish = (wishlistState as? UiState.Success)?.data.orEmpty()
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            item {
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                  Text(profile?.fullName ?: "Customer", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                  Text(profile?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text("${profile?.city ?: ""}, ${profile?.district ?: ""}", style = MaterialTheme.typography.labelMedium, color = EarthBrown)
                  Spacer(Modifier.height(12.dp))
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onEditRoleProfile, modifier = Modifier.weight(1f)) {
                      Text("Edit Role Profile")
                    }
                    Button(
                      onClick = onSignOut,
                      colors = ButtonDefaults.buttonColors(containerColor = TerracottaAlert),
                      modifier = Modifier.weight(1f).testTag("sign_out_button"),
                    ) {
                      Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(6.dp))
                      Text("Sign Out")
                    }
                  }
                }
              }
            }

            item {
              var linkCopied by remember { mutableStateOf(false) }
              val appShareUrl = ShareLinkHelper.appBaseUrl(context)
              Card(
                colors = CardDefaults.cardColors(containerColor = SoftMintTint.copy(alpha = 0.55f)),
                border = BorderStroke(1.dp, FreshLeafGreen),
              ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Text("Share VILAIGUARD Marketplace", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                  Text(appShareUrl, style = MaterialTheme.typography.labelSmall, color = EarthBrown)
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                      onClick = {
                        ShareLinkHelper.copyToClipboard(context, "VILAIGUARD Link", appShareUrl)
                        linkCopied = true
                      },
                      modifier = Modifier.weight(1f).testTag("account_copy_link_button"),
                    ) {
                      Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(6.dp))
                      Text(if (linkCopied) "Copied!" else "Copy Link")
                    }
                    Button(
                      onClick = { ShareLinkHelper.shareApp(context) },
                      colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                      modifier = Modifier.weight(1f).testTag("account_share_app_button"),
                    ) {
                      Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(6.dp))
                      Text("Share Link")
                    }
                  }
                }
              }
            }

            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text("Saved Addresses (${addresses.size})", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { showAddAddressDialog = true }) { Text("+ Add") }
              }
            }
            items(addresses, key = { it.id }) { addr ->
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.LocationOn, contentDescription = null, tint = ForestGreen)
                  Spacer(Modifier.width(8.dp))
                  Column {
                    Text("${addr.label} • ${addr.recipientName}", fontWeight = FontWeight.SemiBold)
                    Text("${addr.streetAddress}, ${addr.city} (${addr.pinCode})", style = MaterialTheme.typography.bodyMedium)
                  }
                }
              }
            }

            item {
              Text("Wishlist (${savedWish.size})", style = MaterialTheme.typography.titleLarge)
            }
            if (savedWish.isEmpty()) {
              item { Text("Tap the heart icon on any produce item to save it here.", style = MaterialTheme.typography.bodyMedium) }
            } else {
              items(savedWish, key = { it.id }) { w ->
                Card(
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column {
                      Text(w.productName, fontWeight = FontWeight.SemiBold)
                      Text(w.farmerName, style = MaterialTheme.typography.labelSmall, color = EarthBrown)
                    }
                    Text("${formatInr(w.pricePerUnit)} / ${w.unit}", fontWeight = FontWeight.Bold, color = ForestGreen)
                  }
                }
              }
            }

            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text("Support & Feedback (${tickets.size})", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { showSupportDialog = true }) {
                  Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("New Ticket")
                }
              }
            }
            items(tickets, key = { it.id }) { ticket ->
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(ticket.subject, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    StatusPill(ticket.status, if (ticket.status == "RESOLVED") FreshLeafGreen else GoldenHarvest)
                  }
                  Text(ticket.message, style = MaterialTheme.typography.bodyMedium)
                  if (ticket.adminResponse.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Admin Reply: ${ticket.adminResponse}", style = MaterialTheme.typography.labelMedium, color = ForestGreen)
                  }
                }
              }
            }
          }
        }
      }
        }
      }
    }
  }

  // PRODUCT DETAIL BOTTOM SHEET
  selectedProduct?.let { prod ->
    val saved = wishlistIds.contains(prod.id)
    val prodReviews = (reviewsState as? UiState.Success)?.data.orEmpty().filter { it.productId == prod.id }
    var qty by remember(prod.id) { mutableLongStateOf(1L) }
    var reviewText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = { selectedProduct = null }) {
      Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        ProductArtworkBanner(prod.imageKey, prod.organic, Modifier.fillMaxWidth().height(150.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Column(modifier = Modifier.weight(1f)) {
            Text(prod.name, style = MaterialTheme.typography.headlineLarge)
            Text("Grown by ${prod.farmerName} • ${prod.farmLocation}", style = MaterialTheme.typography.bodyMedium, color = EarthBrown)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { ShareLinkHelper.shareProduct(context, prod) },
              modifier = Modifier.testTag("modal_share_product_${prod.id}"),
            ) {
              Icon(Icons.Default.Share, contentDescription = "Share product link", tint = ForestGreen)
            }
            IconButton(onClick = { viewModel.toggleWishlist(prod, saved) }) {
              Icon(
                if (saved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Wishlist",
                tint = if (saved) TerracottaAlert else ForestGreen,
              )
            }
          }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          StatusPill("${formatInr(prod.pricePerUnit)} / ${prod.unit}", ForestGreen)
          StatusPill("Stock: ${prod.stockQuantity} ${prod.unit}", if (prod.stockQuantity > 10) FreshLeafGreen else GoldenHarvest)
          StatusPill("Harvest: ${prod.harvestDate}", EarthBrown)
        }
        Text(prod.description, style = MaterialTheme.typography.bodyLarge)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Quantity:", fontWeight = FontWeight.SemiBold)
          IconButton(onClick = { if (qty > 1) qty-- }) { Icon(Icons.Default.Remove, contentDescription = "Decrease") }
          Text("$qty ${prod.unit}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          IconButton(onClick = { if (qty < prod.stockQuantity) qty++ }) { Icon(Icons.Default.Add, contentDescription = "Increase") }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedButton(
            onClick = {
              viewModel.addToCart(prod, qty)
              selectedProduct = null
            },
            modifier = Modifier.weight(1f),
          ) {
            Text("Add to Cart")
          }
          Button(
            onClick = {
              viewModel.addToCart(prod, qty)
              selectedProduct = null
              selectedTab = 1
            },
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            modifier = Modifier.weight(1f),
          ) {
            Text("Buy Now")
          }
        }

        HorizontalDivider(Modifier.padding(vertical = 6.dp))
        Text("Customer Reviews (${prodReviews.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        prodReviews.forEach { rev ->
          Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = GoldenHarvest, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("${rev.rating}/5 • ${rev.userName}", fontWeight = FontWeight.SemiBold)
              }
              Text(rev.comment, style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            placeholder = { Text("Share quality feedback...") },
            modifier = Modifier.weight(1f),
          )
          Button(
            onClick = {
              if (reviewText.isNotBlank()) {
                viewModel.submitReview(prod.id, profile?.fullName ?: "Customer", 5L, reviewText)
                reviewText = ""
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = FreshLeafGreen),
          ) { Text("Post") }
        }
        Spacer(Modifier.height(20.dp))
      }
    }
  }

  // ORDER TRACKING DETAIL SHEET
  selectedOrder?.let { ord ->
    val liveOrder = allOrders.firstOrNull { it.id == ord.id } ?: ord
    val history by viewModel.observeOrderHistory(liveOrder.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val statusStep = OrderStatus.fromRaw(liveOrder.orderStatus).stepIndex.coerceAtLeast(0)

    ModalBottomSheet(onDismissRequest = { selectedOrder = null }) {
      Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text("Order ${liveOrder.orderNumber}", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            StatusPill(OrderStatus.fromRaw(liveOrder.orderStatus).label, FreshLeafGreen)
            IconButton(
              onClick = { ShareLinkHelper.shareOrder(context, liveOrder) },
              modifier = Modifier.testTag("share_order_${liveOrder.orderNumber}"),
            ) {
              Icon(Icons.Default.Share, contentDescription = "Share order tracking link", tint = ForestGreen)
            }
          }
        }
        Text(liveOrder.itemsSummary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text("Farmer: ${liveOrder.farmerName} (${liveOrder.farmLocation})", style = MaterialTheme.typography.bodyMedium, color = EarthBrown)
        Text("Total: ${formatInr(liveOrder.totalAmount)} • ${liveOrder.paymentMethod} (${liveOrder.paymentStatus})", style = MaterialTheme.typography.bodyMedium)

        if (liveOrder.otpCode.isNotBlank()) {
          Surface(color = SoftGold, shape = RoundedCornerShape(10.dp)) {
            Text(
              "Delivery Handoff OTP: ${liveOrder.otpCode}  •  ETA: ${liveOrder.estimatedDelivery}",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = ForestGreen,
              modifier = Modifier.padding(10.dp),
            )
          }
        }
        if (liveOrder.logisticsName.isNotBlank()) {
          Text(
            "Delivery Partner: ${liveOrder.logisticsName} • ${liveOrder.logisticsVehicle}",
            style = MaterialTheme.typography.bodyMedium,
            color = ForestGreen,
          )
        }

        DemoRouteMapCard(
          pickupLabel = liveOrder.farmLocation,
          dropLabel = liveOrder.deliveryCity.ifBlank { liveOrder.deliveryDistrict },
          progressFraction = (statusStep / 7f).coerceIn(0.08f, 1f),
        )

        Text("8-Step Order Timeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OrderTimelineStepper(liveOrder.orderStatus)

        if (history.isNotEmpty()) {
          Text("Live Status Audit Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          history.forEach { h ->
            Text("• [${h.actorRole}] ${h.note}", style = MaterialTheme.typography.bodyMedium)
          }
        }
        Spacer(Modifier.height(20.dp))
      }
    }
  }

  if (showAddAddressDialog) {
    AddAddressDialog(
      defaultName = profile?.fullName ?: "",
      onDismiss = { showAddAddressDialog = false },
      onSave = { lbl, rec, ph, st, ct, dist, state, pin ->
        viewModel.saveAddress(lbl, rec, ph, st, ct, dist, state, pin) { showAddAddressDialog = false }
      },
    )
  }

  if (showSupportDialog) {
    var subj by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showSupportDialog = false },
      title = { Text("Submit Support / Quality Ticket") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = subj, onValueChange = { subj = it }, label = { Text("Subject") })
          OutlinedTextField(value = msg, onValueChange = { msg = it }, label = { Text("Describe issue or feedback") }, minLines = 3)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (subj.isNotBlank() && msg.isNotBlank()) {
              viewModel.submitSupportTicket(profile?.fullName ?: "Customer", subj, msg)
              showSupportDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
        ) { Text("Submit") }
      },
      dismissButton = { TextButton(onClick = { showSupportDialog = false }) { Text("Cancel") } },
    )
  }
}

@Composable
private fun ProductMarketplaceCard(
  product: Product,
  wishlisted: Boolean,
  onOpenDetails: () -> Unit,
  onToggleWishlist: () -> Unit,
  onShareProduct: () -> Unit,
  onAddToCart: () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenDetails).testTag("product_card_${product.id}"),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      ProductArtworkBanner(product.imageKey, product.organic, Modifier.fillMaxWidth().height(110.dp))
      Spacer(Modifier.height(10.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (product.verified) {
              Spacer(Modifier.width(4.dp))
              Icon(Icons.Default.Verified, contentDescription = "Verified Farmer", tint = FreshLeafGreen, modifier = Modifier.size(16.dp))
            }
          }
          Text("${product.farmerName} • ${product.farmLocation}", style = MaterialTheme.typography.labelSmall, color = EarthBrown)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onShareProduct,
            modifier = Modifier.testTag("share_product_${product.id}"),
          ) {
            Icon(
              Icons.Default.Share,
              contentDescription = "Share ${product.name}",
              tint = ForestGreen,
            )
          }
          IconButton(onClick = onToggleWishlist) {
            Icon(
              if (wishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Save to wishlist",
              tint = if (wishlisted) TerracottaAlert else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
      Spacer(Modifier.height(6.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
          Text("${formatInr(product.pricePerUnit)} / ${product.unit}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ForestGreen)
          Text("Harvest: ${product.harvestDate} • Stock: ${product.stockQuantity}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
          onClick = onAddToCart,
          enabled = product.stockQuantity > 0L,
          colors = ButtonDefaults.buttonColors(containerColor = FreshLeafGreen),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          modifier = Modifier.testTag("add_cart_${product.id}"),
        ) {
          Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text(if (product.stockQuantity > 0L) "Add" else "Sold Out")
        }
      }
    }
  }
}

@Composable
private fun CartAndCheckoutTab(
  cartItems: List<CartItem>,
  addresses: List<SavedAddress>,
  isBusy: Boolean,
  onUpdateQty: (String, Long) -> Unit,
  onAddAddress: () -> Unit,
  onPlaceOrder: (SavedAddress, String, String, Double) -> Unit,
) {
  var selectedAddressIdx by remember { mutableIntStateOf(0) }
  var paymentMethod by remember { mutableStateOf("Demo UPI Payment") }
  var deliveryInstructions by remember { mutableStateOf("") }
  var applyFarmDirectDiscount by remember { mutableStateOf(true) }

  val subtotal = cartItems.sumOf { it.pricePerUnit * it.quantity }
  val deliveryFee = if (subtotal == 0.0 || subtotal >= 500.0) 0.0 else 35.0
  val discount = if (applyFarmDirectDiscount && subtotal >= 200.0) subtotal * 0.05 else 0.0
  val total = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item { Text("Cart & Checkout", style = MaterialTheme.typography.headlineLarge, color = ForestGreen) }

    if (cartItems.isEmpty()) {
      item { EmptyStateCard("Your farm basket is empty", "Add fresh vegetables, grains, dairy, or spices from Market.", Icons.Default.ShoppingCart) }
    } else {
      items(cartItems, key = { it.id }) { item ->
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(item.productName, fontWeight = FontWeight.Bold)
              Text("${item.farmerName} • ${formatInr(item.pricePerUnit)}/${item.unit}", style = MaterialTheme.typography.labelSmall, color = EarthBrown)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = { onUpdateQty(item.id, item.quantity - 1) }) { Icon(Icons.Default.Remove, contentDescription = "Decrease") }
              Text("${item.quantity}", fontWeight = FontWeight.Bold)
              IconButton(onClick = { onUpdateQty(item.id, item.quantity + 1) }) { Icon(Icons.Default.Add, contentDescription = "Increase") }
            }
          }
        }
      }

      item {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text("Delivery Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          TextButton(onClick = onAddAddress) { Text("+ New Address") }
        }
        addresses.forEachIndexed { idx, addr ->
          Row(
            modifier = Modifier.fillMaxWidth().clickable { selectedAddressIdx = idx }.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            RadioButton(selected = selectedAddressIdx == idx, onClick = { selectedAddressIdx = idx })
            Column {
              Text("${addr.label} • ${addr.recipientName}", fontWeight = FontWeight.SemiBold)
              Text("${addr.streetAddress}, ${addr.city}, ${addr.district} ${addr.pinCode}", style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      }

      item {
        Text("Payment Selection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Surface(color = SoftGold.copy(alpha = 0.6f), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
          Text(stringResource(R.string.demo_payment_notice), style = MaterialTheme.typography.labelSmall, color = ForestGreen, modifier = Modifier.padding(10.dp))
        }
        listOf("Demo UPI Payment", "Demo Card Payment", "Cash on Delivery").forEach { method ->
          Row(
            modifier = Modifier.fillMaxWidth().clickable { paymentMethod = method },
            verticalAlignment = Alignment.CenterVertically,
          ) {
            RadioButton(selected = paymentMethod == method, onClick = { paymentMethod = method })
            Text(method)
          }
        }
        OutlinedTextField(
          value = deliveryInstructions,
          onValueChange = { deliveryInstructions = it },
          label = { Text("Delivery Instructions (optional)") },
          modifier = Modifier.fillMaxWidth(),
        )
      }

      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = SoftMintTint.copy(alpha = 0.55f)),
          border = BorderStroke(1.dp, FreshLeafGreen),
        ) {
          Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Subtotal")
              Text(formatInr(subtotal))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Delivery Fee (Free ≥ ₹500)")
              Text(if (deliveryFee == 0.0) "FREE" else formatInr(deliveryFee), color = FreshLeafGreen)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Direct-from-Farm Discount (5%)")
              Text("-${formatInr(discount)}", color = ForestGreen)
            }
            HorizontalDivider(Modifier.padding(vertical = 6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Total Payable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text(formatInr(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ForestGreen)
            }
            Spacer(Modifier.height(8.dp))
            val activeAddr = addresses.getOrNull(selectedAddressIdx)
            Button(
              onClick = {
                if (activeAddr != null) onPlaceOrder(activeAddr, paymentMethod, deliveryInstructions, discount)
              },
              enabled = !isBusy && activeAddr != null,
              colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("place_order_button"),
            ) {
              Text(if (isBusy) "Processing Order..." else if (activeAddr == null) "Add Address First" else "Confirm Demo Order")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CustomerOrderCard(order: Order, onClick: () -> Unit) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag("order_card_${order.orderNumber}"),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(order.orderNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
        StatusPill(
          OrderStatus.fromRaw(order.orderStatus).label,
          if (order.orderStatus == OrderStatus.CANCELLED.name) TerracottaAlert else FreshLeafGreen,
        )
      }
      Text(order.itemsSummary, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("From ${order.farmerName}", style = MaterialTheme.typography.labelSmall, color = EarthBrown)
        Text(formatInr(order.totalAmount), fontWeight = FontWeight.Bold, color = ForestGreen)
      }
      if (order.logisticsName.isNotBlank()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.LocalShipping, contentDescription = null, tint = DeepAmber, modifier = Modifier.size(14.dp))
          Spacer(Modifier.width(4.dp))
          Text("Carrier: ${order.logisticsName} • Tap to track timeline", style = MaterialTheme.typography.labelSmall, color = DeepAmber, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

@Composable
private fun AddAddressDialog(
  defaultName: String,
  onDismiss: () -> Unit,
  onSave: (String, String, String, String, String, String, String, String) -> Unit,
) {
  var label by remember { mutableStateOf("Home") }
  var name by remember { mutableStateOf(defaultName) }
  var phone by remember { mutableStateOf("+91 98230 45120") }
  var street by remember { mutableStateOf("") }
  var city by remember { mutableStateOf("Pune") }
  var district by remember { mutableStateOf("Pune") }
  var state by remember { mutableStateOf("Maharashtra") }
  var pin by remember { mutableStateOf("411045") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Delivery Address") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label (Home / Restaurant)") })
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Recipient Name") })
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
        OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street Address") })
        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") })
        OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") })
        OutlinedTextField(value = pin, onValueChange = { pin = it }, label = { Text("PIN Code") })
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (street.isNotBlank() && name.isNotBlank()) onSave(label, name, phone, street, city, district, state, pin)
        },
        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
      ) { Text("Save") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}
