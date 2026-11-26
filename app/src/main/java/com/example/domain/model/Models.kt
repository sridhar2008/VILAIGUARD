package com.example.domain.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>

  data class Success<T>(val data: T) : UiState<T>

  data class Error(val message: String) : UiState<Nothing>
}

enum class UserRole(val label: String) {
  CUSTOMER("Customer"),
  FARMER("Farmer"),
  LOGISTICS("Delivery Partner"),
  ADMIN("Administrator");

  companion object {
    fun fromRaw(raw: String?): UserRole =
      entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: CUSTOMER
  }
}

enum class VerificationStatus(val label: String) {
  PENDING("Pending Review"),
  VERIFIED("Verified"),
  REJECTED("Rejected"),
  SUSPENDED("Suspended");

  companion object {
    fun fromRaw(raw: String?): VerificationStatus =
      entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: PENDING
  }
}

enum class AccountStatus(val label: String) {
  ACTIVE("Active"),
  SUSPENDED("Suspended");

  companion object {
    fun fromRaw(raw: String?): AccountStatus =
      entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: ACTIVE
  }
}

enum class ProductCategory(val label: String) {
  VEGETABLES("Vegetables"),
  FRUITS("Fruits"),
  GRAINS("Grains"),
  PULSES("Pulses"),
  DAIRY("Dairy"),
  SEEDS("Seeds"),
  SPICES("Spices"),
  OTHER("Other Farm Products");

  companion object {
    fun fromRaw(raw: String?): ProductCategory =
      entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: OTHER
  }
}

enum class ProductUnit(val code: String, val label: String) {
  KG("kg", "kg"),
  GRAM("gram", "gram"),
  LITRE("litre", "litre"),
  PACKET("packet", "packet"),
  PIECE("piece", "piece");

  companion object {
    fun fromRaw(raw: String?): ProductUnit =
      entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: KG
  }
}

enum class OrderStatus(val label: String, val stepIndex: Int) {
  ORDER_PLACED("Order Placed", 0),
  ACCEPTED("Farmer Accepted", 1),
  PREPARING("Preparing Products", 2),
  READY_FOR_PICKUP("Ready for Pickup", 3),
  LOGISTICS_ASSIGNED("Logistics Assigned", 4),
  PICKED_UP("Picked Up From Farmer", 5),
  IN_TRANSIT("Out for Delivery", 6),
  DELIVERED("Delivered", 7),
  CANCELLED("Cancelled", -1);

  companion object {
    fun fromRaw(raw: String?): OrderStatus =
      entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: ORDER_PLACED
  }
}

enum class LogisticsStatus(val label: String) {
  UNASSIGNED("Awaiting Assignment"),
  ASSIGNED("Assigned"),
  ACCEPTED("Accepted"),
  REACHING_FARMER("Reaching Farmer"),
  ARRIVED_AT_FARM("Arrived at Farm"),
  PICKUP_CONFIRMED("Pickup Confirmed"),
  IN_TRANSIT("In Transit"),
  REACHING_CUSTOMER("Reaching Customer"),
  DELIVERED("Delivered"),
  FAILED_DELIVERY("Failed Delivery"),
  RETURNED("Returned");

  companion object {
    fun fromRaw(raw: String?): LogisticsStatus =
      entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: UNASSIGNED
  }
}

enum class PaymentStatus(val label: String) {
  PENDING("Pending"),
  DEMO_PAID("Demo Paid"),
  COD_DUE("Cash on Delivery"),
  SETTLED("Settled"),
  REFUNDED("Refunded (Demo)");

