package com.fittrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittrack.app.data.FitTrackRepository
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    repository: FitTrackRepository,
    userId: Long,
    isPremium: Boolean,
    onOpenHistory: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenAIRoutine: () -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    val sessionDatesFlow = remember(userId) { repository.getAllSessionDates(userId) }
    val sessionDates by sessionDatesFlow.collectAsState(initial = emptyList())

    val trainedDays = remember(sessionDates) {
        sessionDates.map { millis ->
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }.toSet()
    }

    var visibleMonth by remember { mutableStateOf(Calendar.getInstance()) }
    
    val sessionsInMonth = remember(trainedDays, visibleMonth) {
        val year = visibleMonth.get(Calendar.YEAR)
        val month = visibleMonth.get(Calendar.MONTH)
        trainedDays.count { it.first == year && it.second == month }
    }
    val totalDaysInMonth = remember(visibleMonth) {
        visibleMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val themeColor = Color(0xFF6B4DFF)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Calendario", fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text("Tu progreso, día a día", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = onOpenAIRoutine) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Rutina con IA")
                    }
                    if (!isPremium) {
                        IconButton(onClick = onOpenPremium) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = "Premium")
                        }
                    }
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Default.History, contentDescription = "Historial")
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Tarjeta principal del calendario
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    MonthHeaderSection(
                        calendar = visibleMonth,
                        themeColor = themeColor,
                        onPrev = {
                            visibleMonth = (visibleMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                        },
                        onNext = {
                            visibleMonth = (visibleMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                        }
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        WeekdayLabels()
                        Spacer(modifier = Modifier.height(16.dp))
                        MonthGrid(calendar = visibleMonth, trainedDays = trainedDays, themeColor = themeColor)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Leyenda Row
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = themeColor.copy(alpha = 0.05f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(themeColor))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Día con entrenamiento registrado", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = themeColor, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Tarjeta inferior de resumen
            SummaryCard(sessionsInMonth, totalDaysInMonth, themeColor = themeColor)
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MonthHeaderSection(calendar: Calendar, themeColor: Color, onPrev: () -> Unit, onNext: () -> Unit) {
    val months = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Detalle naranja diagonal como en la foto
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp)
                    .background(Brush.linearGradient(listOf(themeColor, Color.Transparent)))
            )

            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = themeColor.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday, 
                            contentDescription = null, 
                            tint = themeColor, 
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "${months[calendar.get(Calendar.MONTH)]} ${calendar.get(Calendar.YEAR)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Constancia hoy, resultados mañana",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPrev,
                        modifier = Modifier.size(36.dp).border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.ChevronLeft, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(44.dp).background(themeColor, CircleShape)
                    ) {
                        Icon(Icons.Default.ChevronRight, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdayLabels() {
    val labels = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        labels.forEach { Text(it, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color(0xFF2D3142), fontSize = 13.sp) }
    }
}

@Composable
private fun MonthGrid(calendar: Calendar, trainedDays: Set<Triple<Int, Int, Int>>, themeColor: Color) {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)

    val firstDayCal = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val firstWeekday = (firstDayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val today = Calendar.getInstance()
    val isCurrentMonth = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month

    val totalCells = firstWeekday + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        var day = 1
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    if (cellIndex < firstWeekday) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF1F4F8)))
                    } else if (day > daysInMonth) {
                        Box(
                            modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF1F4F8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("—", color = Color.LightGray, fontSize = 12.sp)
                        }
                    } else {
                        val isTrained = trainedDays.contains(Triple(year, month, day))
                        val isToday = isCurrentMonth && today.get(Calendar.DAY_OF_MONTH) == day
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                            DayCell(day = day, isTrained = isTrained, isToday = isToday, themeColor = themeColor)
                        }
                        day++
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, isTrained: Boolean, isToday: Boolean, themeColor: Color) {
    val isSelected = isToday
    
    val bg = if (isSelected) themeColor else Color.White
    val textColor = if (isSelected) Color.White else Color(0xFF2D3142)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .shadow(if (isSelected) 8.dp else 0.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else Color.LightGray.copy(alpha = 0.2f),
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                day.toString(), 
                color = textColor, 
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (isTrained || isSelected) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else themeColor)
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(sessions: Int, totalDays: Int, themeColor: Color) {
    val progress = if (totalDays > 0) sessions.toFloat() / totalDays else 0f
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = themeColor.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.FitnessCenter, 
                    contentDescription = null, 
                    tint = themeColor,
                    modifier = Modifier.padding(14.dp).size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("Entrenamiento de hoy", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color(0xFF2D3142))
                Text("$sessions de $totalDays días", color = Color.Gray, fontSize = 13.sp)
            }
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp)) {
                CircularProgressIndicator(
                    progress = { progress },
                    color = themeColor,
                    strokeWidth = 5.dp,
                    trackColor = Color(0xFFF1F4F8),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Text("${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF2D3142))
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = themeColor, modifier = Modifier.size(24.dp))
        }
    }
}
