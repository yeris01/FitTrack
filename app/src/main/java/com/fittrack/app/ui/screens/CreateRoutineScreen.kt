package com.fittrack.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.fittrack.app.data.ExerciseEntity
import com.fittrack.app.data.MuscleGroup
import com.fittrack.app.ui.viewmodel.RoutineViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    viewModel: RoutineViewModel,
    onBack: () -> Unit,
    onRoutineCreated: (Long) -> Unit
) {
    var routineName by remember { mutableStateOf("") }
    val allExercises by viewModel.allExercises.collectAsState()
    val selected = remember { mutableStateListOf<ExerciseEntity>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val purpleThemeColor = Color(0xFF6B4DFF)

    LaunchedEffect(allExercises.size) {
        Log.d("CreateRoutineScreen", "allExercises updated, size: ${allExercises.size}")
    }
    
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Nueva rutina") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (routineName.isNotBlank() && selected.isNotEmpty()) {
                        viewModel.createRoutine(routineName, selected.toList()) { id ->
                            onRoutineCreated(id)
                        }
                    }
                },
                enabled = routineName.isNotBlank() && selected.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = purpleThemeColor,
                    disabledContainerColor = purpleThemeColor.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Guardar rutina (${selected.size} ejercicios)", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = routineName,
                onValueChange = { routineName = it },
                label = { Text("Ej: Pecho + Tríceps") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = purpleThemeColor,
                    focusedLabelColor = purpleThemeColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Elegí los ejercicios", style = MaterialTheme.typography.titleSmall)
                TextButton(
                    onClick = { showAddExerciseDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = purpleThemeColor)
                ) {
                    Text("+ Nuevo Ejercicio", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (allExercises.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron ejercicios.\nIntentá agregar uno nuevo arriba.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MuscleGroup.values().forEach { group ->
                        val exercisesInGroup = allExercises.filter { it.muscleGroup == group }
                        if (exercisesInGroup.isNotEmpty()) {
                            item {
                                Text(
                                    group.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                                    color = purpleThemeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(exercisesInGroup, key = { it.id }) { exercise ->
                                val isSelected = selected.any { it.id == exercise.id }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (isSelected) {
                                                selected.removeAll { it.id == exercise.id }
                                            } else {
                                                selected.add(exercise)
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = purpleThemeColor)
                                    )
                                    Text(exercise.name)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    var isAddingExercise by remember { mutableStateOf(false) }

    if (showAddExerciseDialog) {
        AddExerciseDialog(
            isAdding = isAddingExercise,
            onDismiss = { if (!isAddingExercise) showAddExerciseDialog = false },
            onConfirm = { name, group ->
                Log.d("CreateRoutineScreen", "onConfirm triggered: $name, $group")
                try {
                    isAddingExercise = true
                    viewModel.addCustomExercise(
                        name = name,
                        group = group,
                        onError = { error ->
                            Log.e("CreateRoutineScreen", "Error adding exercise: $error")
                            isAddingExercise = false
                            scope.launch {
                                snackbarHostState.showSnackbar(message = error)
                            }
                        },
                        onAdded = { newExercise ->
                            Log.d("CreateRoutineScreen", "Exercise received in callback: ${newExercise.name}, ID: ${newExercise.id}")
                            scope.launch {
                                selected.add(newExercise)
                                isAddingExercise = false
                                showAddExerciseDialog = false
                            }
                        }
                    )
                } catch (e: Exception) {
                    Log.e("CreateRoutineScreen", "Fatal error in onConfirm", e)
                    isAddingExercise = false
                }
            }
        )
    }
}

@Composable
fun AddExerciseDialog(
    isAdding: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, MuscleGroup) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf(MuscleGroup.PECHO) }
    var expanded by remember { mutableStateOf(false) }
    
    val purpleThemeColor = Color(0xFF6B4DFF)
    val lightPurpleBg = Color(0xFFFDF7FF)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = lightPurpleBg,
        shape = RoundedCornerShape(28.dp),
        title = { 
            Text(
                "Nuevo Ejercicio", 
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF2D3142)
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del ejercicio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = purpleThemeColor,
                        focusedLabelColor = purpleThemeColor
                    )
                )

                Box {
                    OutlinedButton(
                        onClick = { expanded = true }, 
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, purpleThemeColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = purpleThemeColor)
                    ) {
                        Text("Grupo: ${group.label}")
                    }
                    DropdownMenu(
                        expanded = expanded, 
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        MuscleGroup.values().forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g.label) },
                                onClick = {
                                    group = g
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && !isAdding) onConfirm(name, group) },
                enabled = name.isNotBlank() && !isAdding,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = purpleThemeColor,
                    disabledContainerColor = purpleThemeColor.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                if (isAdding) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Agregar", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isAdding,
                colors = ButtonDefaults.textButtonColors(contentColor = purpleThemeColor)
            ) {
                Text("Cancelar", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
