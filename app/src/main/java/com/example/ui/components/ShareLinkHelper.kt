package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.example.R
import com.example.domain.model.Order
import com.example.domain.model.Product
import com.example.domain.model.UserRole

sealed interface VilaiguardDeepLink {
  data class OpenProduct(val productId: String) : VilaiguardDeepLink
  data class OpenOrder(val orderNumberOrId: String) : VilaiguardDeepLink
  data class SwitchRole(val role: UserRole) : VilaiguardDeepLink
}

object ShareLinkHelper {

  fun appBaseUrl(context: Context): String =
    context.getString(R.string.share_app_url).trimEnd('/')

  fun productUrl(context: Context, productId: String): String =
    "${appBaseUrl(context)}/product/$productId"

  fun orderUrl(context: Context, orderNumber: String): String =
    "${appBaseUrl(context)}/order/$orderNumber"

  fun parseDeepLink(intent: Intent?): VilaiguardDeepLink? {
    val data = intent?.data ?: return null
    val segments = data.pathSegments.filter { it.isNotBlank() }
    if (segments.isEmpty()) return null
    return when (segments[0].lowercase()) {
      "product" -> segments.getOrNull(1)?.let { VilaiguardDeepLink.OpenProduct(it) }
      "order" -> segments.getOrNull(1)?.let { VilaiguardDeepLink.OpenOrder(it) }
      "role" ->
        segments.getOrNull(1)?.let { raw ->
          VilaiguardDeepLink.SwitchRole(UserRole.fromRaw(raw.uppercase()))
        }
      else -> null
    }
  }

  fun shareApp(context: Context) {
    val url = appBaseUrl(context)
    val text =
      "Explore VILAIGUARD — Direct Farmer-to-Consumer Agricultural Marketplace.\n" +
        "Fresh From Farmers. Fair For Everyone.\n" +
        "Open in VILAIGUARD Android app: $url"
    launchShareChooser(context, "Share VILAIGUARD", text)
  }

  fun shareProduct(context: Context, product: Product) {
    val url = productUrl(context, product.id)
    val organicTag = if (product.organic) " (Organic)" else ""
    val text =
      "${product.name}$organicTag — ${formatInr(product.pricePerUnit)} / ${product.unit}\n" +
        "Grown by ${product.farmerName} (${product.farmLocation})\n" +
        "Open in VILAIGUARD: $url"
    launchShareChooser(context, "Share ${product.name}", text)
  }

  fun shareOrder(context: Context, order: Order) {
    val url = orderUrl(context, order.orderNumber)
    val text =
      "VILAIGUARD Order ${order.orderNumber} (${order.orderStatus})\n" +
        "${order.itemsSummary}\n" +
        "Farmer: ${order.farmerName} • Open in VILAIGUARD: $url"
    launchShareChooser(context, "Share Order ${order.orderNumber}", text)
  }

  fun copyToClipboard(context: Context, label: String, url: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, url))
  }

  private fun launchShareChooser(context: Context, subject: String, text: String) {
    val sendIntent =
      Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
      }
    val chooser = Intent.createChooser(sendIntent, subject).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
  }
}
