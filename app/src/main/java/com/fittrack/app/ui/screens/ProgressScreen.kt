package com.fittrack.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittrack.app.data.ExerciseEntity
import com.fittrack.app.ui.components.ProgressLineChart
import com.fittrack.app.ui.viewmodel.ProgressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: ProgressViewModel, isPremium: Boolean, onUpgradeClick: () -> Unit) {
    val exercises by viewModel.allExercises.collectAsState()
    var selected by remember { mutableStateOf<ExerciseEntity?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(exercises) {
        if (selected == null && exercises.isNotEmpty()) selected = exercises.first()
    }

    val history by remember(selected) {
        selected?.let { viewModel.historyFor(it.id) } ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val points = remember(history) { viewModel.maxWeightPerSession(history) }
    val maxWeight = if (points.isNotEmpty()) points.maxOf { it.second }.toInt() else 0
    
    val purpleThemeColor = Color(0xFF6B4DFF)
    val darkCardBg = Color(0xFF1A1A1A)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShowChart, null, tint = purpleThemeColor, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Progreso", fontWeight = FontWeight.Black, fontSize = 24.sp)
                            Text("Tu esfuerzo de hoy, construye tus resultados", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SELECTOR DE EJERCICIO ---
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, purpleThemeColor.copy(alpha = 0.4f)),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = purpleThemeColor.copy(alpha = 0.1f)
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = purpleThemeColor, modifier = Modifier.padding(10.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Periodo", fontSize = 11.sp, color = Color.Gray)
                        Text(selected?.name ?: "Elegí un ejercicio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Black)
                }
                
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    exercises.forEach { ex ->
                        DropdownMenuItem(
                            text = { Text(ex.name) },
                            onClick = {
                                selected = ex
                                expanded = false
                            }
                        )
                    }
                }
            }

            // --- CHART CARD (DARK) ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = darkCardBg
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                    // Detalle naranja diagonal
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 20.dp, y = (-20).dp)
                            .background(Brush.linearGradient(listOf(purpleThemeColor, Color.Transparent)))
                    )

                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = purpleThemeColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, purpleThemeColor.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Default.FitnessCenter, null, tint = purpleThemeColor, modifier = Modifier.padding(14.dp))
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text("Peso máximo levantado por entrenamiento", color = Color.LightGray, fontSize = 12.sp)
                            Text("$maxWeight kg", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                            if (points.isEmpty()) {
                                Text("Todavía no hay datos para graficar", color = Color.Gray, fontSize = 12.sp)
                            } else {
                                Box(modifier = Modifier.fillMaxWidth().height(130.dp).padding(top = 16.dp)) {
                                    ProgressLineChart(
                                        points = points,
                                        lineColor = purpleThemeColor,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- ESTADÍSTICAS O CTA PREMIUM ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BarChart, null, tint = purpleThemeColor, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Estadísticas avanzadas", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    
                    if (!isPremium) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Con Premium accedés a 1RM estimado, volumen total semanal, comparación entre ejercicios y más.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onUpgradeClick,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = purpleThemeColor)
                        ) {
                            Icon(Icons.Default.WorkspacePremium, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ver Premium", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        AdvancedStatsSection(points = points)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- MOTIVATIONAL FOOTER ---
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFFFF3EE).copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TrackChanges, null, tint = purpleThemeColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Cada repetición cuenta", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("¡Sigue entrenando y ve tu progreso!", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvancedStatsSection(points: List<Pair<Long, Double>>) {
    if (points.isEmpty()) return
    val best = points.maxOf { it.second }
    val last = points.last().second
    val first = points.first().second
    val change = if (first > 0) ((last - first) / first * 100) else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Mejor marca: ${best.toInt()} kg", fontWeight = FontWeight.Medium)
        Text("Variación total: ${"%.1f".format(change)}%", color = if (change >= 0) Color(0xFF2ECC71) else Color.Red)
    }
}
