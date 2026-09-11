package com.fittrack.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittrack.app.data.FitTrackRepository
import com.fittrack.app.data.SetLogEntity
import com.fittrack.app.data.WorkoutSessionEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(repository: FitTrackRepository, userId: Long, onBack: () -> Unit) {
    val sessionsFlow = remember(userId) { 
        Log.d("HistoryScreen", "Fetching sessions for userId: $userId")
        repository.getAllSessions(userId) 
    }
    val sessions by sessionsFlow.collectAsState(initial = emptyList())
    val purpleThemeColor = Color(0xFF6B4DFF)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Historial", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.Black)
                        Text("Tus entrenamientos pasados", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.Black)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (sessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Todavía no completaste entrenamientos 🔥",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sessions) { session ->
                        SessionCard(session, repository, purpleThemeColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(session: WorkoutSessionEntity, repository: FitTrackRepository, themeColor: Color) {
    val fmtDate = remember { SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")) }
    val fmtTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    val setsFlow = remember(session.id) { repository.getSetsForSession(session.id) }
    val sets by setsFlow.collectAsState(initial = emptyList())

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = themeColor.copy(alpha = 0.1f)
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = themeColor, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        session.routineName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF2D3142)
                    )
                    Text(
                        fmtDate.format(Date(session.startedAt)).replaceFirstChar { it.uppercase() },
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(Icons.Default.Timer, "Hora", fmtTime.format(Date(session.startedAt)))
                
                session.finishedAt?.let {
                    val duration = (it - session.startedAt) / 60000
                    InfoItem(Icons.Default.Timer, "Duración", "$duration min")
                }

                val totalWeight = sets.sumOf { it.weightKg * it.reps }
                InfoItem(Icons.Default.History, "Volumen", "${totalWeight.toInt()} kg")
            }

            if (sets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Ejercicios:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2D3142))
                Spacer(modifier = Modifier.height(8.dp))
                
                val setsByExercise = sets.groupBy { it.exerciseName }
                setsByExercise.forEach { (name, exerciseSets) ->
                    Text(
                        "• $name: ${exerciseSets.size} series",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2D3142))
    }
}
