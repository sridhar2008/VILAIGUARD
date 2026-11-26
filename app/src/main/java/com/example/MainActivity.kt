package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.firestore.VilaiguardRepository
import com.example.data.firestore.attemptAutoSignIn
import com.example.data.firestore.authStateFlow
import com.example.data.firestore.signOut
import com.example.domain.model.UiState
import com.example.domain.model.UserRole
import com.example.ui.components.RoleSwitcherBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CustomerMarketplaceScreen
import com.example.ui.screens.FarmerDashboardScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.LogisticsDashboardScreen
import com.example.ui.screens.NotificationsBottomSheet
import com.example.ui.screens.RoleRegistrationBottomSheet
import com.example.ui.theme.VilaiguardTheme
import com.example.ui.viewmodel.VilaiguardViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      VilaiguardTheme {
        VilaiguardAppRoot()
      }
    }
  }
}

@Composable
fun VilaiguardAppRoot(auth: FirebaseAuth = Firebase.auth) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  var desiredIntentRole by remember { mutableStateOf<UserRole?>(null) }

  LaunchedEffect(Unit) {
    if (auth.currentUser == null) {
      attemptAutoSignIn(
        context = context,
        credentialManager = credentialManager,
        onAuthSuccess = {},
        onUnauthenticated = {},
        scope = scope,
      )
    }
  }

  val user = currentUser
  if (user == null) {
    LandingScreen(
      onSignedInWithIntent = { role -> desiredIntentRole = role }
    )
  } else {
    key(user.uid) {
      AuthenticatedVilaiguardApp(
        currentUserId = user.uid,
        displayName = user.displayName ?: "VILAIGUARD Member",
        email = user.email ?: "",
        initialIntentRole = desiredIntentRole,
        onClearIntentRole = { desiredIntentRole = null },
        onSignOut = {
          signOut(credentialManager = credentialManager, scope = scope)
        },
      )
    }
  }
}

@Composable
private fun AuthenticatedVilaiguardApp(
  currentUserId: String,
  displayName: String,
  email: String,
  initialIntentRole: UserRole?,
  onClearIntentRole: () -> Unit,
  onSignOut: () -> Unit,
) {
  val viewModel: VilaiguardViewModel =
    viewModel(
      key = currentUserId,
      factory =
        viewModelFactory {
          initializer {
            val app = checkNotNull(this[APPLICATION_KEY])
            val databaseId = app.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(databaseId)
            VilaiguardViewModel(
              repository = VilaiguardRepository(db),
              currentUserId = currentUserId,
              displayName = displayName,
              email = email,
            )
          }
        },
    )

  val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
  val notificationsState by viewModel.notifications.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  var showNotifications by remember { mutableStateOf(false) }
  var showRoleRegistration by remember { mutableStateOf(false) }

  LaunchedEffect(viewModel) {
    viewModel.toastEvents.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  LaunchedEffect(initialIntentRole, profileState) {
    if (initialIntentRole != null && profileState is UiState.Success) {
      viewModel.switchRole(initialIntentRole)
      if (initialIntentRole == UserRole.FARMER || initialIntentRole == UserRole.LOGISTICS) {
        showRoleRegistration = true
      }
      onClearIntentRole()
    }
  }

  val profile = (profileState as? UiState.Success)?.data
  val activeRole = UserRole.fromRaw(profile?.activeDemoRole)
  val notifications = (notificationsState as? UiState.Success)?.data.orEmpty()
  val unreadCount = notifications.count { !it.read }

  if (activeRole != UserRole.CUSTOMER) {
    BackHandler { viewModel.switchRole(UserRole.CUSTOMER) }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
  ) { innerPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
      Box(modifier = Modifier.statusBarsPadding()) {
        RoleSwitcherBar(
          activeRole = activeRole,
          unreadNotifications = unreadCount,
          onSelectRole = { role -> viewModel.switchRole(role) },
          onOpenNotifications = { showNotifications = true },
        )
      }

      Box(modifier = Modifier.weight(1f)) {
        when (activeRole) {
          UserRole.CUSTOMER ->
            CustomerMarketplaceScreen(
              viewModel = viewModel,
              profile = profile,
              onEditRoleProfile = { showRoleRegistration = true },
              onSignOut = onSignOut,
            )
          UserRole.FARMER ->
            FarmerDashboardScreen(
              viewModel = viewModel,
              profile = profile,
              onEditRoleProfile = { showRoleRegistration = true },
            )
          UserRole.LOGISTICS ->
            LogisticsDashboardScreen(
              viewModel = viewModel,
              onEditRoleProfile = { showRoleRegistration = true },
            )
          UserRole.ADMIN ->
            AdminDashboardScreen(viewModel = viewModel)
        }
      }
    }
  }

  if (showNotifications) {
    NotificationsBottomSheet(
      notifications = notifications,
      onMarkRead = viewModel::markNotificationRead,
      onDismiss = { showNotifications = false },
    )
  }

  if (showRoleRegistration) {
    RoleRegistrationBottomSheet(
      profile = profile,
      initialRole = activeRole,
      onDismiss = { showRoleRegistration = false },
      onSave = { role, fullName, phone, village, district, state, address, city, pinCode, farmName, farmSize, farmingType, bankSummary, vehicleType, vehicleNumber, deliveryArea, available ->
        viewModel.saveRoleProfile(
          role = role,
          fullName = fullName,
          phone = phone,
          village = village,
          district = district,
          state = state,
          address = address,
          city = city,
          pinCode = pinCode,
          farmName = farmName,
          farmSizeAcres = farmSize,
          farmingType = farmingType,
          bankSummary = bankSummary,
          vehicleType = vehicleType,
          vehicleNumber = vehicleNumber,
          deliveryArea = deliveryArea,
          available = available,
          onDone = { showRoleRegistration = false },
        )
      },
    )
  }
}
