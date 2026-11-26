package com.example

import android.content.Intent
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.components.ShareLinkHelper
import com.example.ui.components.VilaiguardDeepLink
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CustomerMarketplaceScreen
import com.example.ui.screens.FarmerDashboardScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.LogisticsDashboardScreen
import com.example.ui.screens.NotificationsBottomSheet
import com.example.ui.screens.RoleRegistrationBottomSheet
import com.example.ui.theme.PaintedIvory
import com.example.ui.theme.TerracottaAlert
import com.example.ui.theme.VilaiguardTheme
import com.example.ui.viewmodel.VilaiguardViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
  private val incomingDeepLink = mutableStateOf<VilaiguardDeepLink?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    incomingDeepLink.value = ShareLinkHelper.parseDeepLink(intent)
    setContent {
      VilaiguardTheme {
        VilaiguardAppRoot(
          pendingDeepLink = incomingDeepLink.value,
          onConsumeDeepLink = { incomingDeepLink.value = null },
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    ShareLinkHelper.parseDeepLink(intent)?.let { incomingDeepLink.value = it }
  }
}

@Composable
fun VilaiguardAppRoot(
  auth: FirebaseAuth = Firebase.auth,
  pendingDeepLink: VilaiguardDeepLink? = null,
  onConsumeDeepLink: () -> Unit = {},
) {
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
        pendingDeepLink = pendingDeepLink,
        onConsumeDeepLink = onConsumeDeepLink,
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
  pendingDeepLink: VilaiguardDeepLink?,
  onConsumeDeepLink: () -> Unit,
  onClearIntentRole: () -> Unit,
  onSignOut: () -> Unit,
) {
  val context = LocalContext.current
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
  var showLogoutConfirm by remember { mutableStateOf(false) }

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

  LaunchedEffect(pendingDeepLink, profileState) {
    if (pendingDeepLink is VilaiguardDeepLink.SwitchRole && profileState is UiState.Success) {
      viewModel.switchRole(pendingDeepLink.role)
      onConsumeDeepLink()
    } else if ((pendingDeepLink is VilaiguardDeepLink.OpenProduct || pendingDeepLink is VilaiguardDeepLink.OpenOrder) &&
      profileState is UiState.Success
    ) {
      val currentRole = UserRole.fromRaw((profileState as UiState.Success).data?.activeDemoRole)
      if (currentRole != UserRole.CUSTOMER) {
        viewModel.switchRole(UserRole.CUSTOMER)
      }
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
          onShareApp = { ShareLinkHelper.shareApp(context) },
          onRequestSignOut = { showLogoutConfirm = true },
        )
      }

      Box(modifier = Modifier.weight(1f)) {
        when (activeRole) {
          UserRole.CUSTOMER ->
            CustomerMarketplaceScreen(
              viewModel = viewModel,
              profile = profile,
              pendingDeepLink = pendingDeepLink,
              onConsumeDeepLink = onConsumeDeepLink,
              onEditRoleProfile = { showRoleRegistration = true },
              onSignOut = { showLogoutConfirm = true },
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

  if (showLogoutConfirm) {
    AlertDialog(
      onDismissRequest = { showLogoutConfirm = false },
      title = { Text("Sign out of VILAIGUARD?") },
      text = {
        Text(
          "You will return to the VILAIGUARD landing page and clear your active session. You can sign back in anytime with your Google account."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showLogoutConfirm = false
            onSignOut()
          },
          colors = ButtonDefaults.buttonColors(containerColor = TerracottaAlert, contentColor = PaintedIvory),
        ) { Text("Logout") }
      },
      dismissButton = {
        TextButton(onClick = { showLogoutConfirm = false }) { Text("Stay Signed In") }
      },
    )
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
