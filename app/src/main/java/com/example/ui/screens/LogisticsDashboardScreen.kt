package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.TaskAlt
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.model.LogisticsStatus
import com.example.domain.model.Order
import com.example.domain.model.OrderStateMachine
import com.example.domain.model.OrderStatus
import com.example.domain.model.UiState
import com.example.ui.components.DemoRouteMapCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MetricStatCard
import com.example.ui.components.StatusPill
import com.example.ui.components.formatInr
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.FreshLeafGreen
import com.example.ui.theme.GoldenHarvest
import com.example.ui.theme.SoftGold
import com.example.ui.viewmodel.VilaiguardViewModel

@Composable
fun LogisticsDashboardScreen(
  viewModel: VilaiguardViewModel,
  onEditRoleProfile: () -> Unit,
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) }
  var confirmingDeliveryOrder by remember { mutableStateOf<Order?>(null) }

  val ordersState by viewModel.orders.collectAsStateWithLifecycle()
  val partnersState by viewModel.logisticsPartners.collectAsStateWithLifecycle()

  val orders = (ordersState as? UiState.Success)?.data.orEmpty()
  val partners = (partnersState as? UiState.Success)?.data.orEmpty()
  val primaryPartner = partners.firstOrNull()

  val readyPool = orders.filter { it.orderStatus == OrderStatus.READY_FOR_PICKUP.name }
  val activeTasks =
    orders.filter {
      it.orderStatus in
        setOf(OrderStatus.LOGISTICS_ASSIGNED.name, OrderStatus.PICKED_UP.name, OrderStatus.IN_TRANSIT.name)
    }
  val completedTasks = orders.filter { it.orderStatus == OrderStatus.DELIVERED.name }

  Scaffold(
    bottomBar = {
      NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = { Icon(Icons.Default.TaskAlt, contentDescription = "Tasks") },
          label = { Text("Tasks (${activeTasks.size})") },
        )
        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = { Icon(Icons.Default.Map, contentDescription = "Route Map") },
          label = { Text("Route Map") },
        )
        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          icon = { Icon(Icons.Default.History, contentDescription = "History") },
          label = { Text("History") },
        )
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
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text("Delivery Partner Hub", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
                  Text(
                    "${primaryPartner?.fullName ?: "Carrier"} • ${primaryPartner?.vehicleNumber ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EarthBrown,
                  )
                }
                OutlinedButton(onClick = onEditRoleProfile) { Text("Fleet Profile") }
              }
            }
            if (primaryPartner != null) {
              item {
                Card(
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column {
                      Text("Duty Availability", fontWeight = FontWeight.Bold)
                      Text(if (primaryPartner.available) "Online • Ready for dispatch" else "Offline", style = MaterialTheme.typography.labelSmall)
                    }
                    Switch(
                      checked = primaryPartner.available,
                      onCheckedChange = { viewModel.togglePartnerAvailability(primaryPartner.id, it) },
                    )
                  }
                }
              }
            }
            item {
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricStatCard("Active Runs", "${activeTasks.size}", "${readyPool.size} awaiting admin", Icons.Default.LocalShipping, ForestGreen, Modifier.weight(1f))
                MetricStatCard("Delivered", "${completedTasks.size}", "98% on-time demo", Icons.Default.CheckCircle, FreshLeafGreen, Modifier.weight(1f))
              }
            }

            item { Text("Assigned Pickup & Delivery Tasks", style = MaterialTheme.typography.titleLarge) }
            if (activeTasks.isEmpty()) {
              item {
                EmptyStateCard(
                  "No active shipments assigned",
                  "Once a farmer marks an order Ready for Pickup and Admin assigns a partner, tasks appear here.",
                  Icons.Default.LocalShipping,
                )
              }
            } else {
              items(activeTasks, key = { it.id }) { order ->
                val lStatus = LogisticsStatus.fromRaw(order.logisticsStatus)
                val nextStep = OrderStateMachine.nextLogisticsStep(lStatus)
                Card(
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                  Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                      Text(order.orderNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
                      StatusPill(lStatus.label, GoldenHarvest)
                    }
                    Text(order.itemsSummary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("Pickup: ${order.farmerName} (${order.farmLocation}) • ${order.farmerPhone}", style = MaterialTheme.typography.labelMedium, color = EarthBrown)
                    Text("Deliver To: ${order.customerName} (${order.deliveryAddress}, ${order.deliveryPinCode}) • ${order.customerPhone}", style = MaterialTheme.typography.labelMedium)
                    if (order.pickupInstructions.isNotBlank()) {
                      Text("Pickup Note: ${order.pickupInstructions}", style = MaterialTheme.typography.labelSmall)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                      if (nextStep != null) {
                        Button(
                          onClick = {
                            if (nextStep == LogisticsStatus.DELIVERED) {
                              confirmingDeliveryOrder = order
                            } else {
                              viewModel.logisticsAdvance(order, nextStep)
                            }
                          },
                          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                          modifier = Modifier.weight(1f).testTag("logistics_advance_${order.orderNumber}"),
                        ) {
                          Text("Mark: ${nextStep.label}")
                        }
                      }
                      OutlinedButton(
                        onClick = {
                          val dest = Uri.encode("${order.farmLocation} to ${order.deliveryAddress}")
                          val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$dest"))
                          runCatching { context.startActivity(intent) }
                        }
                      ) {
                        Icon(Icons.Default.Directions, contentDescription = "Open Maps", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Navigate")
                      }
                    }
                  }
                }
              }
            }
          }
        }

        1 -> {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            item {
              Text("Route Map & Navigation", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
              Surface(color = SoftGold.copy(alpha = 0.6f), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text(stringResource(R.string.demo_tracking_notice), style = MaterialTheme.typography.labelSmall, color = ForestGreen, modifier = Modifier.padding(10.dp))
              }
            }
            if (activeTasks.isEmpty()) {
              item { EmptyStateCard("No active route", "Assigned deliveries will render pickup-to-drop routes here.", Icons.Default.Map) }
            } else {
              items(activeTasks, key = { it.id }) { order ->
                val progress =
                  when (LogisticsStatus.fromRaw(order.logisticsStatus)) {
                    LogisticsStatus.ASSIGNED -> 0.1f
                    LogisticsStatus.ACCEPTED -> 0.2f
                    LogisticsStatus.REACHING_FARMER -> 0.35f
                    LogisticsStatus.ARRIVED_AT_FARM -> 0.45f
                    LogisticsStatus.PICKUP_CONFIRMED -> 0.55f
                    LogisticsStatus.IN_TRANSIT -> 0.75f
                    LogisticsStatus.REACHING_CUSTOMER -> 0.9f
                    else -> 1f
                  }
                DemoRouteMapCard(
                  pickupLabel = order.farmLocation,
                  dropLabel = order.deliveryCity.ifBlank { order.deliveryDistrict },
                  progressFraction = progress,
                )
              }
            }
          }
        }

        2 -> {
          val totalEarned = completedTasks.sumOf { it.deliveryFee.coerceAtLeast(35.0) } + (primaryPartner?.earningsTotal ?: 0.0)
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            item {
              Text("Delivery History & Earnings", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
              Spacer(Modifier.height(8.dp))
              MetricStatCard("Total Carrier Earnings", formatInr(totalEarned), "${completedTasks.size} completed trips", Icons.Default.MonetizationOn, ForestGreen)
            }
            if (completedTasks.isEmpty()) {
              item { EmptyStateCard("No completed deliveries yet", "Completed OTP-verified deliveries appear here.") }
            } else {
              items(completedTasks, key = { it.id }) { order ->
                Card(
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                  Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                      Text(order.orderNumber, fontWeight = FontWeight.Bold, color = ForestGreen)
                      StatusPill("Delivered", FreshLeafGreen)
                    }
                    Text("${order.farmLocation} → ${order.deliveryCity}", style = MaterialTheme.typography.bodyMedium)
                    if (order.proofNote.isNotBlank()) {
                      Text("Proof: ${order.proofNote}", style = MaterialTheme.typography.labelSmall, color = EarthBrown)
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

  confirmingDeliveryOrder?.let { ord ->
    var otpInput by remember { mutableStateOf(ord.otpCode) }
    var proofNote by remember { mutableStateOf("Delivered fresh produce crate in good condition") }
    AlertDialog(
      onDismissRequest = { confirmingDeliveryOrder = null },
      title = { Text("Confirm Delivery • ${ord.orderNumber}") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Verify customer OTP (Demo OTP: ${ord.otpCode}) and record delivery proof.")
          OutlinedTextField(value = otpInput, onValueChange = { otpInput = it }, label = { Text("Customer OTP") })
          OutlinedTextField(value = proofNote, onValueChange = { proofNote = it }, label = { Text("Delivery Proof Note") })
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.logisticsAdvance(ord, LogisticsStatus.DELIVERED, proofNote, otpInput)
            confirmingDeliveryOrder = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
        ) { Text("Complete Delivery") }
      },
      dismissButton = { TextButton(onClick = { confirmingDeliveryOrder = null }) { Text("Cancel") } },
    )
  }
}
