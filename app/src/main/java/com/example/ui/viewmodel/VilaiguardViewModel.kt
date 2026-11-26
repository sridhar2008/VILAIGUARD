package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firestore.VilaiguardRepository
import com.example.domain.model.AccountStatus
import com.example.domain.model.AppNotification
import com.example.domain.model.CartItem
import com.example.domain.model.FarmerRecord
import com.example.domain.model.LogisticsPartner
import com.example.domain.model.LogisticsStatus
import com.example.domain.model.Order
import com.example.domain.model.OrderHistoryEntry
import com.example.domain.model.OrderStatus
import com.example.domain.model.Product
import com.example.domain.model.ProductCategory
import com.example.domain.model.ProductReview
import com.example.domain.model.ProductUnit
import com.example.domain.model.PromotionalBanner
import com.example.domain.model.SavedAddress
import com.example.domain.model.SupportTicket
import com.example.domain.model.UiState
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import com.example.domain.model.VerificationStatus
import com.example.domain.model.WishlistItem
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MarketplaceFilters(
  val searchQuery: String = "",
  val category: ProductCategory? = null,
  val organicOnly: Boolean = false,
  val inStockOnly: Boolean = true,
  val maxPrice: Double = 1500.0,
  val districtFilter: String = "",
)

data class DashboardStats(
  val totalRevenue: Double = 0.0,
  val platformCommission: Double = 0.0,
  val pendingOrders: Int = 0,
  val completedOrders: Int = 0,
  val cancelledOrders: Int = 0,
  val lowStockCount: Int = 0,
  val pendingFarmerVerifications: Int = 0,
  val pendingLogisticsVerifications: Int = 0,
)

