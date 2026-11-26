package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.model.AccountStatus
import com.example.domain.model.LogisticsPartner
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.model.SupportTicket
import com.example.domain.model.UiState
import com.example.domain.model.VerificationStatus
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MetricStatCard
import com.example.ui.components.SimpleBarChart
import com.example.ui.components.StatusPill
import com.example.ui.components.formatInr
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.FreshLeafGreen
import com.example.ui.theme.GoldenHarvest
import com.example.ui.theme.SoftGold
import com.example.ui.theme.TerracottaAlert
import com.example.ui.viewmodel.VilaiguardViewModel

@Composable
fun AdminDashboardScreen(viewModel: VilaiguardViewModel) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) }
  var assigningOrder by remember { mutableStateOf<Order?>(null) }
  var respondingTicket by remember { mutableStateOf<SupportTicket?>(null) }
  var orderSearch by remember { mutableStateOf("") }

  val stats by viewModel.stats.collectAsStateWithLifecycle()
  val ordersState by viewModel.orders.collectAsStateWithLifecycle()
  val farmersState by viewModel.farmers.collectAsStateWithLifecycle()
  val partnersState by viewModel.logisticsPartners.collectAsStateWithLifecycle()
  val productsState by viewModel.products.collectAsStateWithLifecycle()
  val ticketsState by viewModel.supportTickets.collectAsStateWithLifecycle()

  val orders = (ordersState as? UiState.Success)?.data.orEmpty()
  val farmers = (farmersState as? UiState.Success)?.data.orEmpty()
  val partners = (partnersState as? UiState.Success)?.data.orEmpty()
  val products = (productsState as? UiState.Success)?.data.orEmpty()
  val tickets = (ticketsState as? UiState.Success)?.data.orEmpty()

  Scaffold(
    bottomBar = {
      NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = { Icon(Icons.Default.Analytics, contentDescription = "Overview") },
          label = { Text("Overview") },
        )
        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = { Icon(Icons.Default.AssignmentInd, contentDescription = "Orders") },
          label = { Text("Dispatch") },
        )
        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          icon = { Icon(Icons.Default.Groups, contentDescription = "Verification") },
          label = { Text("Users") },
        )
        NavigationBarItem(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          icon = { Icon(Icons.Default.Inventory, contentDescription = "Catalog") },
          label = { Text("Catalog") },
        )
      }
    }
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      when (selectedTab) {
        0 -> {
          val districtSales =
            orders.groupBy { it.deliveryDistrict.ifBlank { "Pune" } }
              .mapValues { (_, list) -> list.sumOf { it.totalAmount } }
              .entries.take(5).map { it.key.take(6) to it.value }
              .ifEmpty { listOf("Pune" to 2400.0, "Nashik" to 1800.0) }

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
                  Text("Admin Control Center", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
                  Text("Monitor ecosystem supply chain, verifications & revenue", style = MaterialTheme.typography.bodyMedium, color = EarthBrown)
                }
                OutlinedButton(
                  onClick = {
                    val csv = viewModel.buildAdminCsvReport()
                    val shareIntent =
                      Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_SUBJECT, "VILAIGUARD Marketplace Report (CSV)")
                        putExtra(Intent.EXTRA_TEXT, csv)
                      }
                    runCatching { context.startActivity(Intent.createChooser(shareIntent, "Export CSV Report")) }
                  }
                ) {
                  Icon(Icons.Default.Download, contentDescription = "Export CSV", modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("CSV")
                }
              }
            }
            item {
              Surface(color = SoftGold.copy(alpha = 0.6f), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.demo_analytics_notice), style = MaterialTheme.typography.labelSmall, color = ForestGreen, modifier = Modifier.padding(10.dp))
              }
            }
            item {
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricStatCard("Platform GMV", formatInr(stats.totalRevenue), "Comm: ${formatInr(stats.platformCommission)}", Icons.Default.MonetizationOn, ForestGreen, Modifier.weight(1f))
                MetricStatCard("Total Orders", "${orders.size}", "${stats.pendingOrders} active • ${stats.completedOrders} done", Icons.Default.PendingActions, GoldenHarvest, Modifier.weight(1f))
              }
            }
            item {
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricStatCard("Farmers", "${farmers.size}", "${stats.pendingFarmerVerifications} pending review", Icons.Default.VerifiedUser, FreshLeafGreen, Modifier.weight(1f))
                MetricStatCard("Delivery Fleet", "${partners.size}", "${stats.pendingLogisticsVerifications} pending review", Icons.Default.LocalShipping, EarthBrown, Modifier.weight(1f))
              }
            }
            item {
              SimpleBarChart(
                title = "District-Wise Agricultural Sales (INR)",
                subtitle = "Live order totals grouped by delivery district",
                bars = districtSales,
                barColor = ForestGreen,
              )
            }
            item {
              SimpleBarChart(
                title = "Monthly Platform Revenue Growth (Demo)",
                subtitle = "Last 5 months ecosystem trend",
                bars = listOf("Jul" to 42000.0, "Aug" to 58000.0, "Sep" to 74000.0, "Oct" to 89000.0, "Nov" to stats.totalRevenue.coerceAtLeast(35000.0)),
                barColor = GoldenHarvest,
              )
            }
          }
        }

        1 -> {
          val filtered =
            orders.filter {
              orderSearch.isBlank() ||
                it.orderNumber.contains(orderSearch, ignoreCase = true) ||
                it.customerName.contains(orderSearch, ignoreCase = true) ||
                it.farmerName.contains(orderSearch, ignoreCase = true)
            }
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            item {
              Text("Orders & Logistics Dispatch", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
              Spacer(Modifier.height(8.dp))
              OutlinedTextField(
                value = orderSearch,
                onValueChange = { orderSearch = it },
                placeholder = { Text("Search order ID, farmer, or customer...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )
            }
            items(filtered, key = { it.id }) { order ->
              val status = OrderStatus.fromRaw(order.orderStatus)
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${order.orderNumber} • ${formatInr(order.totalAmount)}", fontWeight = FontWeight.Bold, color = ForestGreen)
                    StatusPill(status.label, if (status == OrderStatus.CANCELLED) TerracottaAlert else FreshLeafGreen)
                  }
                  Text("${order.customerName} ← ${order.farmerName} (${order.farmLocation})", style = MaterialTheme.typography.bodyMedium)
                  Text(order.itemsSummary, style = MaterialTheme.typography.labelSmall, color = EarthBrown)
                  if (order.logisticsName.isNotBlank()) {
                    Text("Carrier: ${order.logisticsName} (${order.logisticsStatus})", style = MaterialTheme.typography.labelMedium, color = ForestGreen)
                  }
                  if (status == OrderStatus.READY_FOR_PICKUP || status == OrderStatus.LOGISTICS_ASSIGNED) {
                    Button(
                      onClick = { assigningOrder = order },
                      colors = ButtonDefaults.buttonColors(containerColor = GoldenHarvest),
                      modifier = Modifier.fillMaxWidth().testTag("admin_assign_${order.orderNumber}"),
                    ) {
                      Text(if (order.logisticsName.isBlank()) "Assign Verified Delivery Partner" else "Reassign Delivery Partner")
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
            item { Text("Farmer Verification & Governance (${farmers.size})", style = MaterialTheme.typography.titleLarge, color = ForestGreen) }
            items(farmers, key = { it.id }) { f ->
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${f.fullName} • ${f.farmName}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    StatusPill(f.verificationStatus, if (f.verificationStatus == "VERIFIED") FreshLeafGreen else GoldenHarvest)
                  }
                  Text("${f.district}, ${f.state} • ${f.farmSizeAcres} acres • ${f.farmingType}", style = MaterialTheme.typography.bodyMedium)
                  Text("Docs: ${f.documentNote}", style = MaterialTheme.typography.labelSmall, color = EarthBrown)
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    if (f.verificationStatus != VerificationStatus.VERIFIED.name) {
                      Button(
                        onClick = { viewModel.adminSetFarmerStatus(f.id, VerificationStatus.VERIFIED, AccountStatus.ACTIVE) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.weight(1f),
                      ) { Text("Verify") }
                    }
                    if (f.accountStatus == AccountStatus.ACTIVE.name) {
                      OutlinedButton(
                        onClick = { viewModel.adminSetFarmerStatus(f.id, VerificationStatus.SUSPENDED, AccountStatus.SUSPENDED) },
                        modifier = Modifier.weight(1f),
                      ) { Text("Suspend", color = TerracottaAlert) }
                    } else {
                      Button(
                        onClick = { viewModel.adminSetFarmerStatus(f.id, VerificationStatus.VERIFIED, AccountStatus.ACTIVE) },
                        colors = ButtonDefaults.buttonColors(containerColor = FreshLeafGreen),
                        modifier = Modifier.weight(1f),
                      ) { Text("Reactivate") }
                    }
                  }
                }
              }
            }

            item {
              Spacer(Modifier.height(8.dp))
              Text("Logistics Fleet Verification (${partners.size})", style = MaterialTheme.typography.titleLarge, color = ForestGreen)
            }
            items(partners, key = { it.id }) { p ->
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${p.fullName} • ${p.vehicleNumber}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    StatusPill(p.verificationStatus, if (p.verificationStatus == "VERIFIED") FreshLeafGreen else GoldenHarvest)
                  }
                  Text("${p.vehicleType} • ${p.deliveryArea}", style = MaterialTheme.typography.bodyMedium)
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    if (p.verificationStatus != VerificationStatus.VERIFIED.name) {
                      Button(
                        onClick = { viewModel.adminSetLogisticsStatus(p.id, VerificationStatus.VERIFIED, AccountStatus.ACTIVE) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.weight(1f),
                      ) { Text("Approve Partner") }
                    }
                    OutlinedButton(
                      onClick = {
                        val nextActive = p.accountStatus != AccountStatus.ACTIVE.name
                        viewModel.adminSetLogisticsStatus(
                          p.id,
                          if (nextActive) VerificationStatus.VERIFIED else VerificationStatus.SUSPENDED,
                          if (nextActive) AccountStatus.ACTIVE else AccountStatus.SUSPENDED,
                        )
                      },
                      modifier = Modifier.weight(1f),
                    ) { Text(if (p.accountStatus == AccountStatus.ACTIVE.name) "Suspend" else "Reactivate") }
                  }
                }
              }
            }
          }
        }

        3 -> {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            item { Text("Catalog Moderation (${products.size})", style = MaterialTheme.typography.titleLarge, color = ForestGreen) }
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
                    Text(p.name, fontWeight = FontWeight.Bold)
                    Text("${p.farmerName} • ${formatInr(p.pricePerUnit)}/${p.unit} • Status: ${p.approvalStatus}", style = MaterialTheme.typography.labelSmall)
                  }
                  if (p.approvalStatus != "REMOVED") {
                    OutlinedButton(onClick = { viewModel.adminModerateProduct(p.id, "REMOVED", false) }) {
                      Text("Remove", color = TerracottaAlert)
                    }
                  } else {
                    Button(
                      onClick = { viewModel.adminModerateProduct(p.id, "APPROVED", p.stockQuantity > 0) },
                      colors = ButtonDefaults.buttonColors(containerColor = FreshLeafGreen),
                    ) { Text("Restore") }
                  }
                }
              }
            }

            item {
              Spacer(Modifier.height(8.dp))
              Text("Support Tickets (${tickets.size})", style = MaterialTheme.typography.titleLarge, color = ForestGreen)
            }
            if (tickets.isEmpty()) {
              item { EmptyStateCard("No open support tickets", "Customer and farmer disputes appear here.", Icons.Default.AdminPanelSettings) }
            } else {
              items(tickets, key = { it.id }) { t ->
                Card(
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                  Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                      Text(t.subject, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                      StatusPill(t.status, if (t.status == "RESOLVED") FreshLeafGreen else GoldenHarvest)
                    }
                    Text("${t.userName}: ${t.message}", style = MaterialTheme.typography.bodyMedium)
                    if (t.status != "RESOLVED") {
                      Button(
                        onClick = { respondingTicket = t },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                      ) { Text("Respond & Resolve") }
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

  // ASSIGN LOGISTICS PARTNER DIALOG
  assigningOrder?.let { ord ->
    val eligiblePartners = partners.filter { it.accountStatus == AccountStatus.ACTIVE.name }
    AlertDialog(
      onDismissRequest = { assigningOrder = null },
      title = { Text("Assign Carrier • ${ord.orderNumber}") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Pickup: ${ord.farmerName} (${ord.farmLocation})\nDrop: ${ord.deliveryCity}")
          eligiblePartners.forEach { partner: LogisticsPartner ->
            OutlinedButton(
              onClick = {
                viewModel.adminAssignLogistics(ord, partner)
                assigningOrder = null
              },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text("${partner.fullName} • ${partner.vehicleType} (${partner.verificationStatus})")
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = { TextButton(onClick = { assigningOrder = null }) { Text("Cancel") } },
    )
  }

  respondingTicket?.let { t ->
    var reply by remember { mutableStateOf("Standing Tuesday farm crate schedule enabled for your Baner address.") }
    AlertDialog(
      onDismissRequest = { respondingTicket = null },
      title = { Text("Resolve Ticket") },
      text = {
        OutlinedTextField(value = reply, onValueChange = { reply = it }, label = { Text("Admin Resolution Note") }, minLines = 2)
      },
      confirmButton = {
        Button(
          onClick = {
            if (reply.isNotBlank()) {
              viewModel.resolveSupportTicket(t.id, reply)
              respondingTicket = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
        ) { Text("Send & Resolve") }
      },
      dismissButton = { TextButton(onClick = { respondingTicket = null }) { Text("Cancel") } },
    )
  }
}
