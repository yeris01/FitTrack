package com.fittrack.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.app.data.FitTrackRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

data class WeekProgress(val completed: Int, val target: Int, val goalId: Long?)

class GoalViewModel(private val repository: FitTrackRepository, private val userId: Long) : ViewModel() {

    private fun startOfWeekMillis(): Long {
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private val weekStart = startOfWeekMillis()
    private val weekEnd = weekStart + 7L * 24 * 60 * 60 * 1000

    val weekProgress: StateFlow<WeekProgress> = combine(
        repository.getGoalForWeek(userId, weekStart),
        repository.countSessionsInWeek(userId, weekStart, weekEnd)
    ) { goal, count ->
        WeekProgress(completed = count, target = goal?.targetSessions ?: 0, goalId = goal?.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeekProgress(0, 0, null))

    fun setTarget(target: Int) {
        viewModelScope.launch {
            val currentId = weekProgress.value.goalId
            repository.setWeeklyGoal(userId, weekStart, target, currentId)
        }
    }
}
