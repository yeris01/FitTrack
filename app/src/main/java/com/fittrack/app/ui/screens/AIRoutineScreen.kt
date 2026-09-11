package com.fittrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittrack.app.ui.viewmodel.AIGenerationState
import com.fittrack.app.ui.viewmodel.AIRoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIRoutineScreen(
    viewModel: AIRoutineViewModel,
    isPremium: Boolean,
    onBack: () -> Unit,
    onUpgradeClick: () -> Unit,
    onDone: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val purpleThemeColor = Color(0xFF6B4DFF)
    val darkBlueBlack = Color(0xFF1D2635)

    var daysPerWeek by remember { mutableStateOf(4) }
    var goal by remember { mutableStateOf("Ganar masa muscular") }
    var level by remember { mutableStateOf("Intermedio") }
    var equipment by remember { mutableStateOf("Gimnasio completo") }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                buildAnnotatedString {
                                    append("Rutina ")
                                    withStyle(style = SpanStyle(color = purpleThemeColor)) { append("con IA") }
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                color = darkBlueBlack
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.SmartToy, null, tint = darkBlueBlack, modifier = Modifier.size(34.dp))
                        }
                        Text("Tu entrenador inteligente, siempre contigo", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = darkBlueBlack)
                    }
                },
                actions = {
                    Icon(Icons.Default.AutoAwesome, null, tint = purpleThemeColor.copy(alpha = 0.5f), modifier = Modifier.padding(end = 16.dp).size(24.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
            if (!isPremium) {
                // --- DARK HERO CARD ---
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF1C222E)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Diagonal Purple Stripes
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .fillMaxHeight()
                                .align(Alignment.BottomEnd)
                                .background(Brush.linearGradient(listOf(Color.Transparent, purpleThemeColor.copy(0.4f))))
                        )
                        
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Robot in Circle
                                Box(contentAlignment = Alignment.Center) {
                                    Surface(
                                        modifier = Modifier.size(110.dp),
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.05f)
                                    ) { }
                                    Icon(Icons.Default.SmartToy, null, tint = Color.White, modifier = Modifier.size(70.dp))
                                    Text(
                                        "IA", 
                                        color = Color.White, 
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .offset(y = (-4).dp)
                                            .background(purpleThemeColor, CircleShape)
                                            .padding(horizontal = 10.dp, vertical = 2.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(20.dp))
                                
                                Column {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.WorkspacePremium, null, tint = purpleThemeColor, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("PREMIUM", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        buildAnnotatedString {
                                            append("Esta función es\n")
                                            withStyle(style = SpanStyle(color = purpleThemeColor)) { append("Premium") }
                                        },
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        lineHeight = 26.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Genera rutinas a medida según tus objetivos usando IA.",
                                        fontSize = 12.sp,
                                        color = Color.LightGray,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onUpgradeClick,
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(27.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = purpleThemeColor)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WorkspacePremium, null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Ver Premium", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                // --- WHAT'S INCLUDED SECTION ---
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(4.dp).height(24.dp).background(purpleThemeColor))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("¿Qué incluye?", fontWeight = FontWeight.Black, fontSize = 20.sp, color = darkBlueBlack)
                    }
                    Text("Con Premium obtienes una experiencia completa y personalizada.", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }

                // --- BENEFITS GRID ---
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            GridBenefitItem(Modifier.weight(1f), "Rutinas\npersonalizadas", "Adaptadas a tus objetivos y nivel.", Icons.Default.Adjust, purpleThemeColor)
                            GridBenefitItem(Modifier.weight(1f), "IA inteligente", "Analiza tu progreso y ajusta tus rutinas.", Icons.Default.Psychology, purpleThemeColor)
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            GridBenefitItem(Modifier.weight(1f), "Seguimiento\nde progreso", "Visualiza tus avances y mejora cada día.", Icons.Default.BarChart, purpleThemeColor)
                            GridBenefitItem(Modifier.weight(1f), "Más resultados", "Entrena de forma más eficiente y motivador.", Icons.Default.Favorite, purpleThemeColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- FOOTER BANNER ---
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF3EFFF).copy(alpha = 0.8f)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Small gradient accent
                        Box(modifier = Modifier.fillMaxHeight().width(80.dp).align(Alignment.CenterEnd).background(Brush.horizontalGradient(listOf(Color.Transparent, purpleThemeColor.copy(0.1f)))))
                        
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = purpleThemeColor, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Tu mejor versión, con IA", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = darkBlueBlack)
                                Text("Haz que cada entrenamiento cuente.", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }

            } else {
                // (AI Generator UI for Premium users)
                when (val s = state) {
                    is AIGenerationState.Idle, is AIGenerationState.Error -> {
                        Text("Contale a la IA tus objetivos:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = goal,
                            onValueChange = { goal = it },
                            label = { Text("¿Cuál es tu objetivo?") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Días por semana: $daysPerWeek", fontWeight = FontWeight.Medium)
                        Slider(
                            value = daysPerWeek.toFloat(),
                            onValueChange = { daysPerWeek = it.toInt() },
                            valueRange = 2f..6f,
                            steps = 3,
                            colors = SliderDefaults.colors(thumbColor = purpleThemeColor, activeTrackColor = purpleThemeColor)
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = { viewModel.generate(daysPerWeek, goal, level, equipment) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = purpleThemeColor)
                        ) {
                            Text("Generar rutina mágica ✨", fontWeight = FontWeight.Bold)
                        }

                        if (s is AIGenerationState.Error) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(s.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        }
                    }

                    is AIGenerationState.Loading -> {
                        Box(modifier = Modifier.height(300.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = purpleThemeColor)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("La IA está diseñando tu plan...", fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    is AIGenerationState.Success -> {
                        Text("✅ ¡Listo! Se crearon estas rutinas:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        s.routineNames.forEach { name ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                shadowElevation = 1.dp
                            ) {
                                Text("• $name", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { viewModel.reset(); onDone() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = purpleThemeColor)
                        ) {
                            Text("Ir a mis rutinas", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridBenefitItem(modifier: Modifier, title: String, subtitle: String, icon: ImageVector, themeColor: Color) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = themeColor.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = themeColor, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 16.sp, color = Color(0xFF2D3142))
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color.Gray, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}
