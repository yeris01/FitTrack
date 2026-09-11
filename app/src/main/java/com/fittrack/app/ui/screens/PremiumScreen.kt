package com.fittrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittrack.app.billing.BillingManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    billingManager: BillingManager,
    isPremium: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val productDetails by billingManager.premiumProductDetails.collectAsState()
    val price = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$9.99"

    val purpleThemeColor = Color(0xFF6B4DFF)
    val darkCardBg = Color(0xFF1A1A1A)

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Fit", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.Black)
                            Text("Track", fontWeight = FontWeight.Black, fontSize = 22.sp, color = purpleThemeColor)
                        }
                        Text("Premium", color = Color.Gray, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Black)
                    }
                },
                actions = {
                    Icon(Icons.Default.WorkspacePremium, null, tint = purpleThemeColor, modifier = Modifier.padding(end = 16.dp).size(30.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER CARD (DARK) ---
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = darkCardBg
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Adorno violeta diagonal arriba a la derecha
                    Box(modifier = Modifier.size(120.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-30).dp).background(purpleThemeColor.copy(alpha = 0.15f), CircleShape))
                    
                    Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WorkspacePremium, null, tint = purpleThemeColor, modifier = Modifier.size(65.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                buildAnnotatedString {
                                    append("Lleva tu entrenamiento\nal ")
                                    withStyle(style = SpanStyle(color = purpleThemeColor)) { append("siguiente nivel") }
                                },
                                color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Con FitTrack Premium accede a todas las herramientas y funciones avanzadas", color = Color.LightGray, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                    }
                }
            }

            // --- BENEFITS CARD (WHITE) ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    BenefitRowUI("Rutinas ilimitadas", "Accede a todas las rutinas y entrena sin límites.", Icons.Default.Adjust, purpleThemeColor)
                    HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 20.dp))
                    BenefitRowUI("Gráficas y estadísticas avanzadas", "Monitorea tu progreso con datos detallados.", Icons.Default.BarChart, purpleThemeColor)
                    HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 20.dp))
                    BenefitRowUI("Rutinas personalizadas con IA", "Planes adaptados a tus objetivos y nivel.", Icons.Default.Star, purpleThemeColor)
                    HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 20.dp))
                    BenefitRowUI("Sin anuncios", "Disfruta de una experiencia sin interrupciones.", Icons.AutoMirrored.Filled.VolumeOff, purpleThemeColor)
                    HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 20.dp))
                    BenefitRowUI("Sincronización en la nube", "Tu progreso desde cualquier dispositivo.", Icons.Default.CloudQueue, purpleThemeColor)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- PRICE CARD (DARK WITH ACCENTS) ---
            Surface(
                modifier = Modifier.fillMaxWidth().height(105.dp),
                shape = RoundedCornerShape(28.dp),
                color = darkCardBg
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Las franjas violetas en las esquinas
                    Box(modifier = Modifier.width(35.dp).fillMaxHeight().align(Alignment.CenterStart).background(Brush.horizontalGradient(listOf(purpleThemeColor.copy(0.3f), Color.Transparent))))
                    Box(modifier = Modifier.width(35.dp).fillMaxHeight().align(Alignment.CenterEnd).background(Brush.horizontalGradient(listOf(Color.Transparent, purpleThemeColor.copy(0.3f)))))

                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(50.dp).border(1.5.dp, purpleThemeColor, RoundedCornerShape(14.dp)).padding(10.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.WorkspacePremium, null, tint = purpleThemeColor)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Plan Premium", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("Todas las funciones, un solo plan", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.DarkGray))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(color = purpleThemeColor, shape = RoundedCornerShape(10.dp)) {
                                Text("Solo", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp))
                            }
                            Text(price, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                            Text("/mes", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- MAIN BUTTON ---
            Button(
                onClick = { if (!isPremium) (context as? android.app.Activity)?.let { billingManager.launchPurchaseFlow(it) } },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = purpleThemeColor),
                enabled = !isPremium
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(if (isPremium) "¡YA ERES PREMIUM!" else "Suscribirme Ahora", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { billingManager.queryExistingPurchases() }) {
                Text("Restaurar compra", color = purpleThemeColor.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun BenefitRowUI(title: String, subtitle: String, icon: ImageVector, themeColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(12.dp), color = themeColor.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = themeColor, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2D3142))
            Text(subtitle, color = Color.Gray, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Icon(
            Icons.Default.CheckCircle, null, tint = themeColor.copy(0.7f),
            modifier = Modifier.size(22.dp)
        )
    }
}
