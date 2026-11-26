const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const ADMIN_UID = "admin_uid_789";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTime(offsetMs = 5000) {
  return new Date(Date.now() - offsetMs);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("products").get());
  await assertFails(unauthDb.collection("orders").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("products").doc("prod_1").set({
      workspaceOwnerId: ALICE_UID,
      name: "Unauth Tomatoes",
    })
  );
});

test("User profile creation prevents role escalation and isolates PII", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();
  const now = pastTime();

  // Reject self-assigned ADMIN role or VERIFIED status
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice",
      email: "alice@example.com",
      role: "ADMIN",
      activeDemoRole: "ADMIN",
      verificationStatus: "VERIFIED",
      accountStatus: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Valid customer profile create succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Sharma",
      email: "alice@example.com",
      phone: "+91 9876543210",
      role: "CUSTOMER",
      activeDemoRole: "CUSTOMER",
      city: "Pune",
      district: "Pune",
      state: "Maharashtra",
      pinCode: "411001",
      verificationStatus: "PENDING",
      accountStatus: "ACTIVE",
      demoSeeded: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob cannot read Alice's PII profile
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  // Alice cannot update verificationStatus
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      verificationStatus: "VERIFIED",
      updatedAt: pastTime(1000),
    })
  );
  // Alice can switch activeDemoRole safely
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      activeDemoRole: "FARMER",
      updatedAt: pastTime(1000),
    })
  );
});

test("Products: owner create/update, stock >= 0, cross-user denial & filtered query", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = pastTime();

  await assertSucceeds(
    aliceDb.collection("products").doc("prod_tomato").set({
      workspaceOwnerId: ALICE_UID,
      farmerId: "farmer_1",
      farmerUserId: ALICE_UID,
      farmerName: "Ramesh Patel",
      farmLocation: "Nashik, Maharashtra",
      name: "Organic Heirloom Tomatoes",
      category: "VEGETABLES",
      description: "Sun-ripened field tomatoes",
      pricePerUnit: 48.0,
      unit: "kg",
      stockQuantity: 120,
      harvestDate: "2026-11-25",
      organic: true,
      verified: true,
      available: true,
      imageKey: "tomatoes",
      rating: 4.8,
      reviewCount: 24,
      approvalStatus: "APPROVED",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject negative stock
  await assertFails(
    aliceDb.collection("products").doc("prod_tomato").update({
      stockQuantity: -5,
      updatedAt: pastTime(1000),
    })
  );

  // Filtered query matching Kotlin repository succeeds
  await assertSucceeds(
    aliceDb.collection("products").where("workspaceOwnerId", "==", ALICE_UID).get()
  );

  // Unfiltered query or cross-user read fails
  await assertFails(aliceDb.collection("products").get());
  await assertFails(bobDb.collection("products").doc("prod_tomato").get());
});

test("Orders: full lifecycle, terminal state locking, subcollections & notifications", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = pastTime();

  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_1001").set({
      workspaceOwnerId: ALICE_UID,
      orderNumber: "VG-1001",
      customerUserId: ALICE_UID,
      customerName: "Alice Sharma",
      customerPhone: "+91 9876543210",
      deliveryAddress: "14 MG Road, Camp",
      deliveryDistrict: "Pune",
      deliveryCity: "Pune",
      deliveryPinCode: "411001",
      farmerId: "farmer_1",
      farmerUserId: ALICE_UID,
      farmerName: "Ramesh Patel",
      farmLocation: "Nashik, Maharashtra",
      farmerPhone: "+91 9811122233",
      itemsSummary: "Organic Heirloom Tomatoes (2 kg)",
      itemsCount: 1,
      subtotal: 96.0,
      deliveryFee: 25.0,
      discountAmount: 0.0,
      platformCommission: 4.8,
      totalAmount: 121.0,
      paymentMethod: "Demo UPI Payment",
      paymentStatus: "DEMO_PAID",
      orderStatus: "ORDER_PLACED",
      logisticsStatus: "UNASSIGNED",
      otpCode: "4829",
      estimatedDelivery: "Today, 5:30 PM",
      stockRestored: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Subcollection item & history
  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_1001").collection("items").doc("item_1").set({
      workspaceOwnerId: ALICE_UID,
      customerUserId: ALICE_UID,
      farmerUserId: ALICE_UID,
      productId: "prod_tomato",
      productName: "Organic Heirloom Tomatoes",
      unit: "kg",
      quantity: 2,
      unitPrice: 48.0,
      lineTotal: 96.0,
      createdAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_1001").collection("history").doc("hist_1").set({
      workspaceOwnerId: ALICE_UID,
      customerUserId: ALICE_UID,
      farmerUserId: ALICE_UID,
      status: "ORDER_PLACED",
      actorRole: "CUSTOMER",
      note: "Order placed via Demo UPI",
      createdAt: now,
    })
  );

  // Transition to DELIVERED
  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_1001").update({
      orderStatus: "DELIVERED",
      logisticsStatus: "DELIVERED",
      paymentStatus: "SETTLED",
      proofNote: "Delivered with OTP 4829",
      updatedAt: pastTime(500),
    })
  );

  // Terminal state locking prevents non-admin updates after DELIVERED
  await assertFails(
    aliceDb.collection("orders").doc("ord_1001").update({
      orderStatus: "CANCELLED",
      updatedAt: pastTime(200),
    })
  );

  // Cross-user read denied
  await assertFails(bobDb.collection("orders").doc("ord_1001").get());
});

test("Email spoofing test: unverified admin email is rejected", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice",
      email: "alice@example.com",
      role: "CUSTOMER",
      activeDemoRole: "CUSTOMER",
      verificationStatus: "PENDING",
      accountStatus: "ACTIVE",
      createdAt: pastTime(),
      updatedAt: pastTime(),
    });
  });

  const spoofAdminDb = testEnv.authenticatedContext("spoof_uid", {
    email: "g03608160@gmail.com",
    email_verified: false,
  }).firestore();

  await assertFails(spoofAdminDb.collection("users").doc(ALICE_UID).get());

  const realAdminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "g03608160@gmail.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(realAdminDb.collection("users").doc(ALICE_UID).get());
});
