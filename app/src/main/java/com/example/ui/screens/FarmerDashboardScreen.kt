package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.model.FarmerRecord
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.model.Product
import com.example.domain.model.ProductCategory
import com.example.domain.model.ProductUnit
import com.example.domain.model.UiState
import com.example.domain.model.UserProfile
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import com.example.ui.components.AnimatedHarvestFieldBanner
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MetricStatCard
import com.example.ui.components.PromotionalBannerSlider
import com.example.ui.components.SimpleBarChart
import com.example.ui.components.StatusPill
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
import com.example.ui.theme.SoftSage
import com.example.ui.theme.SoftTerracotta
import com.example.ui.theme.TerracottaAlert
import com.example.ui.viewmodel.VilaiguardViewModel

@Composable
fun FarmerDashboardScreen(
  viewModel: VilaiguardViewModel,
  profile: UserProfile?,
  onEditRoleProfile: () -> Unit,
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var editingProduct by remember { mutableStateOf<Product?>(null) }
  var showProductEditor by remember { mutableStateOf(false) }
  var rejectingOrder by remember { mutableStateOf<Order?>(null) }

  val productsState by viewModel.products.collectAsStateWithLifecycle()
  val ordersState by viewModel.orders.collectAsStateWithLifecycle()
  val farmersState by viewModel.farmers.collectAsStateWithLifecycle()
  val bannersState by viewModel.banners.collectAsStateWithLifecycle()

  val products = (productsState as? UiState.Success)?.data.orEmpty()
  val orders = (ordersState as? UiState.Success)?.data.orEmpty()
  val farmers = (farmersState as? UiState.Success)?.data.orEmpty()
  val banners = (bannersState as? UiState.Success)?.data.orEmpty()
  val primaryFarmer = farmers.firstOrNull()

  val pendingOrders = orders.filter { it.orderStatus == OrderStatus.ORDER_PLACED.name }
  val readyOrders = orders.filter { it.orderStatus == OrderStatus.READY_FOR_PICKUP.name }
  val completedOrders = orders.filter { it.orderStatus == OrderStatus.DELIVERED.name }
  val lowStockProducts = products.filter { it.stockQuantity <= 10L }
  val totalSales = orders.filter { it.orderStatus != OrderStatus.CANCELLED.name }.sumOf { it.subtotal }
  val pendingPayout =
    orders.filter {
      it.orderStatus != OrderStatus.CANCELLED.name && it.orderStatus != OrderStatus.DELIVERED.name
    }.sumOf { it.subtotal }

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
            icon = { Icon(Icons.Default.Inventory2, contentDescription = "Overview") },
            label = { Text("Overview") },
            colors = railColors,
          )
          NavigationRailItem(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            icon = { Icon(Icons.Default.Edit, contentDescription = "Catalog") },
            label = { Text("Products") },
            colors = railColors,
          )
          NavigationRailItem(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
            label = { Text("Orders (${pendingOrders.size})") },
            colors = railColors,
          )
          NavigationRailItem(
            selected = selectedTab == 3,
            onClick = { selectedTab = 3 },
            icon = { Icon(Icons.Default.Analytics, contentDescription = "Earnings") },
            label = { Text("Earnings") },
            colors = railColors,
          )
        }
      }

      Scaffold(
        modifier = Modifier.weight(1f),
        floatingActionButton = {
          if (selectedTab == 1) {
            FloatingActionButton(
              onClick = {
                editingProduct = null
                showProductEditor = true
              },
              containerColor = ForestGreen,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.testTag("add_product_fab"),
            ) {
              Icon(Icons.Default.Add, contentDescription = "Add farm product")
            }
          }
        },
        bottomBar = {
          if (!useRail) {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
              NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.Inventory2, contentDescription = "Overview") },
                label = { Text("Overview") },
              )
              NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Default.Edit, contentDescription = "Catalog") },
                label = { Text("Products") },
              )
              NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                label = { Text("Orders (${pendingOrders.size})") },
              )
              NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                icon = { Icon(Icons.Default.Analytics, contentDescription = "Earnings") },
                label = { Text("Earnings") },
              )
            }
          }
        },
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
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text("Farmer Command Center", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
                      Text(
                        "${primaryFarmer?.farmName ?: profile?.farmName ?: "Partner Farm"} • ${primaryFarmer?.verificationStatus ?: "PENDING"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EarthBrown,
                      )
                    }
                    OutlinedButton(onClick = onEditRoleProfile) { Text("Farm Profile") }
                  }
                }
                item {
                  AnimatedHarvestFieldBanner(
                    farmName = primaryFarmer?.farmName ?: profile?.farmName ?: "Partner Farm",
                    activeProductsCount = products.size,
                    pendingOrdersCount = pendingOrders.size,
                  )
                }
                item {
                  PromotionalBannerSlider(
                    banners = banners,
                    onBannerClick = { banner ->
                      selectedTab = if (banner.targetSection == "ORDERS") 2 else 1
                    },
                  )
                }
            item {
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricStatCard("Total Sales", formatInr(totalSales), "${orders.size} total orders", Icons.Default.MonetizationOn, ForestGreen, Modifier.weight(1f))
                MetricStatCard("Pending Orders", "${pendingOrders.size}", "${readyOrders.size} ready for pickup", Icons.Default.PendingActions, GoldenHarvest, Modifier.weight(1f))
              }
            }
            item {
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricStatCard("Active Listings", "${products.size}", "${completedOrders.size} completed", Icons.Default.CheckCircle, FreshLeafGreen, Modifier.weight(1f))
                MetricStatCard("Low Stock", "${lowStockProducts.size}", "≤ 10 units left", Icons.Default.Warning, TerracottaAlert, Modifier.weight(1f))
              }
            }
            if (lowStockProducts.isNotEmpty()) {
              item {
                Surface(color = SoftTerracotta, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Text("Low Stock Alert", fontWeight = FontWeight.Bold, color = TerracottaAlert)
                    lowStockProducts.forEach { p ->
                      Text("• ${p.name}: only ${p.stockQuantity} ${p.unit} remaining", style = MaterialTheme.typography.bodyMedium)
                    }
                  }
                }
              }
            }
            item { Text("Recent Customer Orders", style = MaterialTheme.typography.titleLarge) }
            items(orders.take(4), key = { it.id }) { order ->
              FarmerOrderCard(
                order = order,
                onAccept = { viewModel.farmerUpdateOrder(order, OrderStatus.ACCEPTED) },
                onReject = { rejectingOrder = order },
                onPrepare = { viewModel.farmerUpdateOrder(order, OrderStatus.PREPARING) },
                onReady = { viewModel.farmerUpdateOrder(order, OrderStatus.READY_FOR_PICKUP) },
              )
            }
          }
        }

        1 -> {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            item {
              Text("My Farm Catalog (${products.size})", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
              Text("Add, edit, or update harvest stock across 8 agricultural categories.", style = MaterialTheme.typography.bodyMedium)
            }
            if (products.isEmpty()) {
              item { EmptyStateCard("No products listed", "Tap + to list your first fresh harvest.") }
            } else {
              items(products, key = { it.id }) { p ->
                Card(
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(p.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                      Text("${p.category} • ${formatInr(p.pricePerUnit)}/${p.unit}", style = MaterialTheme.typography.bodyMedium, color = ForestGreen)
                      Text("Stock: ${p.stockQuantity} ${p.unit} • Harvest ${p.harvestDate}", style = MaterialTheme.typography.labelSmall, color = EarthBrown)
                    }
                    Row {
                      IconButton(onClick = {
                        editingProduct = p
                        showProductEditor = true
                      }) { Icon(Icons.Default.Edit, contentDescription = "Edit product", tint = ForestGreen) }
                      IconButton(onClick = { viewModel.deleteProduct(p.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete product", tint = TerracottaAlert)
                      }
                    }
                  }
                }
              }
            }
          }
        }

        2 -> {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            item {
              Text("Order Fulfillment Queue", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
              Text("Accept incoming orders, pack harvests, and hand off to assigned logistics.", style = MaterialTheme.typography.bodyMedium)
            }
            if (orders.isEmpty()) {
              item { EmptyStateCard("No incoming orders", "Customer orders will appear here in real time.") }
            } else {
              items(orders, key = { it.id }) { order ->
                FarmerOrderCard(
                  order = order,
                  onAccept = { viewModel.farmerUpdateOrder(order, OrderStatus.ACCEPTED) },
                  onReject = { rejectingOrder = order },
                  onPrepare = { viewModel.farmerUpdateOrder(order, OrderStatus.PREPARING) },
                  onReady = { viewModel.farmerUpdateOrder(order, OrderStatus.READY_FOR_PICKUP) },
                )
              }
            }
          }
        }

        3 -> {
          val categoryTotals =
            products.groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.pricePerUnit * 10 } }
              .entries.take(5).map { it.key.take(4) to it.value }
              .ifEmpty { listOf("VEG" to 1200.0, "FRU" to 900.0, "GRA" to 1500.0) }

          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            item {
              Text("Earnings & Smart Insights", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
              Surface(color = SoftGold.copy(alpha = 0.6f), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text(stringResource(R.string.demo_analytics_notice), style = MaterialTheme.typography.labelSmall, color = ForestGreen, modifier = Modifier.padding(10.dp))
              }
            }
            item {
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricStatCard("Settled Revenue", formatInr(totalSales - pendingPayout), "Direct to bank", Icons.Default.MonetizationOn, ForestGreen, Modifier.weight(1f))
                MetricStatCard("Pending Payout", formatInr(pendingPayout), "In active orders", Icons.Default.LocalShipping, GoldenHarvest, Modifier.weight(1f))
              }
            }
            item {
              SimpleBarChart(
                title = "Monthly Farm Revenue Trend (Demo)",
                subtitle = "Last 5 harvest cycles (INR)",
                bars = listOf("Jul" to 18400.0, "Aug" to 22100.0, "Sep" to 26800.0, "Oct" to 24500.0, "Nov" to totalSales.coerceAtLeast(15000.0)),
                barColor = ForestGreen,
              )
            }
            item {
              SimpleBarChart(
                title = "Catalog Value by Category",
                subtitle = "Active produce inventory valuation",
                bars = categoryTotals,
                barColor = GoldenHarvest,
              )
            }
            item {
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text("Seasonal Crop & Price Insights (Demo)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                  Text("• Winter demand surge expected for Organic Tomatoes & Turmeric (+18% Pune/Mumbai corridor).", style = MaterialTheme.typography.bodyMedium)
                  Text("• Direct farm price beats wholesale mandi average by 24–30% on pulses & A2 dairy.", style = MaterialTheme.typography.bodyMedium)
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

  if (showProductEditor) {
    ProductEditorBottomSheet(
      existing = editingProduct,
      farmer = primaryFarmer,
      onDismiss = { showProductEditor = false },
      onSave = { name, cat, desc, price, unit, stock, harvest, organic, imgKey ->
        viewModel.saveProduct(editingProduct?.id, primaryFarmer, name, cat, desc, price, unit, stock, harvest, organic, imgKey) {
          showProductEditor = false
        }
      },
    )
  }

  rejectingOrder?.let { ord ->
    var reason by remember { mutableStateOf("Current harvest batch sold out") }
    AlertDialog(
      onDismissRequest = { rejectingOrder = null },
      title = { Text("Reject Order ${ord.orderNumber}?") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Rejecting restores reserved stock back to your inventory automatically.")
          OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Rejection Reason") })
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.farmerUpdateOrder(ord, OrderStatus.CANCELLED, reason)
            rejectingOrder = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = TerracottaAlert),
        ) { Text("Reject & Restore Stock") }
      },
      dismissButton = { TextButton(onClick = { rejectingOrder = null }) { Text("Keep Order") } },
    )
  }
}

