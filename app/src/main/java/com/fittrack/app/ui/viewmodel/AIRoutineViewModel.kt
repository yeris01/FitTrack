package com.fittrack.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.app.ai.AIResult
import com.fittrack.app.ai.AIRoutineService
import com.fittrack.app.data.ExerciseEntity
import com.fittrack.app.data.FitTrackRepository
import com.fittrack.app.data.MuscleGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AIGenerationState {
    object Idle : AIGenerationState()
    object Loading : AIGenerationState()
    data class Success(val routineNames: List<String>) : AIGenerationState()
    data class Error(val message: String) : AIGenerationState()
}

class AIRoutineViewModel(private val repository: FitTrackRepository, private val userId: Long) : ViewModel() {

    private val service = AIRoutineService()

    private val _state = MutableStateFlow<AIGenerationState>(AIGenerationState.Idle)
    val state: StateFlow<AIGenerationState> = _state

    fun generate(daysPerWeek: Int, goal: String, experienceLevel: String, equipment: String) {
        viewModelScope.launch {
            _state.value = AIGenerationState.Loading

            when (val result = service.generateRoutine(daysPerWeek, goal, experienceLevel, equipment)) {
                is AIResult.Error -> _state.value = AIGenerationState.Error(result.message)
                is AIResult.Success -> {
                    val existingExercises = repository.getAllExercises(userId).first()
                    val createdNames = mutableListOf<String>()

                    result.plan.days.forEach { day ->
                        val exercisesForRoutine = day.exercises.map { aiEx ->
                            val group = runCatching { MuscleGroup.valueOf(aiEx.muscleGroup) }
                                .getOrDefault(MuscleGroup.PECHO)

                            // Reutilizamos el ejercicio si ya existe (por nombre y grupo); si no, lo creamos.
                            val match = existingExercises.firstOrNull {
                                it.name.equals(aiEx.name, ignoreCase = true) && it.muscleGroup == group
                            }
                            match ?: ExerciseEntity(
                                id = repository.addCustomExercise(userId, aiEx.name, group),
                                name = aiEx.name,
                                muscleGroup = group,
                                isCustom = true,
                                userId = userId
                            )
                        }

                        repository.createRoutine(userId, day.dayLabel, exercisesForRoutine)
                        createdNames.add(day.dayLabel)
                    }

                    _state.value = AIGenerationState.Success(createdNames)
                }
            }
        }
    }

    fun reset() {
        _state.value = AIGenerationState.Idle
    }
}
