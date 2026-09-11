package com.fittrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittrack.app.data.RoutineExerciseEntity
import com.fittrack.app.data.SetLogEntity
import com.fittrack.app.ui.components.RestTimerBar
import com.fittrack.app.ui.viewmodel.RoutineViewModel
import com.fittrack.app.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    routineId: Long,
    routineName: String,
    routineViewModel: RoutineViewModel,
    workoutViewModel: WorkoutViewModel,
    onFinished: () -> Unit
) {
    val exercisesFlow = remember(routineId) { routineViewModel.exercisesForRoutine(routineId) }
    val exercisesList by exercisesFlow.collectAsState(initial = emptyList())
    val purpleThemeColor = Color(0xFF6B4DFF)

    // Distinguimos "todavía no llegó el primer resultado del Flow" (cargando) de
    // "llegó y la rutina realmente no tiene ejercicios", para no mostrar un spinner infinito.
    var hasLoadedExercises by remember(routineId) { mutableStateOf(false) }
    LaunchedEffect(routineId) {
        exercisesFlow.collect { hasLoadedExercises = true }
    }
    val exercises: List<RoutineExerciseEntity>? = if (hasLoadedExercises) exercisesList else null

    val sessionId by workoutViewModel.sessionId.collectAsState()
    val timerSeconds by workoutViewModel.timerSeconds.collectAsState()
    val timerRunning by workoutViewModel.timerRunning.collectAsState()
    val totalSeconds by workoutViewModel.totalWorkoutSeconds.collectAsState()

    fun formatTime(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    LaunchedEffect(exercisesList, hasLoadedExercises) {
        if (hasLoadedExercises && exercisesList.isNotEmpty() && sessionId == null) {
            workoutViewModel.startSession(routineId, routineName, exercisesList) {}
        }
    }

    val loggedSets by remember(sessionId) {
        sessionId?.let { workoutViewModel.getSetsForSession(it) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    var showSummaryDialog by remember { mutableStateOf(false) }

    if (showSummaryDialog) {
        AlertDialog(
            onDismissRequest = { /* No cerrar afuera */ },
            containerColor = Color(0xFFFDF7FF),
            shape = RoundedCornerShape(28.dp),
            title = { Text("¡Entrenamiento completado! 🏆", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("¡Excelente trabajo!")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tiempo total: ${formatTime(totalSeconds)}", fontWeight = FontWeight.Medium)
                    Text("Series registradas: ${loggedSets.size}", fontWeight = FontWeight.Medium)
                }
            },
            confirmButton = {
                Button(
                    onClick = onFinished,
                    colors = ButtonDefaults.buttonColors(containerColor = purpleThemeColor),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Continuar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { 
            TopAppBar(
                title = { 
                    Column {
                        Text(routineName, fontWeight = FontWeight.Bold)
                        Text("Tiempo: ${formatTime(totalSeconds)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { workoutViewModel.finishSession { showSummaryDialog = true } }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            ) 
        },
        bottomBar = {
            Column {
                RestTimerBar(
                    secondsLeft = timerSeconds,
                    isRunning = timerRunning,
                    onStart = { workoutViewModel.startRestTimer(it) },
                    onCancel = { workoutViewModel.cancelRestTimer() }
                )
                Button(
                    onClick = { workoutViewModel.finishSession { showSummaryDialog = true } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = purpleThemeColor),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("▶ TERMINAR", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            }
        }
    ) { padding ->
        when {
            exercises == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = purpleThemeColor)
                }
            }
            exercises!!.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Esta rutina no tiene ejercicios cargados.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { workoutViewModel.finishSession(onFinished) },
                            colors = ButtonDefaults.buttonColors(containerColor = purpleThemeColor),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Volver")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(exercises!!) { ex ->
                        ExerciseLogCard(
                            exercise = ex,
                            loggedSets = loggedSets.filter { it.exerciseId == ex.exerciseId },
                            themeColor = purpleThemeColor,
                            onAddSet = { weight, reps, setNumber ->
                                workoutViewModel.logSet(ex.exerciseId, ex.exerciseName, setNumber, weight, reps)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseLogCard(
    exercise: RoutineExerciseEntity,
    loggedSets: List<SetLogEntity>,
    themeColor: Color,
    onAddSet: (weight: Double, reps: Int, setNumber: Int) -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }

    Column {
        Text(exercise.exerciseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2D3142))
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.8f),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (loggedSets.isEmpty()) {
                    Text("Sin series todavía", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    loggedSets.forEachIndexed { index, set ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(themeColor, CircleShape))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "${set.weightKg.toInt()} kg × ${set.reps}",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2D3142)
                            )
                        }
                        if (index != loggedSets.lastIndex) Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("kg") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, focusedLabelColor = themeColor)
            )
            OutlinedTextField(
                value = repsInput,
                onValueChange = { repsInput = it },
                label = { Text("reps") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, focusedLabelColor = themeColor)
            )
            Button(
                onClick = {
                    val weight = weightInput.toDoubleOrNull()
                    val reps = repsInput.toIntOrNull()
                    if (weight != null && reps != null) {
                        onAddSet(weight, reps, loggedSets.size + 1)
                        weightInput = ""
                        repsInput = ""
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text("+ Serie", fontWeight = FontWeight.Bold)
            }
        }
    }
}
