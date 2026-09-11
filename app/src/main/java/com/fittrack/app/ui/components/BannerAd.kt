package com.fittrack.app.ui.components

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Banner de AdMob. Usa el ID de bloque de anuncios de PRUEBA de Google.
 * Antes de publicar, reemplazar por el ID real creado en la cuenta de AdMob.
 */
private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            FrameLayout(context).apply {
                val adView = AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = TEST_BANNER_AD_UNIT_ID
                    loadAd(AdRequest.Builder().build())
                }
                addView(adView)
            }
        }
    )
}
