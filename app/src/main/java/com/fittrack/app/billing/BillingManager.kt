package com.fittrack.app.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Encapsula Google Play Billing para el producto único "Premium" (compra que no vence).
 *
 * IMPORTANTE antes de publicar:
 * 1. Crear el producto administrado "fittrack_premium" en Play Console > Monetización > Productos.
 * 2. Publicar la app al menos en test interno para que la compra funcione (Billing no
 *    funciona con builds instalados directamente por USB salvo cuentas de prueba configuradas).
 */
class BillingManager(context: Context, private val onPurchaseUpdated: (isPremium: Boolean) -> Unit) {

    companion object {
        const val PREMIUM_PRODUCT_ID = "fittrack_premium"
        private const val TAG = "BillingManager"
    }

    private val _premiumProductDetails = MutableStateFlow<ProductDetails?>(null)
    val premiumProductDetails: StateFlow<ProductDetails?> = _premiumProductDetails

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { handlePurchase(it) }
        } else {
            Log.w(TAG, "Compra no completada: ${billingResult.debugMessage}")
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    queryExistingPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Se puede reintentar la conexión con backoff si hace falta.
            }
        })
    }

    private fun queryProductDetails() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PREMIUM_PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billingClient.queryProductDetailsAsync(
            params,
            object : ProductDetailsResponseListener {
                override fun onProductDetailsResponse(
                    billingResult: BillingResult,
                    productDetailsList: List<ProductDetails>
                ) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _premiumProductDetails.value = productDetailsList.firstOrNull()
                    }
                }
            }
        )
    }

    /** Revisa compras ya realizadas (por si el usuario reinstaló la app o cambió de dispositivo). */
    fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchases.any {
                    it.products.contains(PREMIUM_PRODUCT_ID) && it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                onPurchaseUpdated(hasPremium)
                purchases.forEach { handlePurchase(it) }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val productDetails = _premiumProductDetails.value ?: run {
            Log.w(TAG, "Todavía no se cargó el producto Premium desde Play")
            return
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(ackParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        onPurchaseUpdated(true)
                    }
                }
            } else {
                onPurchaseUpdated(true)
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}
