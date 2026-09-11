package com.fittrack.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.app.data.ExerciseEntity
import com.fittrack.app.data.FitTrackRepository
import com.fittrack.app.data.MuscleGroup
import com.fittrack.app.data.RoutineEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutineViewModel(private val repository: FitTrackRepository, val userId: Long) : ViewModel() {

    init {
        Log.d("RoutineViewModel", "Initialized with userId: $userId")
    }

    val routines: StateFlow<List<RoutineEntity>> = repository.getAllRoutines(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises: StateFlow<List<ExerciseEntity>> = repository.getAllExercises(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun exercisesByGroup(group: MuscleGroup) = repository.getExercisesByGroup(userId, group)

    fun exercisesForRoutine(routineId: Long) = repository.getExercisesForRoutine(routineId)

    fun createRoutine(name: String, selected: List<ExerciseEntity>, onCreated: (Long) -> Unit) {
        if (selected.isEmpty()) {
            Log.e("RoutineViewModel", "createRoutine llamado sin ejercicios seleccionados, se ignora")
            return
        }
        viewModelScope.launch {
            val id = repository.createRoutine(userId, name, selected)
            onCreated(id)
        }
    }

    /**
     * Crea una rutina sugerida a partir de nombres de ejercicios "semilla".
     * A diferencia de [createRoutine], busca los ejercicios directamente en la base de datos
     * (suspend, sin depender del StateFlow [allExercises]) para evitar la carrera en la que,
     * si se tocaba el botón antes de que terminara de cargar la lista de ejercicios, se creaba
     * una rutina vacía que luego dejaba la pantalla de entrenamiento cargando para siempre.
     */
    fun createRoutineFromTemplate(
        name: String,
        exerciseNames: List<String>,
        onError: (String) -> Unit,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val exercises = repository.getExercisesByNames(exerciseNames)
            if (exercises.isEmpty()) {
                onError("No se pudieron cargar los ejercicios, intentá de nuevo en un momento")
                return@launch
            }
            val id = repository.createRoutine(userId, name, exercises)
            onCreated(id)
        }
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch { repository.deleteRoutine(routine) }
    }

    fun addCustomExercise(name: String, group: MuscleGroup, onError: (String) -> Unit, onAdded: (ExerciseEntity) -> Unit) {
        Log.d("RoutineViewModel", "addCustomExercise called: $name, group: $group, userId: $userId")
        
        viewModelScope.launch {
            try {
                val id = kotlinx.coroutines.withTimeout(10000) {
                    repository.addCustomExercise(userId, name, group)
                }
                
                Log.d("RoutineViewModel", "Repository returned ID: $id")
                
                if (id > 0) {
                    val exercise = ExerciseEntity(id = id, name = name, muscleGroup = group, isCustom = true, userId = userId)
                    Log.d("RoutineViewModel", "Successfully added, calling onAdded on Main thread")
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onAdded(exercise)
                    }
                } else {
                    Log.e("RoutineViewModel", "Repository returned invalid ID: $id")
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onError("Error al guardar el ejercicio")
                    }
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e("RoutineViewModel", "Timeout adding exercise", e)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError("La base de datos está ocupada, intenta de nuevo")
                }
            } catch (t: Throwable) {
                Log.e("RoutineViewModel", "Critical error adding exercise", t)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError("Error inesperado: ${t.localizedMessage ?: "Error de base de datos"}")
                }
            }
        }
    }
}
