package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import com.example.ui.theme.ForestGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleRegistrationBottomSheet(
  profile: UserProfile?,
  initialRole: UserRole,
  onDismiss: () -> Unit,
  onSave: (
    role: UserRole,
    fullName: String,
    phone: String,
    village: String,
    district: String,
    state: String,
    address: String,
    city: String,
    pinCode: String,
    farmName: String,
    farmSizeAcres: Double,
    farmingType: String,
    bankSummary: String,
    vehicleType: String,
    vehicleNumber: String,
    deliveryArea: String,
    available: Boolean,
  ) -> Unit,
) {
  var selectedRole by remember {
    mutableStateOf(if (initialRole == UserRole.ADMIN) UserRole.CUSTOMER else initialRole)
  }
  var fullName by remember { mutableStateOf(profile?.fullName ?: "") }
  var phone by remember { mutableStateOf(profile?.phone ?: "") }
  var village by remember { mutableStateOf(profile?.village ?: "") }
  var district by remember { mutableStateOf(profile?.district ?: "Pune") }
  var state by remember { mutableStateOf(profile?.state ?: "Maharashtra") }
  var address by remember { mutableStateOf(profile?.address ?: "") }
  var city by remember { mutableStateOf(profile?.city ?: "Pune") }
  var pinCode by remember { mutableStateOf(profile?.pinCode ?: "411045") }
  var farmName by remember { mutableStateOf(profile?.farmName ?: "") }
  var farmSizeText by remember { mutableStateOf((profile?.farmSizeAcres ?: 5.0).toString()) }
  var farmingType by remember { mutableStateOf(profile?.farmingType ?: "Organic Vegetables") }
  var bankSummary by remember { mutableStateOf(profile?.bankSummary ?: "SBI •••• 4821") }
  var vehicleType by remember { mutableStateOf(profile?.vehicleType ?: "Cold-Chain Van") }
  var vehicleNumber by remember { mutableStateOf(profile?.vehicleNumber ?: "MH-12-VG-4092") }
  var deliveryArea by remember { mutableStateOf(profile?.deliveryArea ?: "Pune & Nashik") }
  var available by remember { mutableStateOf(profile?.available ?: true) }

  ModalBottomSheet(onDismissRequest = onDismiss) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text("Role Profile & Registration", style = MaterialTheme.typography.headlineLarge, color = ForestGreen)
      Text(
        "Admin accounts are protected and provisioned securely — select Farmer, Customer, or Delivery Partner below.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(UserRole.CUSTOMER, UserRole.FARMER, UserRole.LOGISTICS).forEach { r ->
          FilterChip(
            selected = selectedRole == r,
            onClick = { selectedRole = r },
            label = { Text(r.label) },
          )
        }
      }

      OutlinedTextField(
        value = fullName,
        onValueChange = { fullName = it },
        label = { Text("Full Name") },
        modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
        value = phone,
        onValueChange = { phone = it },
        label = { Text("Phone Number") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth(),
      )

      when (selectedRole) {
        UserRole.FARMER -> {
          OutlinedTextField(value = farmName, onValueChange = { farmName = it }, label = { Text("Farm Name") }, modifier = Modifier.fillMaxWidth())
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("Village") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.weight(1f))
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.weight(1f))
            OutlinedTextField(
              value = farmSizeText,
              onValueChange = { farmSizeText = it },
              label = { Text("Size (Acres)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier.weight(1f),
            )
          }
          OutlinedTextField(value = farmingType, onValueChange = { farmingType = it }, label = { Text("Farming Type & Products") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = bankSummary, onValueChange = { bankSummary = it }, label = { Text("Payout Bank / UPI Reference") }, modifier = Modifier.fillMaxWidth())
        }

        UserRole.CUSTOMER -> {
          OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Delivery Street Address") }, modifier = Modifier.fillMaxWidth())
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.weight(1f))
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = pinCode, onValueChange = { pinCode = it }, label = { Text("PIN Code") }, modifier = Modifier.weight(1f))
          }
        }

        UserRole.LOGISTICS -> {
          OutlinedTextField(value = vehicleType, onValueChange = { vehicleType = it }, label = { Text("Vehicle Type") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = vehicleNumber, onValueChange = { vehicleNumber = it }, label = { Text("Vehicle Registration Number") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = deliveryArea, onValueChange = { deliveryArea = it }, label = { Text("Delivery Corridor / Area") }, modifier = Modifier.fillMaxWidth())
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Available for Pickup Tasks", fontWeight = FontWeight.Medium)
            Switch(checked = available, onCheckedChange = { available = it })
          }
        }

        UserRole.ADMIN -> Unit
      }

      Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
        Button(
          onClick = {
            onSave(
              selectedRole,
              fullName,
              phone,
              village,
              district,
              state,
              address,
              city,
              pinCode,
              farmName,
              farmSizeText.toDoubleOrNull() ?: 5.0,
              farmingType,
              bankSummary,
              vehicleType,
              vehicleNumber,
              deliveryArea,
              available,
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
          modifier = Modifier.weight(1f),
        ) {
          Text("Save Profile")
        }
      }
      Spacer(Modifier.height(8.dp))
    }
  }
}
