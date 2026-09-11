package com.fittrack.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.app.data.ExerciseEntity
import com.fittrack.app.data.FitTrackRepository
import com.fittrack.app.data.SetLogEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ProgressViewModel(private val repository: FitTrackRepository, private val userId: Long) : ViewModel() {

    val allExercises: StateFlow<List<ExerciseEntity>> = repository.getAllExercises(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun historyFor(exerciseId: Long) = repository.getAllSetsForExercise(userId, exerciseId)

    // Agrupa las series por sesión de entrenamiento y devuelve el peso máximo levantado ese día.
    // Esto da un punto por entrenamiento en vez de un punto por serie, que es más legible en el gráfico.
    fun maxWeightPerSession(sets: List<SetLogEntity>): List<Pair<Long, Double>> {
        return sets
            .groupBy { it.sessionId }
            .map { (_, setsInSession) ->
                val date = setsInSession.minOf { it.loggedAt }
                val maxWeight = setsInSession.maxOf { it.weightKg }
                date to maxWeight
            }
            .sortedBy { it.first }
    }
}