@Composable
private fun FarmerOrderCard(
  order: Order,
  onAccept: () -> Unit,
  onReject: () -> Unit,
  onPrepare: () -> Unit,
  onReady: () -> Unit,
) {
  val status = OrderStatus.fromRaw(order.orderStatus)
  val showFulfillmentAddress =
    status in setOf(OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP, OrderStatus.LOGISTICS_ASSIGNED)

  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("${order.orderNumber} • ${order.customerName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        StatusPill(status.label, if (status == OrderStatus.CANCELLED) TerracottaAlert else FreshLeafGreen)
      }
      Text(order.itemsSummary, style = MaterialTheme.typography.bodyMedium)
      Text("Total: ${formatInr(order.totalAmount)} • ${order.paymentStatus}", style = MaterialTheme.typography.labelMedium, color = ForestGreen)
      if (showFulfillmentAddress) {
        Text("Fulfillment Dest: ${order.deliveryCity}, ${order.deliveryDistrict} (${order.deliveryPinCode})", style = MaterialTheme.typography.labelSmall, color = EarthBrown)
      }
      if (order.logisticsName.isNotBlank()) {
        Text("Assigned Carrier: ${order.logisticsName} (${order.logisticsStatus})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = DeepAmber)
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
        when (status) {
          OrderStatus.ORDER_PLACED -> {
            Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = ForestGreen), modifier = Modifier.weight(1f).testTag("farmer_accept_${order.orderNumber}")) {
              Text("Accept")
            }
            OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) {
              Text("Reject", color = TerracottaAlert)
            }
          }
          OrderStatus.ACCEPTED -> {
            Button(onClick = onPrepare, colors = ButtonDefaults.buttonColors(containerColor = FreshLeafGreen), modifier = Modifier.weight(1f)) {
              Text("Start Preparing")
            }
            OutlinedButton(onClick = onReady, modifier = Modifier.weight(1f)) {
              Text("Ready for Pickup")
            }
          }
          OrderStatus.PREPARING -> {
            Button(
              onClick = onReady,
              colors = ButtonDefaults.buttonColors(containerColor = GoldenHarvest, contentColor = DeepPine),
              modifier = Modifier.fillMaxWidth().testTag("farmer_ready_${order.orderNumber}"),
            ) {
              Text("Mark Ready for Pickup", fontWeight = FontWeight.Bold)
            }
          }
          else -> Unit
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductEditorBottomSheet(
  existing: Product?,
  farmer: FarmerRecord?,
  onDismiss: () -> Unit,
  onSave: (String, ProductCategory, String, Double, ProductUnit, Long, String, Boolean, String) -> Unit,
) {
  var name by remember { mutableStateOf(existing?.name ?: "") }
  var category by remember { mutableStateOf(ProductCategory.fromRaw(existing?.category)) }
  var description by remember { mutableStateOf(existing?.description ?: "") }
  var priceText by remember { mutableStateOf((existing?.pricePerUnit ?: 60.0).toString()) }
  var unit by remember { mutableStateOf(ProductUnit.fromRaw(existing?.unit)) }
  var stockText by remember { mutableStateOf((existing?.stockQuantity ?: 50L).toString()) }
  var harvestDate by remember { mutableStateOf(existing?.harvestDate ?: "2026-11-25") }
  var organic by remember { mutableStateOf(existing?.organic ?: true) }
  var imageKey by remember { mutableStateOf(existing?.imageKey ?: "tomatoes") }

  ModalBottomSheet(onDismissRequest = onDismiss) {
    Column(
      modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(if (existing == null) "List New Farm Produce" else "Edit Produce", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
      Text("Farmer: ${farmer?.fullName ?: "Your Farm"}", style = MaterialTheme.typography.labelMedium, color = EarthBrown)
      OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())

      Text("Category", style = MaterialTheme.typography.labelMedium)
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ProductCategory.entries.take(4).forEach { cat ->
          FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat.label) })
        }
      }
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ProductCategory.entries.drop(4).forEach { cat ->
          FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat.label) })
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = priceText,
          onValueChange = { priceText = it },
          label = { Text("Price (INR)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
          value = stockText,
          onValueChange = { stockText = it },
          label = { Text("Stock Qty") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.weight(1f),
        )
      }

      Text("Unit", style = MaterialTheme.typography.labelMedium)
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ProductUnit.entries.forEach { u ->
          FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(u.label) })
        }
      }

      OutlinedTextField(value = harvestDate, onValueChange = { harvestDate = it }, label = { Text("Harvest Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Quality & Farm Description") }, minLines = 2, modifier = Modifier.fillMaxWidth())

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Certified / Verified Organic", fontWeight = FontWeight.Medium)
        Switch(checked = organic, onCheckedChange = { organic = it })
      }

      Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
        Button(
          onClick = {
            val price = priceText.toDoubleOrNull() ?: 0.0
            val stock = stockText.toLongOrNull() ?: 0L
            if (name.isNotBlank() && price > 0) {
              onSave(name, category, description, price, unit, stock, harvestDate, organic, imageKey)
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
          modifier = Modifier.weight(1f),
        ) { Text("Save Produce") }
      }
    }
  }
}
