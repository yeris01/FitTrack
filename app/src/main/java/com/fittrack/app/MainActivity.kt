package com.fittrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fittrack.app.billing.BillingManager
import com.fittrack.app.auth.AuthManager
import com.fittrack.app.data.AppDatabase
import com.fittrack.app.data.FitTrackRepository
import com.fittrack.app.ui.navigation.FitTrackNavHost
import com.fittrack.app.ui.theme.FitTrackTheme
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var billingManager: BillingManager
    private lateinit var repository: FitTrackRepository
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this)

        val db = AppDatabase.getInstance(applicationContext)
        repository = FitTrackRepository(db)
        authManager = AuthManager(applicationContext, db.userDao())

        billingManager = BillingManager(applicationContext) { isPremium ->
            // Google Play confirmó el estado de la compra: lo guardamos en la caché local.
            CoroutineScope(Dispatchers.IO).launch {
                val userId = authManager.currentUserId.value
                if (userId != -1L) {
                    repository.setPremiumStatus(userId, isPremium)
                }
            }
        }
        billingManager.startConnection()

        setContent {
            FitTrackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FitTrackNavHost(
                        repository = repository,
                        billingManager = billingManager,
                        authManager = authManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        billingManager.endConnection()
        super.onDestroy()
    }
}
