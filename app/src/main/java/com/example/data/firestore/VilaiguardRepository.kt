package com.example.data.firestore

import android.content.Context
import com.example.R
import com.example.domain.model.AccountStatus
import com.example.domain.model.AppNotification
import com.example.domain.model.CartItem
import com.example.domain.model.FarmerRecord
import com.example.domain.model.LogisticsPartner
import com.example.domain.model.LogisticsStatus
import com.example.domain.model.Order
import com.example.domain.model.OrderHistoryEntry
import com.example.domain.model.OrderItem
import com.example.domain.model.OrderStateMachine
import com.example.domain.model.OrderStatus
import com.example.domain.model.PaymentStatus
import com.example.domain.model.Product
import com.example.domain.model.ProductCategory
import com.example.domain.model.ProductReview
import com.example.domain.model.ProductUnit
import com.example.domain.model.SavedAddress
import com.example.domain.model.SupportTicket
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import com.example.domain.model.VerificationStatus
import com.example.domain.model.WishlistItem
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class VilaiguardRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireUserId(): String =
    auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

  private inline fun <reified T> observeCollection(
    query: Query,
    path: String,
    uid: String,
    crossinline sortSelector: (T) -> Comparable<*>?,
  ): Flow<List<T>> = flow {
    emitAll(
      query.snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(T::class.java, ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { sortSelector(it) as? Long ?: 0L }
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val ex = firestoreError ?: (error as? Exception)
          if (ex != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(ex, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  fun observeUserProfile(uid: String): Flow<UserProfile?> = flow {
    val path = "users/$uid"
    emitAll(
      db.collection("users").document(uid)
        .snapshots()
        .map { it.toObject(UserProfile::class.java, ServerTimestampBehavior.ESTIMATE) }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val ex = firestoreError ?: (error as? Exception)
          if (ex != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(ex, OperationType.GET, path)
          }
          throw error
        }
    )
  }

  fun observeFarmers(uid: String): Flow<List<FarmerRecord>> =
    observeCollection(
      db.collection("farmers").whereEqualTo("workspaceOwnerId", uid),
      "farmers",
      uid,
    ) { it.createdAt?.seconds ?: 0L }

  fun observeLogisticsPartners(uid: String): Flow<List<LogisticsPartner>> =
    observeCollection(
      db.collection("logistics_partners").whereEqualTo("workspaceOwnerId", uid),
      "logistics_partners",
      uid,
    ) { it.createdAt?.seconds ?: 0L }

  fun observeProducts(uid: String): Flow<List<Product>> =
    observeCollection(
      db.collection("products").whereEqualTo("workspaceOwnerId", uid),
      "products",
      uid,
    ) { it.createdAt?.seconds ?: 0L }

  fun observeOrders(uid: String): Flow<List<Order>> =
    observeCollection(
      db.collection("orders").whereEqualTo("workspaceOwnerId", uid),
      "orders",
      uid,
    ) { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L }

  fun observeOrderHistory(orderId: String, uid: String): Flow<List<OrderHistoryEntry>> =
    observeCollection(
      db.collection("orders").document(orderId).collection("history")
        .whereEqualTo("workspaceOwnerId", uid),
      "orders/$orderId/history",
      uid,
    ) { it.createdAt?.seconds ?: 0L }

  fun observeCart(uid: String): Flow<List<CartItem>> =
    observeCollection(
      db.collection("users").document(uid).collection("cart_items"),
      "users/$uid/cart_items",
      uid,
    ) { it.createdAt?.seconds ?: 0L }

  fun observeAddresses(uid: String): Flow<List<SavedAddress>> =
    observeCollection(
      db.collection("users").document(uid).collection("addresses"),
      "users/$uid/addresses",
      uid,
    ) { it.createdAt?.seconds ?: 0L }

  fun observeWishlist(uid: String): Flow<List<WishlistItem>> =
    observeCollection(
      db.collection("users").document(uid).collection("wishlist"),
      "users/$uid/wishlist",
      uid,
    ) { it.createdAt?.seconds ?: 0L }

  fun observeNotifications(uid: String): Flow<List<AppNotification>> =
    observeCollection(
      db.collection("notifications").whereEqualTo("workspaceOwnerId", uid),
      "notifications",
      uid,
    ) { it.createdAt?.seconds ?: 0L }

  fun observeSupportTickets(uid: String): Flow<List<SupportTicket>> =
    observeCollection(
      db.collection("support_tickets").whereEqualTo("workspaceOwnerId", uid),
      "support_tickets",
      uid,
    ) { it.createdAt?.seconds ?: 0L }

  fun observeReviews(uid: String): Flow<List<ProductReview>> =
    observeCollection(
      db.collection("reviews").whereEqualTo("workspaceOwnerId", uid),
      "reviews",
      uid,
    ) { it.createdAt?.seconds ?: 0L }

  suspend fun ensureUserAndSeedWorkspace(displayName: String, email: String): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    return try {
      DemoSeedHelper.seedWorkspaceIfEmpty(db, uid, displayName, email)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, "users/$uid")
      Result.failure(e)
    }
  }

  suspend fun switchActiveRole(role: UserRole): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    val path = "users/$uid"
    return try {
      db.document(path)
        .update(mapOf("activeDemoRole" to role.name, "updatedAt" to FieldValue.serverTimestamp()))
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun saveRoleProfile(
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
  ): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    val path = "users/$uid"
    return try {
      val safeRole = if (role == UserRole.ADMIN) UserRole.CUSTOMER else role
      val updateMap =
        mapOf(
          "fullName" to fullName.trim().ifBlank { "VILAIGUARD User" }.take(80),
          "phone" to phone.trim().take(20),
          "role" to safeRole.name,
          "activeDemoRole" to role.name,
          "village" to village.trim().take(80),
          "district" to district.trim().take(80),
          "state" to state.trim().take(80),
          "address" to address.trim().take(200),
          "city" to city.trim().take(80),
          "pinCode" to pinCode.trim().take(12),
          "farmName" to farmName.trim().take(100),
          "farmSizeAcres" to farmSizeAcres.coerceAtLeast(0.0),
          "farmingType" to farmingType.trim().take(60),
          "bankSummary" to bankSummary.trim().take(120),
          "vehicleType" to vehicleType.trim().take(60),
          "vehicleNumber" to vehicleNumber.trim().take(30),
          "deliveryArea" to deliveryArea.trim().take(100),
          "available" to available,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      db.document(path).update(updateMap).await()

      if (role == UserRole.FARMER) {
        val farmerDoc = db.collection("farmers").document("farmer_self_$uid")
        val existing = farmerDoc.get().await()
        if (existing.exists()) {
          farmerDoc.update(
            mapOf(
              "fullName" to fullName.trim().take(80),
              "farmName" to farmName.trim().ifBlank { "$fullName Farm" }.take(100),
              "village" to village.trim().take(80),
              "district" to district.trim().ifBlank { "Pune" }.take(80),
              "state" to state.trim().ifBlank { "Maharashtra" }.take(80),
              "farmSizeAcres" to farmSizeAcres.coerceAtLeast(0.0),
              "farmingType" to farmingType.trim().take(60),
              "phone" to phone.trim().take(20),
              "bankSummary" to bankSummary.trim().take(120),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          ).await()
        } else {
          farmerDoc.set(
            mapOf(
              "workspaceOwnerId" to uid,
              "farmerUserId" to uid,
              "fullName" to fullName.trim().take(80),
              "farmName" to farmName.trim().ifBlank { "$fullName Farm" }.take(100),
              "village" to village.trim().take(80),
              "district" to district.trim().ifBlank { "Pune" }.take(80),
              "state" to state.trim().ifBlank { "Maharashtra" }.take(80),
              "farmSizeAcres" to farmSizeAcres.coerceAtLeast(0.0),
              "farmingType" to farmingType.trim().take(60),
              "phone" to phone.trim().take(20),
              "bankSummary" to bankSummary.trim().take(120),
              "verificationStatus" to VerificationStatus.PENDING.name,
              "accountStatus" to AccountStatus.ACTIVE.name,
              "totalSales" to 0.0,
              "completedOrders" to 0L,
              "rating" to 4.8,
              "documentNote" to "Self-registered farmer profile",
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          ).await()
        }
      } else if (role == UserRole.LOGISTICS) {
        val partnerDoc = db.collection("logistics_partners").document("log_self_$uid")
        val existing = partnerDoc.get().await()
        if (existing.exists()) {
          partnerDoc.update(
            mapOf(
              "fullName" to fullName.trim().take(80),
              "phone" to phone.trim().take(20),
              "vehicleType" to vehicleType.trim().ifBlank { "Cargo Van" }.take(60),
              "vehicleNumber" to vehicleNumber.trim().ifBlank { "MH-12-0001" }.take(30),
              "deliveryArea" to deliveryArea.trim().ifBlank { "Local District" }.take(100),
              "available" to available,
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          ).await()
        } else {
          partnerDoc.set(
            mapOf(
              "workspaceOwnerId" to uid,
              "logisticsUserId" to uid,
              "fullName" to fullName.trim().take(80),
              "phone" to phone.trim().take(20),
              "vehicleType" to vehicleType.trim().ifBlank { "Cargo Van" }.take(60),
              "vehicleNumber" to vehicleNumber.trim().ifBlank { "MH-12-0001" }.take(30),
              "deliveryArea" to deliveryArea.trim().ifBlank { "Local District" }.take(100),
              "available" to available,
              "verificationStatus" to VerificationStatus.PENDING.name,
              "accountStatus" to AccountStatus.ACTIVE.name,
              "completedDeliveries" to 0L,
              "onTimeRate" to 100.0,
              "earningsTotal" to 0.0,
              "documentNote" to "Self-registered delivery partner",
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          ).await()
        }
      }

      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  // --- FARMER PRODUCT CRUD ---

  suspend fun saveProduct(
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
  ): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    val docRef =
      if (existingId.isNullOrBlank()) db.collection("products").document()
      else db.collection("products").document(existingId)
    return try {
      require(name.isNotBlank() && pricePerUnit > 0.0 && stockQuantity in 0L..1_000_000L) {
        "Provide a valid product name, positive price, and stock between 0 and 1,000,000."
      }
      if (existingId.isNullOrBlank()) {
        val payload =
          mapOf(
            "workspaceOwnerId" to uid,
            "farmerId" to (farmer?.id ?: "farmer_self_$uid"),
            "farmerUserId" to uid,
            "farmerName" to (farmer?.fullName ?: "Partner Farmer"),
            "farmLocation" to
              if (farmer != null) "${farmer.district}, ${farmer.state}" else "Pune, Maharashtra",
            "name" to name.trim().take(120),
            "category" to category.name,
            "description" to description.trim().take(600),
            "pricePerUnit" to pricePerUnit,
            "unit" to unit.code,
            "stockQuantity" to stockQuantity,
            "harvestDate" to harvestDate.trim().ifBlank { "2026-11-25" }.take(40),
            "organic" to organic,
            "verified" to (farmer?.verificationStatus == VerificationStatus.VERIFIED.name),
            "available" to (stockQuantity > 0L),
            "imageKey" to imageKey.take(60),
            "rating" to 4.8,
            "reviewCount" to 1L,
            "approvalStatus" to "APPROVED",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        docRef.set(payload).await()
      } else {
        docRef
          .update(
            mapOf(
              "name" to name.trim().take(120),
              "category" to category.name,
              "description" to description.trim().take(600),
              "pricePerUnit" to pricePerUnit,
              "unit" to unit.code,
              "stockQuantity" to stockQuantity,
              "harvestDate" to harvestDate.trim().take(40),
              "organic" to organic,
              "available" to (stockQuantity > 0L),
              "imageKey" to imageKey.take(60),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteProduct(productId: String): Result<Unit> {
    return try {
      requireUserId()
      db.collection("products").document(productId).delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, "products/$productId")
      Result.failure(e)
    }
  }

  // --- CART, WISHLIST, ADDRESSES ---

  suspend fun addToCart(product: Product, addQty: Long = 1L): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    val docRef = db.collection("users").document(uid).collection("cart_items").document(product.id)
    return try {
      require(product.stockQuantity >= addQty) { "Requested quantity exceeds available stock." }
      val snap = docRef.get().await()
      if (snap.exists()) {
        val currentQty = snap.getLong("quantity") ?: 0L
        val newQty = (currentQty + addQty).coerceAtMost(product.stockQuantity)
        docRef.update(
          mapOf(
            "quantity" to newQty,
            "pricePerUnit" to product.pricePerUnit,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      } else {
        docRef.set(
          mapOf(
            "productId" to product.id,
            "productName" to product.name.take(120),
            "farmerId" to product.farmerId,
            "farmerName" to product.farmerName.take(100),
            "farmLocation" to product.farmLocation.take(120),
            "unit" to product.unit,
            "pricePerUnit" to product.pricePerUnit,
            "quantity" to addQty.coerceIn(1L, product.stockQuantity.coerceAtLeast(1L)),
            "imageKey" to product.imageKey,
            "organic" to product.organic,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateCartQuantity(itemId: String, newQuantity: Long): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    val docRef = db.collection("users").document(uid).collection("cart_items").document(itemId)
    return try {
      if (newQuantity <= 0L) {
        docRef.delete().await()
      } else {
        docRef.update(
          mapOf(
            "quantity" to newQuantity.coerceAtMost(1000L),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun saveAddress(
    label: String,
    recipientName: String,
    phone: String,
    streetAddress: String,
    city: String,
    district: String,
    state: String,
    pinCode: String,
  ): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    val docRef = db.collection("users").document(uid).collection("addresses").document()
    return try {
      require(recipientName.trim().isNotEmpty()) { "Recipient name is required." }
      require(phone.trim().length >= 6) { "Enter a valid phone number (at least 6 digits)." }
      require(streetAddress.trim().length >= 3) { "Enter a complete street address." }
      require(city.trim().isNotEmpty() && district.trim().isNotEmpty()) { "City and district are required." }
      require(pinCode.trim().length >= 4) { "Enter a valid PIN code." }
      docRef.set(
        mapOf(
          "label" to label.trim().ifBlank { "Home" }.take(40),
          "recipientName" to recipientName.trim().take(80),
          "phone" to phone.trim().take(20),
          "streetAddress" to streetAddress.trim().take(200),
          "city" to city.trim().take(80),
          "district" to district.trim().take(80),
          "state" to state.trim().take(80),
          "pinCode" to pinCode.trim().take(12),
          "defaultAddress" to true,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun toggleWishlist(product: Product, currentlySaved: Boolean): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    val docRef = db.collection("users").document(uid).collection("wishlist").document(product.id)
    return try {
      if (currentlySaved) {
        docRef.delete().await()
      } else {
        docRef.set(
          mapOf(
            "productId" to product.id,
            "productName" to product.name.take(120),
            "farmerName" to product.farmerName.take(100),
            "pricePerUnit" to product.pricePerUnit,
            "unit" to product.unit,
            "imageKey" to product.imageKey,
            "createdAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  // --- TRANSACTION-SAFE ORDER PLACEMENT (PREVENTS OVERSELLING) ---

  suspend fun placeOrdersFromCart(
    cartItems: List<CartItem>,
    address: SavedAddress,
    paymentMethod: String,
    deliveryInstructions: String,
    discountAmount: Double,
  ): Result<List<String>> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    if (cartItems.isEmpty()) return Result.failure(IllegalStateException("Your cart is empty."))

    return try {
      val groupedByFarmer = cartItems.groupBy { it.farmerId }
      val createdOrderNumbers = mutableListOf<String>()

      db.runTransaction { transaction ->
        // 1. Read all product snapshots first (Firestore transaction requirement: reads before writes)
        val productSnapshots =
          cartItems.associate { item ->
            val ref = db.collection("products").document(item.productId)
            item.productId to (ref to transaction.get(ref))
          }

        // 2. Validate non-negative stock across all items
        productSnapshots.forEach { (_, pair) ->
          val (_, snap) = pair
          if (!snap.exists()) throw IllegalStateException("A product in your cart is no longer available.")
          val stock = snap.getLong("stockQuantity") ?: 0L
          val name = snap.getString("name") ?: "Product"
          val requested = cartItems.first { it.productId == snap.id }.quantity
          if (stock < requested) {
            throw IllegalStateException("Insufficient stock for $name (only $stock left).")
          }
        }

        // 3. Decrement stock safely
        productSnapshots.forEach { (productId, pair) ->
          val (ref, snap) = pair
          val stock = snap.getLong("stockQuantity") ?: 0L
          val requested = cartItems.first { it.productId == productId }.quantity
          val newStock = (stock - requested).coerceAtLeast(0L)
          transaction.update(
            ref,
            mapOf(
              "stockQuantity" to newStock,
              "available" to (newStock > 0L),
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }

        // 4. Create one connected order per farmer group
        groupedByFarmer.entries.forEachIndexed { idx, (farmerId, items) ->
          val firstSnap = productSnapshots[items.first().productId]?.second
          val farmerUserId = firstSnap?.getString("farmerUserId") ?: uid
          val farmerName = items.first().farmerName
          val farmLocation = items.first().farmLocation.ifBlank { "India Farm" }
          val subtotal = items.sumOf { it.pricePerUnit * it.quantity }
          val deliveryFee = if (subtotal >= 500.0) 0.0 else 35.0
          val appliedDiscount = if (idx == 0) discountAmount.coerceAtMost(subtotal) else 0.0
          val commission = (subtotal * 0.05)
          val total = (subtotal + deliveryFee - appliedDiscount).coerceAtLeast(0.0)
          val orderNumber = "VG-${Random.nextInt(2000, 9999)}"
          val otp = "${Random.nextInt(1000, 9999)}"
          val summary =
            items.joinToString(", ") { "${it.productName} (${it.quantity} ${it.unit})" }.take(390)

          val payStatus =
            if (paymentMethod.contains("Cash", ignoreCase = true)) PaymentStatus.COD_DUE.name
            else PaymentStatus.DEMO_PAID.name

          val orderRef = db.collection("orders").document()
          transaction.set(
            orderRef,
            mapOf(
              "workspaceOwnerId" to uid,
              "orderNumber" to orderNumber,
              "customerUserId" to uid,
              "customerName" to address.recipientName.take(80),
              "customerPhone" to address.phone.take(20),
              "deliveryAddress" to "${address.streetAddress}, ${address.city}".take(240),
              "deliveryDistrict" to address.district.take(80),
              "deliveryCity" to address.city.take(80),
              "deliveryPinCode" to address.pinCode.take(12),
              "farmerId" to farmerId,
              "farmerUserId" to farmerUserId,
              "farmerName" to farmerName,
              "farmLocation" to farmLocation,
              "farmerPhone" to "+91 98111 22334",
              "itemsSummary" to summary,
              "itemsCount" to items.size.toLong(),
              "subtotal" to subtotal,
              "deliveryFee" to deliveryFee,
              "discountAmount" to appliedDiscount,
              "platformCommission" to commission,
              "totalAmount" to total,
              "paymentMethod" to paymentMethod.take(50),
              "paymentStatus" to payStatus,
              "orderStatus" to OrderStatus.ORDER_PLACED.name,
              "logisticsStatus" to LogisticsStatus.UNASSIGNED.name,
              "pickupInstructions" to "Freshly packed at farm gate",
              "deliveryInstructions" to deliveryInstructions.trim().take(200),
              "otpCode" to otp,
              "estimatedDelivery" to "Within 24 hrs",
              "stockRestored" to false,
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )

          items.forEach { item ->
            val itemRef = orderRef.collection("items").document()
            transaction.set(
              itemRef,
              mapOf(
                "workspaceOwnerId" to uid,
                "customerUserId" to uid,
                "farmerUserId" to farmerUserId,
                "productId" to item.productId,
                "productName" to item.productName,
                "unit" to item.unit,
                "quantity" to item.quantity,
                "unitPrice" to item.pricePerUnit,
                "lineTotal" to (item.pricePerUnit * item.quantity),
                "createdAt" to FieldValue.serverTimestamp(),
              ),
            )
            // Clear cart item
            transaction.delete(
              db.collection("users").document(uid).collection("cart_items").document(item.id)
            )
          }

          val histRef = orderRef.collection("history").document()
          transaction.set(
            histRef,
            mapOf(
              "workspaceOwnerId" to uid,
              "customerUserId" to uid,
              "farmerUserId" to farmerUserId,
              "status" to OrderStatus.ORDER_PLACED.name,
              "actorRole" to UserRole.CUSTOMER.name,
              "note" to "Order $orderNumber placed ($paymentMethod).",
              "createdAt" to FieldValue.serverTimestamp(),
            ),
          )

          val notifRef = db.collection("notifications").document()
          transaction.set(
            notifRef,
            mapOf(
              "workspaceOwnerId" to uid,
              "recipientUserId" to farmerUserId,
              "targetRole" to UserRole.FARMER.name,
              "orderId" to orderRef.id,
              "title" to "New Order $orderNumber",
              "message" to "$summary ordered by ${address.recipientName}.",
              "category" to "ORDER",
              "read" to false,
              "createdAt" to FieldValue.serverTimestamp(),
            ),
          )

          createdOrderNumbers.add(orderNumber)
        }
      }.await()

      Result.success(createdOrderNumbers)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, "orders")
      Result.failure(e)
    }
  }

  // --- FARMER ORDER ACTIONS ---

  suspend fun transitionOrderByFarmer(
    order: Order,
    nextStatus: OrderStatus,
    rejectionReason: String = "",
  ): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    val current = OrderStatus.fromRaw(order.orderStatus)
    if (!OrderStateMachine.canTransitionOrder(current, nextStatus)) {
      return Result.failure(IllegalStateException("Invalid status transition from ${current.label} to ${nextStatus.label}."))
    }

    val orderRef = db.collection("orders").document(order.id)
    return try {
      if (nextStatus == OrderStatus.CANCELLED) {
        // Atomic stock restoration guarded by stockRestored flag (no double restoration)
        val itemSnaps = orderRef.collection("items")
          .whereEqualTo("workspaceOwnerId", order.workspaceOwnerId)
          .get()
          .await()
        val lineItems = itemSnaps.toObjects(OrderItem::class.java)

        db.runTransaction { tx ->
          val freshOrder = tx.get(orderRef)
          val alreadyRestored = freshOrder.getBoolean("stockRestored") ?: false
          val prodRefs =
            lineItems.map { item ->
              val ref = db.collection("products").document(item.productId)
              Triple(item, ref, tx.get(ref))
            }

          if (!alreadyRestored) {
            prodRefs.forEach { (item, ref, snap) ->
              if (snap.exists()) {
                val curStock = snap.getLong("stockQuantity") ?: 0L
                val updatedStock = curStock + item.quantity
                tx.update(
                  ref,
                  mapOf(
                    "stockQuantity" to updatedStock,
                    "available" to (updatedStock > 0L),
                    "updatedAt" to FieldValue.serverTimestamp(),
                  ),
                )
              }
            }
          }

          tx.update(
            orderRef,
            mapOf(
              "orderStatus" to OrderStatus.CANCELLED.name,
              "rejectionReason" to rejectionReason.trim().ifBlank { "Declined by farmer" }.take(200),
              "paymentStatus" to
                if (order.paymentStatus == PaymentStatus.DEMO_PAID.name) PaymentStatus.REFUNDED.name
                else order.paymentStatus,
              "stockRestored" to true,
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }.await()
      } else {
        orderRef.update(
          mapOf("orderStatus" to nextStatus.name, "updatedAt" to FieldValue.serverTimestamp())
        ).await()
      }

      // Add history & notify Customer + Admin
      orderRef.collection("history").add(
        mapOf(
          "workspaceOwnerId" to order.workspaceOwnerId,
          "customerUserId" to order.customerUserId,
          "farmerUserId" to order.farmerUserId,
          "status" to nextStatus.name,
          "actorRole" to UserRole.FARMER.name,
          "note" to
            when (nextStatus) {
              OrderStatus.ACCEPTED -> "Farmer accepted order ${order.orderNumber}."
              OrderStatus.PREPARING -> "Farmer is harvesting & packing produce."
              OrderStatus.READY_FOR_PICKUP -> "Packed and marked Ready for Pickup."
              OrderStatus.CANCELLED -> "Order rejected: ${rejectionReason.ifBlank { "Out of seasonal batch" }}."
              else -> "Status updated to ${nextStatus.label}."
            },
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()

      val targetRole =
        if (nextStatus == OrderStatus.READY_FOR_PICKUP) UserRole.ADMIN.name else UserRole.CUSTOMER.name
      db.collection("notifications").add(
        mapOf(
          "workspaceOwnerId" to order.workspaceOwnerId,
          "recipientUserId" to uid,
          "targetRole" to targetRole,
          "orderId" to order.id,
          "title" to "Order ${order.orderNumber}: ${nextStatus.label}",
          "message" to
            if (nextStatus == OrderStatus.READY_FOR_PICKUP)
              "${order.farmerName} packed order ${order.orderNumber}. Assign a delivery partner."
            else "Your order from ${order.farmerName} is now ${nextStatus.label}.",
          "category" to "ORDER",
          "read" to false,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()

      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, orderRef.path)
      Result.failure(e)
    }
  }

  // --- ADMIN ASSIGN LOGISTICS ---

  suspend fun assignLogisticsPartner(order: Order, partner: LogisticsPartner): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    val current = OrderStatus.fromRaw(order.orderStatus)
    if (current != OrderStatus.READY_FOR_PICKUP && current != OrderStatus.LOGISTICS_ASSIGNED) {
      return Result.failure(IllegalStateException("Order must be Ready for Pickup before assigning logistics."))
    }
    val orderRef = db.collection("orders").document(order.id)
    return try {
      orderRef.update(
        mapOf(
          "orderStatus" to OrderStatus.LOGISTICS_ASSIGNED.name,
          "logisticsStatus" to LogisticsStatus.ASSIGNED.name,
          "logisticsId" to partner.id,
          "logisticsUserId" to partner.logisticsUserId,
          "logisticsName" to partner.fullName,
          "logisticsPhone" to partner.phone,
          "logisticsVehicle" to "${partner.vehicleType} (${partner.vehicleNumber})".take(60),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()

      orderRef.collection("history").add(
        mapOf(
          "workspaceOwnerId" to order.workspaceOwnerId,
          "customerUserId" to order.customerUserId,
          "farmerUserId" to order.farmerUserId,
          "logisticsUserId" to partner.logisticsUserId,
          "status" to OrderStatus.LOGISTICS_ASSIGNED.name,
          "actorRole" to UserRole.ADMIN.name,
          "note" to "Admin assigned ${partner.fullName} (${partner.vehicleNumber}).",
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()

      db.collection("notifications").add(
        mapOf(
          "workspaceOwnerId" to order.workspaceOwnerId,
          "recipientUserId" to uid,
          "targetRole" to UserRole.LOGISTICS.name,
          "orderId" to order.id,
          "title" to "Delivery Task ${order.orderNumber}",
          "message" to "Pickup from ${order.farmerName} (${order.farmLocation}) → ${order.deliveryCity}.",
          "category" to "LOGISTICS",
          "read" to false,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()

      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, orderRef.path)
      Result.failure(e)
    }
  }

  // --- LOGISTICS DELIVERY WORKFLOW ---

  suspend fun advanceLogisticsWorkflow(
    order: Order,
    nextLogisticsStatus: LogisticsStatus,
    proofNote: String = "",
    enteredOtp: String = "",
  ): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    if (nextLogisticsStatus == LogisticsStatus.DELIVERED && order.otpCode.isNotBlank()) {
      if (enteredOtp.trim() != order.otpCode.trim()) {
        return Result.failure(IllegalStateException("Customer OTP mismatch. Expected ${order.otpCode} for demo verification."))
      }
    }

    val nextOrderStatus =
      when (nextLogisticsStatus) {
        LogisticsStatus.PICKUP_CONFIRMED -> OrderStatus.PICKED_UP
        LogisticsStatus.IN_TRANSIT, LogisticsStatus.REACHING_CUSTOMER -> OrderStatus.IN_TRANSIT
        LogisticsStatus.DELIVERED -> OrderStatus.DELIVERED
        else -> OrderStatus.fromRaw(order.orderStatus)
      }

    val orderRef = db.collection("orders").document(order.id)
    return try {
      val updates =
        mutableMapOf<String, Any>(
          "logisticsStatus" to nextLogisticsStatus.name,
          "orderStatus" to nextOrderStatus.name,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      if (proofNote.isNotBlank()) {
        updates["proofNote"] = proofNote.trim().take(200)
      }
      if (nextLogisticsStatus == LogisticsStatus.DELIVERED) {
        updates["paymentStatus"] = PaymentStatus.SETTLED.name
        updates["estimatedDelivery"] = "Delivered"
      }

      orderRef.update(updates).await()

      orderRef.collection("history").add(
        mapOf(
          "workspaceOwnerId" to order.workspaceOwnerId,
          "customerUserId" to order.customerUserId,
          "farmerUserId" to order.farmerUserId,
          "logisticsUserId" to order.logisticsUserId.ifBlank { uid },
          "status" to nextOrderStatus.name,
          "actorRole" to UserRole.LOGISTICS.name,
          "note" to
            when (nextLogisticsStatus) {
              LogisticsStatus.PICKUP_CONFIRMED -> "Pickup confirmed from ${order.farmerName}'s farm."
              LogisticsStatus.IN_TRANSIT -> "Produce in transit toward ${order.deliveryCity}."
              LogisticsStatus.DELIVERED -> "Delivered to customer. ${proofNote.ifBlank { "OTP verified." }}"
              else -> "Delivery partner status: ${nextLogisticsStatus.label}."
            },
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()

      val notifyTarget =
        when (nextLogisticsStatus) {
          LogisticsStatus.PICKUP_CONFIRMED -> UserRole.FARMER.name
          LogisticsStatus.DELIVERED -> "ALL"
          else -> UserRole.CUSTOMER.name
        }
      db.collection("notifications").add(
        mapOf(
          "workspaceOwnerId" to order.workspaceOwnerId,
          "recipientUserId" to uid,
          "targetRole" to notifyTarget,
          "orderId" to order.id,
          "title" to "Order ${order.orderNumber}: ${nextLogisticsStatus.label}",
          "message" to "Shipment handled by ${order.logisticsName.ifBlank { "Delivery Partner" }}.",
          "category" to "LOGISTICS",
          "read" to false,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()

      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, orderRef.path)
      Result.failure(e)
    }
  }

  // --- ADMIN MODERATION & GOVERNANCE ---

  suspend fun updateFarmerVerification(
    farmerId: String,
    verificationStatus: VerificationStatus,
    accountStatus: AccountStatus,
  ): Result<Unit> {
    return try {
      requireUserId()
      db.collection("farmers").document(farmerId)
        .update(
          mapOf(
            "verificationStatus" to verificationStatus.name,
            "accountStatus" to accountStatus.name,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, "farmers/$farmerId")
      Result.failure(e)
    }
  }

  suspend fun updateLogisticsVerification(
    partnerId: String,
    verificationStatus: VerificationStatus,
    accountStatus: AccountStatus,
  ): Result<Unit> {
    return try {
      requireUserId()
      db.collection("logistics_partners").document(partnerId)
        .update(
          mapOf(
            "verificationStatus" to verificationStatus.name,
            "accountStatus" to accountStatus.name,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, "logistics_partners/$partnerId")
      Result.failure(e)
    }
  }

  suspend fun setLogisticsAvailability(partnerId: String, available: Boolean): Result<Unit> {
    return try {
      requireUserId()
      db.collection("logistics_partners").document(partnerId)
        .update(mapOf("available" to available, "updatedAt" to FieldValue.serverTimestamp()))
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, "logistics_partners/$partnerId")
      Result.failure(e)
    }
  }

  suspend fun moderateProduct(productId: String, approvalStatus: String, available: Boolean): Result<Unit> {
    return try {
      requireUserId()
      db.collection("products").document(productId)
        .update(
          mapOf(
            "approvalStatus" to approvalStatus,
            "available" to available,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, "products/$productId")
      Result.failure(e)
    }
  }

  suspend fun submitSupportTicket(
    userName: String,
    subject: String,
    message: String,
    orderId: String = "",
  ): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    val docRef = db.collection("support_tickets").document()
    return try {
      require(subject.trim().isNotEmpty() && message.trim().isNotEmpty()) {
        "Please enter both a subject and message."
      }
      docRef.set(
        mapOf(
          "workspaceOwnerId" to uid,
          "userId" to uid,
          "userName" to userName.trim().ifBlank { "Customer" }.take(80),
          "orderId" to orderId.trim().take(128),
          "subject" to subject.trim().take(120),
          "message" to message.trim().take(600),
          "status" to "OPEN",
          "adminResponse" to "",
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun resolveSupportTicket(ticketId: String, response: String): Result<Unit> {
    return try {
      requireUserId()
      db.collection("support_tickets").document(ticketId)
        .update(
          mapOf(
            "status" to "RESOLVED",
            "adminResponse" to response.trim().take(400),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, "support_tickets/$ticketId")
      Result.failure(e)
    }
  }

  suspend fun submitReview(
    productId: String,
    userName: String,
    rating: Long,
    comment: String,
  ): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    val docRef = db.collection("reviews").document()
    return try {
      require(comment.trim().isNotEmpty()) { "Please enter a review comment." }
      docRef.set(
        mapOf(
          "workspaceOwnerId" to uid,
          "productId" to productId,
          "userId" to uid,
          "userName" to userName.trim().ifBlank { "Verified Buyer" }.take(80),
          "rating" to rating.coerceIn(1L, 5L),
          "comment" to comment.trim().take(400),
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun markNotificationRead(notificationId: String): Result<Unit> {
    return try {
      requireUserId()
      db.collection("notifications").document(notificationId)
        .update(mapOf("read" to true))
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, "notifications/$notificationId")
      Result.failure(e)
    }
  }
}
