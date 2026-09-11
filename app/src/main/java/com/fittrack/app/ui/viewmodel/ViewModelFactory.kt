package com.fittrack.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fittrack.app.data.FitTrackRepository

class ViewModelFactory(
    private val repository: FitTrackRepository,
    private val userId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RoutineViewModel::class.java) ->
                RoutineViewModel(repository, userId) as T
            modelClass.isAssignableFrom(WorkoutViewModel::class.java) ->
                WorkoutViewModel(repository, userId) as T
            modelClass.isAssignableFrom(ProgressViewModel::class.java) ->
                ProgressViewModel(repository, userId) as T
            modelClass.isAssignableFrom(GoalViewModel::class.java) ->
                GoalViewModel(repository, userId) as T
            modelClass.isAssignableFrom(PremiumViewModel::class.java) ->
                PremiumViewModel(repository, userId) as T
            modelClass.isAssignableFrom(AIRoutineViewModel::class.java) ->
                AIRoutineViewModel(repository, userId) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