class VilaiguardViewModel(
  private val repository: VilaiguardRepository,
  val currentUserId: String,
  private val displayName: String,
  private val email: String,
) : ViewModel() {

  private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val toastEvents = _toastEvents.asSharedFlow()

  private val _filters = MutableStateFlow(MarketplaceFilters())
  val filters: StateFlow<MarketplaceFilters> = _filters.asStateFlow()

  private val _isBusy = MutableStateFlow(false)
  val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

  private fun <T> Flow<T>.toUiState(label: String): StateFlow<UiState<T>> =
    map<T, UiState<T>> { UiState.Success(it) }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w("VilaiguardVM", "Error observing $label", rootError)
        emit(UiState.Error(rootError.message ?: "Failed to load $label"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = UiState.Loading,
      )

  val userProfile: StateFlow<UiState<UserProfile?>> =
    repository.observeUserProfile(currentUserId).toUiState("profile")

  val farmers: StateFlow<UiState<List<FarmerRecord>>> =
    repository.observeFarmers(currentUserId).toUiState("farmers")

  val logisticsPartners: StateFlow<UiState<List<LogisticsPartner>>> =
    repository.observeLogisticsPartners(currentUserId).toUiState("logistics partners")

  val products: StateFlow<UiState<List<Product>>> =
    repository.observeProducts(currentUserId).toUiState("products")

  val filteredProducts: StateFlow<UiState<List<Product>>> =
    combine(products, filters) { prodState, f ->
      when (prodState) {
        is UiState.Loading -> UiState.Loading
        is UiState.Error -> prodState
        is UiState.Success -> {
          val filtered =
            prodState.data.filter { p ->
              p.approvalStatus != "REMOVED" &&
                (f.searchQuery.isBlank() ||
                  p.name.contains(f.searchQuery, ignoreCase = true) ||
                  p.farmerName.contains(f.searchQuery, ignoreCase = true) ||
                  p.farmLocation.contains(f.searchQuery, ignoreCase = true)) &&
                (f.category == null || p.category == f.category.name) &&
                (!f.organicOnly || p.organic) &&
                (!f.inStockOnly || (p.available && p.stockQuantity > 0L)) &&
                p.pricePerUnit <= f.maxPrice &&
                (f.districtFilter.isBlank() ||
                  p.farmLocation.contains(f.districtFilter, ignoreCase = true))
            }
          UiState.Success(filtered)
        }
      }
    }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = UiState.Loading,
      )

  val orders: StateFlow<UiState<List<Order>>> =
    repository.observeOrders(currentUserId).toUiState("orders")

  val cartItems: StateFlow<UiState<List<CartItem>>> =
    repository.observeCart(currentUserId).toUiState("cart")

  val addresses: StateFlow<UiState<List<SavedAddress>>> =
    repository.observeAddresses(currentUserId).toUiState("addresses")

  val wishlist: StateFlow<UiState<List<WishlistItem>>> =
    repository.observeWishlist(currentUserId).toUiState("wishlist")

  val notifications: StateFlow<UiState<List<AppNotification>>> =
    repository.observeNotifications(currentUserId).toUiState("notifications")

  val supportTickets: StateFlow<UiState<List<SupportTicket>>> =
    repository.observeSupportTickets(currentUserId).toUiState("support tickets")

  val reviews: StateFlow<UiState<List<ProductReview>>> =
    repository.observeReviews(currentUserId).toUiState("reviews")

  val banners: StateFlow<UiState<List<PromotionalBanner>>> =
    repository.observeBanners(currentUserId).toUiState("banners")

  val stats: StateFlow<DashboardStats> =
    combine(orders, products, farmers, logisticsPartners) { ordS, prodS, farmS, logS ->
      val oList = (ordS as? UiState.Success)?.data.orEmpty()
      val pList = (prodS as? UiState.Success)?.data.orEmpty()
      val fList = (farmS as? UiState.Success)?.data.orEmpty()
      val lList = (logS as? UiState.Success)?.data.orEmpty()
      DashboardStats(
        totalRevenue = oList.filter { it.orderStatus != OrderStatus.CANCELLED.name }.sumOf { it.totalAmount },
        platformCommission = oList.filter { it.orderStatus != OrderStatus.CANCELLED.name }.sumOf { it.platformCommission },
        pendingOrders = oList.count { it.orderStatus != OrderStatus.DELIVERED.name && it.orderStatus != OrderStatus.CANCELLED.name },
        completedOrders = oList.count { it.orderStatus == OrderStatus.DELIVERED.name },
        cancelledOrders = oList.count { it.orderStatus == OrderStatus.CANCELLED.name },
        lowStockCount = pList.count { it.stockQuantity <= 10L },
        pendingFarmerVerifications = fList.count { it.verificationStatus == VerificationStatus.PENDING.name },
        pendingLogisticsVerifications = lList.count { it.verificationStatus == VerificationStatus.PENDING.name },
      )
    }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DashboardStats(),
      )

  init {
    viewModelScope.launch {
      repository.ensureUserAndSeedWorkspace(displayName, email)
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Workspace initialization notice") }
    }
  }

  fun observeOrderHistory(orderId: String): Flow<List<OrderHistoryEntry>> =
    repository.observeOrderHistory(orderId, currentUserId)
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        emit(emptyList())
      }

  fun updateSearch(query: String) {
    _filters.value = _filters.value.copy(searchQuery = query)
  }

  fun selectCategory(category: ProductCategory?) {
    _filters.value = _filters.value.copy(category = category)
  }

  fun toggleOrganicOnly() {
    _filters.value = _filters.value.copy(organicOnly = !_filters.value.organicOnly)
  }

  fun updateMaxPrice(maxPrice: Double) {
    _filters.value = _filters.value.copy(maxPrice = maxPrice)
  }

  fun updateDistrictFilter(district: String) {
    _filters.value = _filters.value.copy(districtFilter = district)
  }

  fun switchRole(role: UserRole) {
    viewModelScope.launch {
      repository.switchActiveRole(role)
        .onSuccess { _toastEvents.tryEmit("Switched to ${role.label} Dashboard") }
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Failed to switch role") }
    }
  }

  fun saveRoleProfile(
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
    onDone: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      repository.saveRoleProfile(
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
        farmSizeAcres = farmSizeAcres,
        farmingType = farmingType,
        bankSummary = bankSummary,
        vehicleType = vehicleType,
        vehicleNumber = vehicleNumber,
        deliveryArea = deliveryArea,
        available = available,
      ).onSuccess {
        _toastEvents.tryEmit("${role.label} profile saved")
        onDone()
      }.onFailure {
        _toastEvents.tryEmit(it.localizedMessage ?: "Unable to save profile")
      }
      _isBusy.value = false
    }
  }

  fun saveProduct(
    existingId: String?,
    farmer: FarmerRecord?,
    name: String,
    category: ProductCategory,
    description: String,
    pricePerUnit: Double,
    unit: ProductUnit,
    stockQuantity: Long,
    harvestDate: String,
    organic: Boolean,
    imageKey: String,
    onDone: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      repository.saveProduct(
        existingId = existingId,
        farmer = farmer,
        name = name,
        category = category,
        description = description,
        pricePerUnit = pricePerUnit,
        unit = unit,
        stockQuantity = stockQuantity,
        harvestDate = harvestDate,
        organic = organic,
        imageKey = imageKey,
      ).onSuccess {
        _toastEvents.tryEmit(if (existingId == null) "Product listed in marketplace" else "Product updated")
        onDone()
      }.onFailure {
        _toastEvents.tryEmit(it.localizedMessage ?: "Failed to save product")
      }
      _isBusy.value = false
    }
  }

  fun deleteProduct(productId: String) {
    viewModelScope.launch {
      repository.deleteProduct(productId)
        .onSuccess { _toastEvents.tryEmit("Product removed from catalog") }
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Delete failed") }
    }
  }

  fun addToCart(product: Product, qty: Long = 1L) {
    viewModelScope.launch {
      repository.addToCart(product, qty)
        .onSuccess { _toastEvents.tryEmit("Added ${product.name} to cart") }
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Unable to add to cart") }
    }
  }

  fun updateCartQty(itemId: String, newQty: Long) {
    viewModelScope.launch {
      repository.updateCartQuantity(itemId, newQty)
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Failed to update cart") }
    }
  }

  fun saveAddress(
    label: String,
    recipientName: String,
    phone: String,
    streetAddress: String,
    city: String,
    district: String,
    state: String,
    pinCode: String,
    onDone: () -> Unit = {},
  ) {
    viewModelScope.launch {
      repository.saveAddress(label, recipientName, phone, streetAddress, city, district, state, pinCode)
        .onSuccess {
          _toastEvents.tryEmit("Delivery address saved")
          onDone()
        }
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Failed to save address") }
    }
  }

  fun toggleWishlist(product: Product, saved: Boolean) {
    viewModelScope.launch {
      repository.toggleWishlist(product, saved)
        .onSuccess {
          _toastEvents.tryEmit(if (saved) "Removed from wishlist" else "Saved to wishlist")
        }
    }
  }

  fun placeOrder(
    cartItems: List<CartItem>,
    address: SavedAddress,
    paymentMethod: String,
    deliveryInstructions: String,
    discountAmount: Double,
    onSuccess: (List<String>) -> Unit,
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      repository.placeOrdersFromCart(cartItems, address, paymentMethod, deliveryInstructions, discountAmount)
        .onSuccess { numbers ->
          _toastEvents.tryEmit("Order ${numbers.joinToString(", ")} confirmed!")
          onSuccess(numbers)
        }
        .onFailure {
          _toastEvents.tryEmit(it.localizedMessage ?: "Order placement failed")
        }
      _isBusy.value = false
    }
  }

  fun farmerUpdateOrder(order: Order, nextStatus: OrderStatus, rejectionReason: String = "") {
    viewModelScope.launch {
      _isBusy.value = true
      repository.transitionOrderByFarmer(order, nextStatus, rejectionReason)
        .onSuccess { _toastEvents.tryEmit("Order ${order.orderNumber} → ${nextStatus.label}") }
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Status update rejected") }
      _isBusy.value = false
    }
  }

  fun adminAssignLogistics(order: Order, partner: LogisticsPartner) {
    viewModelScope.launch {
      _isBusy.value = true
      repository.assignLogisticsPartner(order, partner)
        .onSuccess { _toastEvents.tryEmit("Assigned ${partner.fullName} to ${order.orderNumber}") }
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Assignment failed") }
      _isBusy.value = false
    }
  }

  fun logisticsAdvance(order: Order, nextStatus: LogisticsStatus, proofNote: String = "", otp: String = "") {
    viewModelScope.launch {
      _isBusy.value = true
      repository.advanceLogisticsWorkflow(order, nextStatus, proofNote, otp)
        .onSuccess { _toastEvents.tryEmit("${order.orderNumber}: ${nextStatus.label}") }
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Update failed") }
      _isBusy.value = false
    }
  }

  fun adminSetFarmerStatus(farmerId: String, verification: VerificationStatus, account: AccountStatus) {
    viewModelScope.launch {
      repository.updateFarmerVerification(farmerId, verification, account)
        .onSuccess { _toastEvents.tryEmit("Farmer status updated to ${verification.label}") }
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Update failed") }
    }
  }

  fun adminSetLogisticsStatus(partnerId: String, verification: VerificationStatus, account: AccountStatus) {
    viewModelScope.launch {
      repository.updateLogisticsVerification(partnerId, verification, account)
        .onSuccess { _toastEvents.tryEmit("Logistics partner updated to ${verification.label}") }
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Update failed") }
    }
  }

  fun togglePartnerAvailability(partnerId: String, available: Boolean) {
    viewModelScope.launch {
      repository.setLogisticsAvailability(partnerId, available)
        .onSuccess {
          _toastEvents.tryEmit(if (available) "Online for pickups" else "Marked unavailable")
        }
    }
  }

  fun adminModerateProduct(productId: String, status: String, available: Boolean) {
    viewModelScope.launch {
      repository.moderateProduct(productId, status, available)
        .onSuccess { _toastEvents.tryEmit("Product moderation updated") }
    }
  }

  fun submitSupportTicket(userName: String, subject: String, message: String, orderId: String = "") {
    viewModelScope.launch {
      repository.submitSupportTicket(userName, subject, message, orderId)
        .onSuccess { _toastEvents.tryEmit("Support ticket submitted") }
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Ticket failed") }
    }
  }

  fun resolveSupportTicket(ticketId: String, response: String) {
    viewModelScope.launch {
      repository.resolveSupportTicket(ticketId, response)
        .onSuccess { _toastEvents.tryEmit("Ticket marked resolved") }
    }
  }

  fun submitReview(productId: String, userName: String, rating: Long, comment: String) {
    viewModelScope.launch {
      repository.submitReview(productId, userName, rating, comment)
        .onSuccess { _toastEvents.tryEmit("Thank you for your farm produce review!") }
    }
  }

  fun markNotificationRead(id: String) {
    viewModelScope.launch { repository.markNotificationRead(id) }
  }

  fun saveBanner(
    existingId: String?,
    title: String,
    subtitle: String,
    ctaLabel: String,
    targetSection: String,
    themeStyle: String,
    imageKey: String,
    active: Boolean,
    priority: Long,
    scheduleNote: String,
    onDone: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      repository.saveBanner(
        existingId = existingId,
        title = title,
        subtitle = subtitle,
        ctaLabel = ctaLabel,
        targetSection = targetSection,
        themeStyle = themeStyle,
        imageKey = imageKey,
        active = active,
        priority = priority,
        scheduleNote = scheduleNote,
      ).onSuccess {
        _toastEvents.tryEmit(if (existingId == null) "Promotional banner published" else "Promotional banner updated")
        onDone()
      }.onFailure {
        _toastEvents.tryEmit(it.localizedMessage ?: "Failed to save banner")
      }
      _isBusy.value = false
    }
  }

  fun toggleBannerActive(bannerId: String, active: Boolean) {
    viewModelScope.launch {
      repository.toggleBannerActive(bannerId, active)
        .onSuccess { _toastEvents.tryEmit(if (active) "Banner set Active" else "Banner hidden") }
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Failed to update banner") }
    }
  }

  fun deleteBanner(bannerId: String) {
    viewModelScope.launch {
      repository.deleteBanner(bannerId)
        .onSuccess { _toastEvents.tryEmit("Banner deleted") }
        .onFailure { _toastEvents.tryEmit(it.localizedMessage ?: "Failed to delete banner") }
    }
  }

  fun buildAdminCsvReport(): String {
    val oList = (orders.value as? UiState.Success)?.data.orEmpty()
    val header = "OrderNumber,Customer,Farmer,District,Items,Subtotal,DeliveryFee,Commission,Total,OrderStatus,LogisticsStatus,PaymentStatus\n"
    val rows =
      oList.joinToString("\n") { o ->
        "\"${o.orderNumber}\",\"${o.customerName}\",\"${o.farmerName}\",\"${o.deliveryDistrict}\",\"${o.itemsSummary.replace("\"", "'")}\",${o.subtotal},${o.deliveryFee},${o.platformCommission},${o.totalAmount},${o.orderStatus},${o.logisticsStatus},${o.paymentStatus}"
      }
    return header + rows
  }
}
