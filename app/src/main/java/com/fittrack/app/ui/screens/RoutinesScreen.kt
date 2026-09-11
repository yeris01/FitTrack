package com.fittrack.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittrack.app.R
import com.fittrack.app.data.RoutineEntity
import com.fittrack.app.ui.viewmodel.RoutineViewModel

private const val FREE_ROUTINE_LIMIT = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    viewModel: RoutineViewModel,
    isPremium: Boolean,
    onCreateRoutine: () -> Unit,
    onStartRoutine: (RoutineEntity) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenAIRoutine: () -> Unit,
    onSignOut: () -> Unit
) {
    val routines by viewModel.routines.collectAsState()
    val reachedFreeLimit = !isPremium && routines.size >= FREE_ROUTINE_LIMIT
    val purpleColor = Color(0xFF6B4DFF)
    val darkBlue = Color(0xFF2D3142)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Fit", fontWeight = FontWeight.Black, fontSize = 24.sp, color = darkBlue)
                            Text("Track", fontWeight = FontWeight.Black, fontSize = 24.sp, color = purpleColor)
                        }
                        Text("Tu entrenamiento, tu progreso", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = onOpenAIRoutine) {
                        Icon(Icons.Default.AutoAwesome, null, tint = darkBlue)
                    }
                    IconButton(onClick = onOpenPremium) {
                        Icon(Icons.Default.WorkspacePremium, null, tint = darkBlue)
                    }
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Default.History, null, tint = darkBlue)
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = darkBlue)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (reachedFreeLimit) onOpenPremium() else onCreateRoutine() },
                containerColor = purpleColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- HERO CARD (PURPLE GRADIENT) ---
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF4A34CC)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(Color(0xFF6B4DFF), Color(0xFF4A34CC))))
                    )

                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                Icons.Default.TrackChanges, 
                                null, 
                                tint = Color.White, 
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                buildAnnotatedString {
                                    append("¡Hoy es un gran día para seguir ")
                                    withStyle(style = SpanStyle(color = Color(0xFFFFA000))) { append("avanzando!") }
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "No tienes rutinas asignadas.\nToca + para crear la primera 💪",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // --- SECTION HEADER ---
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(4.dp).height(24.dp).background(purpleColor))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Sugerencias para ti",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = darkBlue
                    )
                }
                Text(
                    "Elige una rutina y comienza hoy mismo.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // --- SUGGESTED ROUTINES ---
            SuggestedRoutinesListNew(viewModel, onRoutineCreated = onStartRoutine)

            // --- ADS CARD ---
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF3EFFF)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = purpleColor, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Test Ad", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = darkBlue)
                            Text("AdMob. Way to go!", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = purpleColor),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("OPEN ->", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SuggestedRoutinesListNew(
    viewModel: RoutineViewModel, 
    onRoutineCreated: (RoutineEntity) -> Unit
) {
    val templates = listOf(
        TemplateDataUI("Full Body (Básico)", "Trabaja todo el cuerpo con ejercicios esenciales.", Icons.Default.AccessibilityNew, R.drawable.bg_full_body, Color(0xFF6B4DFF)),
        TemplateDataUI("Tren Superior", "Fortalece pecho, espalda, hombros y brazos.", Icons.Default.FitnessCenter, R.drawable.bg_upper_body, Color(0xFF2196F3)),
        TemplateDataUI("Tren Inferior", "Trabaja piernas y glúteos con ejercicios efectivos.", Icons.Default.DirectionsRun, R.drawable.bg_lower_body, Color(0xFF00BFA5))
    )
    val allExercises by viewModel.allExercises.collectAsState()

    templates.forEach { template ->
        NewRoutineCard(
            title = template.name,
            description = template.desc,
            icon = template.icon,
            backgroundRes = template.bgRes,
            themeColor = template.themeColor,
            onAction = {
                val exNames = when(template.name) {
                    "Full Body (Básico)" -> listOf("Press banca", "Sentadilla", "Dominadas", "Press militar")
                    "Tren Superior" -> listOf("Press banca", "Remo con barra", "Elevaciones laterales", "Curl con barra")
                    else -> listOf("Sentadilla", "Prensa de piernas", "Peso muerto", "Hip thrust")
                }
                val selected = allExercises.filter { it.name in exNames }
                if (selected.isNotEmpty()) {
                    viewModel.createRoutine(template.name, selected) { id ->
                        onRoutineCreated(RoutineEntity(id = id, userId = viewModel.userId, name = template.name))
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

data class TemplateDataUI(val name: String, val desc: String, val icon: ImageVector, val bgRes: Int, val themeColor: Color)

@Composable
fun NewRoutineCard(
    title: String,
    description: String,
    icon: ImageVector,
    backgroundRes: Int,
    themeColor: Color,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onAction() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.height(IntrinsicSize.Min).fillMaxWidth()) {
            
            // Imagen de fondo con opacidad suave
            Image(
                painter = painterResource(id = backgroundRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.15f
            )
            
            Row(modifier = Modifier.height(IntrinsicSize.Min).fillMaxWidth()) {
                Box(modifier = Modifier.width(6.dp).fillMaxHeight().background(themeColor))
                
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = themeColor.copy(alpha = 0.1f)
                    ) {
                        Icon(icon, null, tint = themeColor, modifier = Modifier.padding(14.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF2D3142))
                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text(" 4 ejercicios   ", fontSize = 12.sp, color = Color.Gray)
                            Icon(Icons.Default.AccessTime, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text(" 20 - 30 min", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(description, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
                    }
                    
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(themeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
