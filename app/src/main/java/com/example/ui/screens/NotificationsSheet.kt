package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppNotification
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.FreshLeafGreen
import com.example.ui.theme.GoldenHarvest
import com.example.ui.theme.SoftMintTint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
  notifications: List<AppNotification>,
  onMarkRead: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(onDismissRequest = onDismiss) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
      Text("Ecosystem Notifications", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
      Text("Real-time alerts across Customer, Farmer, Delivery & Admin", style = MaterialTheme.typography.bodyMedium)
      Spacer(Modifier.height(12.dp))
      if (notifications.isEmpty()) {
        EmptyStateCard("All caught up", "Order and dispatch updates appear here in real time.")
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxWidth().height(380.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(notifications, key = { it.id }) { notif ->
            Card(
              colors =
                CardDefaults.cardColors(
                  containerColor =
                    if (notif.read) MaterialTheme.colorScheme.surface
                    else SoftMintTint.copy(alpha = 0.45f)
                ),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              modifier = Modifier.fillMaxWidth().clickable { if (!notif.read) onMarkRead(notif.id) },
            ) {
              Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(notif.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                  StatusPill(notif.targetRole, if (notif.read) FreshLeafGreen else GoldenHarvest)
                }
                Spacer(Modifier.height(4.dp))
                Text(notif.message, style = MaterialTheme.typography.bodyMedium)
              }
            }
          }
        }
      }
      Spacer(Modifier.height(20.dp))
    }
  }
}