  companion object {
    fun fromRaw(raw: String?): PaymentStatus =
      entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: PENDING
  }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val fullName: String = "",
  val email: String = "",
  val phone: String = "",
  val role: String = UserRole.CUSTOMER.name,
  val activeDemoRole: String = UserRole.CUSTOMER.name,
  val village: String = "",
  val district: String = "",
  val state: String = "",
  val address: String = "",
  val city: String = "",
  val pinCode: String = "",
  val farmName: String = "",
  val farmSizeAcres: Double = 0.0,
  val farmingType: String = "",
  val bankSummary: String = "",
  val vehicleType: String = "",
  val vehicleNumber: String = "",
  val deliveryArea: String = "",
  val available: Boolean = true,
  val verificationStatus: String = VerificationStatus.PENDING.name,
  val accountStatus: String = AccountStatus.ACTIVE.name,
  val profileImageKey: String = "farmer_avatar",
  val demoSeeded: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class FarmerRecord(
  @DocumentId val id: String = "",
  val workspaceOwnerId: String = "",
  val farmerUserId: String = "",
  val fullName: String = "",
  val farmName: String = "",
  val village: String = "",
  val district: String = "",
  val state: String = "",
  val farmSizeAcres: Double = 0.0,
  val farmingType: String = "",
  val phone: String = "",
  val bankSummary: String = "",
  val verificationStatus: String = VerificationStatus.PENDING.name,
  val accountStatus: String = AccountStatus.ACTIVE.name,
  val totalSales: Double = 0.0,
  val completedOrders: Long = 0L,
  val rating: Double = 4.8,
  val documentNote: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class LogisticsPartner(
  @DocumentId val id: String = "",
  val workspaceOwnerId: String = "",
  val logisticsUserId: String = "",
  val fullName: String = "",
  val phone: String = "",
  val vehicleType: String = "",
  val vehicleNumber: String = "",
  val deliveryArea: String = "",
  val available: Boolean = true,
  val verificationStatus: String = VerificationStatus.PENDING.name,
  val accountStatus: String = AccountStatus.ACTIVE.name,
  val completedDeliveries: Long = 0L,
  val onTimeRate: Double = 96.0,
  val earningsTotal: Double = 0.0,
  val documentNote: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class Product(
  @DocumentId val id: String = "",
  val workspaceOwnerId: String = "",
  val farmerId: String = "",
  val farmerUserId: String = "",
  val farmerName: String = "",
  val farmLocation: String = "",
  val name: String = "",
  val category: String = ProductCategory.VEGETABLES.name,
  val description: String = "",
  val pricePerUnit: Double = 0.0,
  val unit: String = ProductUnit.KG.code,
  val stockQuantity: Long = 0L,
  val harvestDate: String = "",
  val organic: Boolean = false,
  val verified: Boolean = true,
  val available: Boolean = true,
  val imageKey: String = "produce_mix",
  val rating: Double = 4.7,
  val reviewCount: Long = 0L,
  val approvalStatus: String = "APPROVED",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class CartItem(
  @DocumentId val id: String = "",
  val productId: String = "",
  val productName: String = "",
  val farmerId: String = "",
  val farmerName: String = "",
  val farmLocation: String = "",
  val unit: String = ProductUnit.KG.code,
  val pricePerUnit: Double = 0.0,
  val quantity: Long = 1L,
  val imageKey: String = "produce_mix",
  val organic: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class SavedAddress(
  @DocumentId val id: String = "",
  val label: String = "Home",
  val recipientName: String = "",
  val phone: String = "",
  val streetAddress: String = "",
  val city: String = "",
  val district: String = "",
  val state: String = "",
  val pinCode: String = "",
  val defaultAddress: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class WishlistItem(
  @DocumentId val id: String = "",
  val productId: String = "",
  val productName: String = "",
  val farmerName: String = "",
  val pricePerUnit: Double = 0.0,
  val unit: String = ProductUnit.KG.code,
  val imageKey: String = "produce_mix",
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class Order(
  @DocumentId val id: String = "",
  val workspaceOwnerId: String = "",
  val orderNumber: String = "",
  val customerUserId: String = "",
  val customerName: String = "",
  val customerPhone: String = "",
  val deliveryAddress: String = "",
  val deliveryDistrict: String = "",
  val deliveryCity: String = "",
  val deliveryPinCode: String = "",
  val farmerId: String = "",
  val farmerUserId: String = "",
  val farmerName: String = "",
  val farmLocation: String = "",
  val farmerPhone: String = "",
  val logisticsId: String = "",
  val logisticsUserId: String = "",
  val logisticsName: String = "",
  val logisticsPhone: String = "",
  val logisticsVehicle: String = "",
  val itemsSummary: String = "",
  val itemsCount: Long = 1L,
  val subtotal: Double = 0.0,
  val deliveryFee: Double = 0.0,
  val discountAmount: Double = 0.0,
  val platformCommission: Double = 0.0,
  val totalAmount: Double = 0.0,
  val paymentMethod: String = "Demo UPI Payment",
  val paymentStatus: String = PaymentStatus.DEMO_PAID.name,
  val orderStatus: String = OrderStatus.ORDER_PLACED.name,
  val logisticsStatus: String = LogisticsStatus.UNASSIGNED.name,
  val rejectionReason: String = "",
  val pickupInstructions: String = "",
  val deliveryInstructions: String = "",
  val otpCode: String = "",
  val estimatedDelivery: String = "",
  val proofNote: String = "",
  val stockRestored: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class OrderItem(
  @DocumentId val id: String = "",
  val workspaceOwnerId: String = "",
  val customerUserId: String = "",
  val farmerUserId: String = "",
  val logisticsUserId: String = "",
  val productId: String = "",
  val productName: String = "",
  val unit: String = "",
  val quantity: Long = 1L,
  val unitPrice: Double = 0.0,
  val lineTotal: Double = 0.0,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class OrderHistoryEntry(
  @DocumentId val id: String = "",
  val workspaceOwnerId: String = "",
  val customerUserId: String = "",
  val farmerUserId: String = "",
  val logisticsUserId: String = "",
  val status: String = "",
  val actorRole: String = "",
  val note: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class AppNotification(
  @DocumentId val id: String = "",
  val workspaceOwnerId: String = "",
  val recipientUserId: String = "",
  val targetRole: String = "ALL",
  val orderId: String = "",
  val title: String = "",
  val message: String = "",
  val category: String = "ORDER",
  val read: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class SupportTicket(
  @DocumentId val id: String = "",
  val workspaceOwnerId: String = "",
  val userId: String = "",
  val userName: String = "",
  val orderId: String = "",
  val subject: String = "",
  val message: String = "",
  val status: String = "OPEN",
  val adminResponse: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ProductReview(
  @DocumentId val id: String = "",
  val workspaceOwnerId: String = "",
  val productId: String = "",
  val userId: String = "",
  val userName: String = "",
  val rating: Long = 5L,
  val comment: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class PromotionalBanner(
  @DocumentId val id: String = "",
  val workspaceOwnerId: String = "",
  val title: String = "",
  val subtitle: String = "",
  val ctaLabel: String = "Explore",
  val targetSection: String = "MARKET",
  val themeStyle: String = "FOREST",
  val imageKey: String = "farmland",
  val active: Boolean = true,
  val priority: Long = 1L,
  val scheduleNote: String = "All Season",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

