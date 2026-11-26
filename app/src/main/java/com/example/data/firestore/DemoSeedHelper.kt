package com.example.data.firestore

import com.example.domain.model.AccountStatus
import com.example.domain.model.LogisticsStatus
import com.example.domain.model.OrderStatus
import com.example.domain.model.PaymentStatus
import com.example.domain.model.ProductCategory
import com.example.domain.model.ProductUnit
import com.example.domain.model.UserRole
import com.example.domain.model.VerificationStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object DemoSeedHelper {

  suspend fun seedWorkspaceIfEmpty(
    db: FirebaseFirestore,
    uid: String,
    displayName: String,
    email: String,
  ) {
    val userRef = db.collection("users").document(uid)
    val existingUser = userRef.get().await()
    if (existingUser.exists()) return

    val batch = db.batch()
    val isBootstrappedAdmin = email.equals("g03608160@gmail.com", ignoreCase = true)
    val safeName = displayName.ifBlank { "Aarav Deshmukh" }.take(80)

    val userPayload =
      mapOf(
        "userId" to uid,
        "fullName" to safeName,
        "email" to email.take(120),
        "phone" to "+91 98230 45120",
        "role" to UserRole.CUSTOMER.name,
        "activeDemoRole" to UserRole.CUSTOMER.name,
        "village" to "Khed Shivapur",
        "district" to "Pune",
        "state" to "Maharashtra",
        "address" to "24 Sahyadri Residency, Baner Road",
        "city" to "Pune",
        "pinCode" to "411045",
        "farmName" to "$safeName Family Farm",
        "farmSizeAcres" to 6.5,
        "farmingType" to "Organic Mixed Crops",
        "bankSummary" to "SBI •••• 4821",
        "vehicleType" to "Mini Refrigerated Van",
        "vehicleNumber" to "MH-12-VG-4092",
        "deliveryArea" to "Pune & Nashik Corridor",
        "available" to true,
        "verificationStatus" to if (isBootstrappedAdmin) VerificationStatus.VERIFIED.name else VerificationStatus.PENDING.name,
        "accountStatus" to AccountStatus.ACTIVE.name,
        "profileImageKey" to "farmer_avatar",
        "demoSeeded" to true,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    batch.set(userRef, userPayload)

    // Farmers
    data class SeedFarmer(
      val id: String,
      val name: String,
      val farm: String,
      val village: String,
      val district: String,
      val state: String,
      val acres: Double,
      val type: String,
      val phone: String,
      val status: String,
      val sales: Double,
      val orders: Long,
      val rating: Double,
      val docNote: String,
    )

    val farmers =
      listOf(
        SeedFarmer(
          "farmer_ramesh_$uid",
          "Ramesh Patel",
          "Sahyadri Organic Acres",
          "Pimpalgaon",
          "Nashik",
          "Maharashtra",
          12.5,
          "Organic Vegetables & Grapes",
          "+91 98111 22334",
          VerificationStatus.VERIFIED.name,
          84650.0,
          42L,
          4.9,
          "7/12 Land Extract & NPOP Organic Certificate Verified",
        ),
        SeedFarmer(
          "farmer_gurpreet_$uid",
          "Gurpreet Singh Gill",
          "Sutlej Golden Harvest",
          "Jagraon",
          "Ludhiana",
          "Punjab",
          18.0,
          "Heritage Grains & Pulses",
          "+91 98760 33112",
          VerificationStatus.VERIFIED.name,
          126400.0,
          58L,
          4.8,
          "FPO Registration & Mandi Direct Permit Verified",
        ),
        SeedFarmer(
          "farmer_kavitha_$uid",
          "Kavitha Nair",
          "Malabar Spice Canopy",
          "Kalpetta",
          "Wayanad",
          "Kerala",
          7.2,
          "Single-Origin Spices & Coffee",
          "+91 94471 88210",
          VerificationStatus.VERIFIED.name,
          63200.0,
          31L,
          4.9,
          "Spices Board Grower Card Verified",
        ),
        SeedFarmer(
          "farmer_suresh_$uid",
          "Suresh Gowda",
          "Cauvery Green Dairy & Orchard",
          "Maddur",
          "Mandya",
          "Karnataka",
          9.0,
          "A2 Dairy & Tropical Fruits",
          "+91 99002 55419",
          VerificationStatus.PENDING.name,
          19400.0,
          11L,
          4.6,
          "Awaiting Aadhaar & Farm Geo-Tag Approval",
        ),
      )

    farmers.forEach { f ->
      batch.set(
        db.collection("farmers").document(f.id),
        mapOf(
          "workspaceOwnerId" to uid,
          "farmerUserId" to uid,
          "fullName" to f.name,
          "farmName" to f.farm,
          "village" to f.village,
          "district" to f.district,
          "state" to f.state,
          "farmSizeAcres" to f.acres,
          "farmingType" to f.type,
          "phone" to f.phone,
          "bankSummary" to "HDFC •••• 7710",
          "verificationStatus" to f.status,
          "accountStatus" to AccountStatus.ACTIVE.name,
          "totalSales" to f.sales,
          "completedOrders" to f.orders,
          "rating" to f.rating,
          "documentNote" to f.docNote,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
    }

    // Logistics Partners
    data class SeedLogistics(
      val id: String,
      val name: String,
      val phone: String,
      val vehicle: String,
      val plate: String,
      val area: String,
      val status: String,
      val deliveries: Long,
      val onTime: Double,
      val earnings: Double,
      val docNote: String,
    )

    val partners =
      listOf(
        SeedLogistics(
          "log_vikram_$uid",
          "Vikram Chavan",
          "+91 97650 11223",
          "Insulated Cold-Chain Van",
          "MH-15-TR-8841",
          "Nashik – Pune Express",
          VerificationStatus.VERIFIED.name,
          64L,
          98.2,
          28400.0,
          "Commercial DL & Cold-Box Inspection Verified",
        ),
        SeedLogistics(
          "log_arun_$uid",
          "Arun Kulkarni",
          "+91 98450 99812",
          "Electric Cargo 3-Wheeler",
          "MH-12-EV-3109",
          "Pune Metro & Baner",
          VerificationStatus.VERIFIED.name,
          49L,
          96.5,
          19850.0,
          "EV Commercial Permit Verified",
        ),
        SeedLogistics(
          "log_priya_$uid",
          "Deepak Yadav",
          "+91 98901 66741",
          "Tata Ace Farm Pickup",
          "PB-10-AG-5520",
          "Ludhiana – Chandigarh",
          VerificationStatus.PENDING.name,
          14L,
          94.0,
          7200.0,
          "RC Book Submitted – Pending Verification",
        ),
      )

    partners.forEach { p ->
      batch.set(
        db.collection("logistics_partners").document(p.id),
        mapOf(
          "workspaceOwnerId" to uid,
          "logisticsUserId" to uid,
          "fullName" to p.name,
          "phone" to p.phone,
          "vehicleType" to p.vehicle,
          "vehicleNumber" to p.plate,
          "deliveryArea" to p.area,
          "available" to true,
          "verificationStatus" to p.status,
          "accountStatus" to AccountStatus.ACTIVE.name,
          "completedDeliveries" to p.deliveries,
          "onTimeRate" to p.onTime,
          "earningsTotal" to p.earnings,
          "documentNote" to p.docNote,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
    }

    // 10 Diverse Agricultural Products covering all 8 categories
    data class SeedProduct(
      val id: String,
      val farmerIdx: Int,
      val name: String,
      val category: ProductCategory,
      val desc: String,
      val price: Double,
      val unit: ProductUnit,
      val stock: Long,
      val harvest: String,
      val organic: Boolean,
      val imageKey: String,
      val rating: Double,
    )

    val products =
      listOf(
        SeedProduct("prod_tomato_$uid", 0, "Organic Heirloom Tomatoes", ProductCategory.VEGETABLES, "Vine-ripened Nashik desi tomatoes harvested at dawn, zero chemical wax.", 48.0, ProductUnit.KG, 140L, "2026-11-25", true, "tomatoes", 4.9),
        SeedProduct("prod_okra_$uid", 0, "Tender Green Okra (Bhindi)", ProductCategory.VEGETABLES, "Crisp morning-picked bhindi ideal for daily kitchen cooking and restaurants.", 56.0, ProductUnit.KG, 8L, "2026-11-25", true, "okra", 4.7),
        SeedProduct("prod_mango_$uid", 3, "Ratnagiri Alphonso Mangoes", ProductCategory.FRUITS, "Naturally hay-ripened GI-tagged Alphonso mangoes with rich aroma.", 420.0, ProductUnit.KG, 45L, "2026-11-24", true, "mango", 4.9),
        SeedProduct("prod_basmati_$uid", 1, "Aged Traditional Basmati Rice", ProductCategory.GRAINS, "18-month aged extra-long grain aromatic basmati directly from Punjab farms.", 135.0, ProductUnit.KG, 320L, "2026-11-18", false, "basmati", 4.8),
        SeedProduct("prod_toor_$uid", 1, "Unpolished Toor Dal (Arhar)", ProductCategory.PULSES, "Stone-milled unpolished farm toor dal rich in protein and natural sweetness.", 162.0, ProductUnit.KG, 190L, "2026-11-20", true, "pulses", 4.8),
        SeedProduct("prod_ghee_$uid", 3, "Bilona A2 Gir Cow Ghee", ProductCategory.DAIRY, "Hand-churned curd bilona ghee from grass-fed indigenous Gir cows.", 980.0, ProductUnit.LITRE, 24L, "2026-11-23", true, "dairy", 5.0),
        SeedProduct("prod_seeds_$uid", 1, "Heirloom Mustard & Millet Seeds", ProductCategory.SEEDS, "Open-pollinated untreated farm seeds suited for kitchen gardens and growers.", 110.0, ProductUnit.PACKET, 75L, "2026-11-15", true, "seeds", 4.6),
        SeedProduct("prod_turmeric_$uid", 2, "Wayanad Golden Turmeric", ProductCategory.SPICES, "High-curcumin (5.8%) sun-dried single-estate turmeric fingers and powder.", 210.0, ProductUnit.PACKET, 65L, "2026-11-21", true, "spices", 4.9),
        SeedProduct("prod_pepper_$uid", 2, "Malabar Tellicherry Black Pepper", ProductCategory.SPICES, "Bold sun-cured Tellicherry peppercorns hand-harvested in Wayanad.", 340.0, ProductUnit.GRAM, 52L, "2026-11-19", true, "spices", 4.9),
        SeedProduct("prod_honey_$uid", 2, "Wild Forest Multiflora Honey", ProductCategory.OTHER, "Raw unfiltered farm-edge forest honey bottled without heat processing.", 360.0, ProductUnit.LITRE, 30L, "2026-11-22", true, "honey", 4.8),
      )

    products.forEach { p ->
      val f = farmers[p.farmerIdx]
      batch.set(
        db.collection("products").document(p.id),
        mapOf(
          "workspaceOwnerId" to uid,
          "farmerId" to f.id,
          "farmerUserId" to uid,
          "farmerName" to f.name,
          "farmLocation" to "${f.district}, ${f.state}",
          "name" to p.name,
          "category" to p.category.name,
          "description" to p.desc,
          "pricePerUnit" to p.price,
          "unit" to p.unit.code,
          "stockQuantity" to p.stock,
          "harvestDate" to p.harvest,
          "organic" to p.organic,
          "verified" to (f.status == VerificationStatus.VERIFIED.name),
          "available" to (p.stock > 0),
          "imageKey" to p.imageKey,
          "rating" to p.rating,
          "reviewCount" to 18L,
          "approvalStatus" to "APPROVED",
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
    }

    // Saved Address
    batch.set(
      db.collection("users").document(uid).collection("addresses").document("addr_home_$uid"),
      mapOf(
        "label" to "Home",
        "recipientName" to safeName,
        "phone" to "+91 98230 45120",
        "streetAddress" to "24 Sahyadri Residency, Baner Road",
        "city" to "Pune",
        "district" to "Pune",
        "state" to "Maharashtra",
        "pinCode" to "411045",
        "defaultAddress" to true,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      ),
    )

    // Interconnected Orders across different stages of the workflow
    val order1Id = "ord_vg_1041_$uid"
    batch.set(
      db.collection("orders").document(order1Id),
      mapOf(
        "workspaceOwnerId" to uid,
        "orderNumber" to "VG-1041",
        "customerUserId" to uid,
        "customerName" to safeName,
        "customerPhone" to "+91 98230 45120",
        "deliveryAddress" to "24 Sahyadri Residency, Baner Road",
        "deliveryDistrict" to "Pune",
        "deliveryCity" to "Pune",
        "deliveryPinCode" to "411045",
        "farmerId" to farmers[0].id,
        "farmerUserId" to uid,
        "farmerName" to farmers[0].name,
        "farmLocation" to "Nashik, Maharashtra",
        "farmerPhone" to farmers[0].phone,
        "itemsSummary" to "Organic Heirloom Tomatoes (3 kg), Tender Green Okra (2 kg)",
        "itemsCount" to 2L,
        "subtotal" to 256.0,
        "deliveryFee" to 30.0,
        "discountAmount" to 0.0,
        "platformCommission" to 12.8,
        "totalAmount" to 286.0,
        "paymentMethod" to "Demo UPI Payment",
        "paymentStatus" to PaymentStatus.DEMO_PAID.name,
        "orderStatus" to OrderStatus.ORDER_PLACED.name,
        "logisticsStatus" to LogisticsStatus.UNASSIGNED.name,
        "pickupInstructions" to "Morning crate at East Gate shed",
        "deliveryInstructions" to "Call upon arrival at Baner gate",
        "otpCode" to "3914",
        "estimatedDelivery" to "Tomorrow, 9:00 AM",
        "stockRestored" to false,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      ),
    )
    batch.set(
      db.collection("orders").document(order1Id).collection("items").document("item_1"),
      mapOf(
        "workspaceOwnerId" to uid,
        "customerUserId" to uid,
        "farmerUserId" to uid,
        "productId" to products[0].id,
        "productName" to products[0].name,
        "unit" to "kg",
        "quantity" to 3L,
        "unitPrice" to 48.0,
        "lineTotal" to 144.0,
        "createdAt" to FieldValue.serverTimestamp(),
      ),
    )
    batch.set(
      db.collection("orders").document(order1Id).collection("history").document("hist_1"),
      mapOf(
        "workspaceOwnerId" to uid,
        "customerUserId" to uid,
        "farmerUserId" to uid,
        "status" to OrderStatus.ORDER_PLACED.name,
        "actorRole" to UserRole.CUSTOMER.name,
        "note" to "Order placed via Demo UPI Payment. Awaiting farmer confirmation.",
        "createdAt" to FieldValue.serverTimestamp(),
      ),
    )

    // Order 2: Ready for Pickup (for Admin to assign Logistics!)
    val order2Id = "ord_vg_1040_$uid"
    batch.set(
      db.collection("orders").document(order2Id),
      mapOf(
        "workspaceOwnerId" to uid,
        "orderNumber" to "VG-1040",
        "customerUserId" to uid,
        "customerName" to "GreenBowl Restaurant",
        "customerPhone" to "+91 98221 77440",
        "deliveryAddress" to "18 Koregaon Park Lane 4",
        "deliveryDistrict" to "Pune",
        "deliveryCity" to "Pune",
        "deliveryPinCode" to "411001",
        "farmerId" to farmers[1].id,
        "farmerUserId" to uid,
        "farmerName" to farmers[1].name,
        "farmLocation" to "Ludhiana, Punjab",
        "farmerPhone" to farmers[1].phone,
        "itemsSummary" to "Aged Traditional Basmati Rice (10 kg)",
        "itemsCount" to 1L,
        "subtotal" to 1350.0,
        "deliveryFee" to 45.0,
        "discountAmount" to 50.0,
        "platformCommission" to 67.5,
        "totalAmount" to 1345.0,
        "paymentMethod" to "Demo Card / Bulk Invoice",
        "paymentStatus" to PaymentStatus.DEMO_PAID.name,
        "orderStatus" to OrderStatus.READY_FOR_PICKUP.name,
        "logisticsStatus" to LogisticsStatus.UNASSIGNED.name,
        "pickupInstructions" to "2 sealed burlap sacks ready at loading bay",
        "deliveryInstructions" to "Deliver to restaurant kitchen entrance",
        "otpCode" to "7420",
        "estimatedDelivery" to "Ready for courier dispatch",
        "stockRestored" to false,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      ),
    )
    batch.set(
      db.collection("orders").document(order2Id).collection("history").document("hist_1"),
      mapOf(
        "workspaceOwnerId" to uid,
        "customerUserId" to uid,
        "farmerUserId" to uid,
        "status" to OrderStatus.READY_FOR_PICKUP.name,
        "actorRole" to UserRole.FARMER.name,
        "note" to "Harvest packed and marked Ready for Pickup.",
        "createdAt" to FieldValue.serverTimestamp(),
      ),
    )

    // Order 3: Logistics Assigned (for Delivery Partner workflow!)
    val order3Id = "ord_vg_1039_$uid"
    batch.set(
      db.collection("orders").document(order3Id),
      mapOf(
        "workspaceOwnerId" to uid,
        "orderNumber" to "VG-1039",
        "customerUserId" to uid,
        "customerName" to safeName,
        "customerPhone" to "+91 98230 45120",
        "deliveryAddress" to "24 Sahyadri Residency, Baner Road",
        "deliveryDistrict" to "Pune",
        "deliveryCity" to "Pune",
        "deliveryPinCode" to "411045",
        "farmerId" to farmers[2].id,
        "farmerUserId" to uid,
        "farmerName" to farmers[2].name,
        "farmLocation" to "Wayanad, Kerala",
        "farmerPhone" to farmers[2].phone,
        "logisticsId" to partners[0].id,
        "logisticsUserId" to uid,
        "logisticsName" to partners[0].name,
        "logisticsPhone" to partners[0].phone,
        "logisticsVehicle" to "${partners[0].vehicle} (${partners[0].plate})",
        "itemsSummary" to "Wayanad Golden Turmeric (3 packet), Wild Forest Honey (1 litre)",
        "itemsCount" to 2L,
        "subtotal" to 990.0,
        "deliveryFee" to 40.0,
        "discountAmount" to 0.0,
        "platformCommission" to 49.5,
        "totalAmount" to 1030.0,
        "paymentMethod" to "Demo UPI Payment",
        "paymentStatus" to PaymentStatus.DEMO_PAID.name,
        "orderStatus" to OrderStatus.LOGISTICS_ASSIGNED.name,
        "logisticsStatus" to LogisticsStatus.ASSIGNED.name,
        "pickupInstructions" to "Collect sealed spice box from Kavitha",
        "deliveryInstructions" to "Verify OTP 5582 with customer",
        "otpCode" to "5582",
        "estimatedDelivery" to "Today, 6:15 PM",
        "stockRestored" to false,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      ),
    )
    batch.set(
      db.collection("orders").document(order3Id).collection("history").document("hist_1"),
      mapOf(
        "workspaceOwnerId" to uid,
        "customerUserId" to uid,
        "farmerUserId" to uid,
        "logisticsUserId" to uid,
        "status" to OrderStatus.LOGISTICS_ASSIGNED.name,
        "actorRole" to UserRole.ADMIN.name,
        "note" to "Assigned to Vikram Chavan (MH-15-TR-8841).",
        "createdAt" to FieldValue.serverTimestamp(),
      ),
    )

    // Order 4: Completed Delivered Order
    val order4Id = "ord_vg_1035_$uid"
    batch.set(
      db.collection("orders").document(order4Id),
      mapOf(
        "workspaceOwnerId" to uid,
        "orderNumber" to "VG-1035",
        "customerUserId" to uid,
        "customerName" to safeName,
        "customerPhone" to "+91 98230 45120",
        "deliveryAddress" to "24 Sahyadri Residency, Baner Road",
        "deliveryDistrict" to "Pune",
        "deliveryCity" to "Pune",
        "deliveryPinCode" to "411045",
        "farmerId" to farmers[0].id,
        "farmerUserId" to uid,
        "farmerName" to farmers[0].name,
        "farmLocation" to "Nashik, Maharashtra",
        "farmerPhone" to farmers[0].phone,
        "logisticsId" to partners[1].id,
        "logisticsUserId" to uid,
        "logisticsName" to partners[1].name,
        "logisticsPhone" to partners[1].phone,
        "logisticsVehicle" to "${partners[1].vehicle} (${partners[1].plate})",
        "itemsSummary" to "Organic Heirloom Tomatoes (5 kg), Unpolished Toor Dal (4 kg)",
        "itemsCount" to 2L,
        "subtotal" to 888.0,
        "deliveryFee" to 30.0,
        "discountAmount" to 0.0,
        "platformCommission" to 44.4,
        "totalAmount" to 918.0,
        "paymentMethod" to "Demo UPI Payment",
        "paymentStatus" to PaymentStatus.SETTLED.name,
        "orderStatus" to OrderStatus.DELIVERED.name,
        "logisticsStatus" to LogisticsStatus.DELIVERED.name,
        "otpCode" to "1129",
        "estimatedDelivery" to "Delivered",
        "proofNote" to "Handed over to recipient with OTP 1129",
        "stockRestored" to false,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      ),
    )

    // Notifications
    batch.set(
      db.collection("notifications").document("notif_1_$uid"),
      mapOf(
        "workspaceOwnerId" to uid,
        "recipientUserId" to uid,
        "targetRole" to UserRole.FARMER.name,
        "orderId" to order1Id,
        "title" to "New Direct Order VG-1041",
        "message" to "$safeName ordered Heirloom Tomatoes & Okra (₹286). Tap to accept.",
        "category" to "ORDER",
        "read" to false,
        "createdAt" to FieldValue.serverTimestamp(),
      ),
    )
    batch.set(
      db.collection("notifications").document("notif_2_$uid"),
      mapOf(
        "workspaceOwnerId" to uid,
        "recipientUserId" to uid,
        "targetRole" to UserRole.ADMIN.name,
        "orderId" to order2Id,
        "title" to "Logistics Assignment Needed",
        "message" to "Order VG-1040 (10 kg Basmati Rice) is Ready for Pickup from Ludhiana.",
        "category" to "LOGISTICS",
        "read" to false,
        "createdAt" to FieldValue.serverTimestamp(),
      ),
    )
    batch.set(
      db.collection("notifications").document("notif_3_$uid"),
      mapOf(
        "workspaceOwnerId" to uid,
        "recipientUserId" to uid,
        "targetRole" to UserRole.LOGISTICS.name,
        "orderId" to order3Id,
        "title" to "New Pickup Task Assigned",
        "message" to "Order VG-1039 assigned to Vikram Chavan. Collect from Wayanad.",
        "category" to "LOGISTICS",
        "read" to false,
        "createdAt" to FieldValue.serverTimestamp(),
      ),
    )

    // Sample Review
    batch.set(
      db.collection("reviews").document("rev_1_$uid"),
      mapOf(
        "workspaceOwnerId" to uid,
        "productId" to products[0].id,
        "userId" to uid,
        "userName" to safeName,
        "rating" to 5L,
        "comment" to "Arrived with morning dew still on the stems. Unmatched farm sweetness!",
        "createdAt" to FieldValue.serverTimestamp(),
      ),
    )

    // Sample Support Ticket
    batch.set(
      db.collection("support_tickets").document("ticket_1_$uid"),
      mapOf(
        "workspaceOwnerId" to uid,
        "userId" to uid,
        "userName" to safeName,
        "orderId" to order4Id,
        "subject" to "Bulk restaurant weekly standing order inquiry",
        "message" to "Can we schedule Tuesday morning crates of tomatoes and okra for Baner?",
        "status" to "OPEN",
        "adminResponse" to "",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      ),
    )

    seedBannerBatchEntries(db, batch, uid)

    batch.commit().await()
  }

  suspend fun ensureBannersSeeded(db: FirebaseFirestore, uid: String) {
    val existing =
      db.collection("banners")
        .whereEqualTo("workspaceOwnerId", uid)
        .limit(1)
        .get()
        .await()
    if (!existing.isEmpty) return

    val batch = db.batch()
    seedBannerBatchEntries(db, batch, uid)
    batch.commit().await()
  }

  private fun seedBannerBatchEntries(
    db: FirebaseFirestore,
    batch: com.google.firebase.firestore.WriteBatch,
    uid: String,
  ) {
    data class SeedBanner(
      val id: String,
      val title: String,
      val subtitle: String,
      val ctaLabel: String,
      val targetSection: String,
      val themeStyle: String,
      val imageKey: String,
      val priority: Long,
      val scheduleNote: String,
    )

    val banners =
      listOf(
        SeedBanner(
          "banner_1_$uid",
          "Fresh Harvest, Direct From Farmers",
          "Morning-picked Nashik vegetables with transparent farm pricing and zero middlemen.",
          "Shop Harvest",
          "MARKET",
          "FOREST",
          "farmland",
          1L,
          "Featured Campaign",
        ),
        SeedBanner(
          "banner_2_$uid",
          "Support Local Farmers. Eat Fresh.",
          "Every basket transfers 95% of order value straight to verified rural growers.",
          "Browse Organic",
          "ORGANIC",
          "OLIVE",
          "tomatoes",
          2L,
          "NPOP Organic Spotlight",
        ),
        SeedBanner(
          "banner_3_$uid",
          "Seasonal Vegetables Are Now Available",
          "Heirloom tomatoes, tender bhindi, and hill spices harvested within the last 18 hours.",
          "Explore Produce",
          "MARKET",
          "TERRACOTTA",
          "okra",
          3L,
          "Winter Harvest Window",
        ),
        SeedBanner(
          "banner_4_$uid",
          "Farm Fresh Fruits Delivered to Your Door",
          "Insulated cold-chain vans bring Alphonso mangoes and orchard fruit straight to your kitchen.",
          "Track Orders",
          "ORDERS",
          "HARVEST",
          "mango",
          4L,
          "Express Cold-Chain",
        ),
        SeedBanner(
          "banner_5_$uid",
          "Buy Direct. Save More. Support Agriculture.",
          "5% direct-from-farm basket savings plus free delivery above ₹500 for homes & restaurants.",
          "Bulk Packs",
          "BULK",
          "FOREST",
          "basmati",
          5L,
          "Restaurant & Family Offer",
        ),
        SeedBanner(
          "banner_6_$uid",
          "Discover Local Farmers Near You.",
          "Verified growers across Nashik, Ludhiana, Wayanad & Mandya ready for direct orders.",
          "Meet Growers",
          "FARMERS",
          "OLIVE",
          "spices",
          6L,
          "Verified Farmer Network",
        ),
      )

    banners.forEach { b ->
      batch.set(
        db.collection("banners").document(b.id),
        mapOf(
          "workspaceOwnerId" to uid,
          "title" to b.title,
          "subtitle" to b.subtitle,
          "ctaLabel" to b.ctaLabel,
          "targetSection" to b.targetSection,
          "themeStyle" to b.themeStyle,
          "imageKey" to b.imageKey,
          "active" to true,
          "priority" to b.priority,
          "scheduleNote" to b.scheduleNote,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
    }
  }
}
