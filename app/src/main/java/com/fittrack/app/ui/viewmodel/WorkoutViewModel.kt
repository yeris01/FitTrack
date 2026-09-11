package com.fittrack.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.app.data.FitTrackRepository
import com.fittrack.app.data.RoutineExerciseEntity
import com.fittrack.app.data.WorkoutSessionEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel(private val repository: FitTrackRepository, private val userId: Long) : ViewModel() {

    private var currentSession: WorkoutSessionEntity? = null

    private val _sessionId = MutableStateFlow<Long?>(null)
    val sessionId: StateFlow<Long?> = _sessionId

    // --- Temporizador de descanso ---
    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds
    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning

    // --- Cronómetro de sesión total ---
    private val _totalWorkoutSeconds = MutableStateFlow(0)
    val totalWorkoutSeconds: StateFlow<Int> = _totalWorkoutSeconds
    private var totalTimeJob: kotlinx.coroutines.Job? = null

    fun startSession(routineId: Long, routineName: String, exercises: List<RoutineExerciseEntity>, onReady: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.startSession(userId, routineId, routineName)
            currentSession = WorkoutSessionEntity(id = id, userId = userId, routineId = routineId, routineName = routineName)
            _sessionId.value = id
            startTotalTimer()
            onReady(id)
        }
    }

    private fun startTotalTimer() {
        totalTimeJob?.cancel()
        totalTimeJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _totalWorkoutSeconds.value += 1
            }
        }
    }

    fun logSet(exerciseId: Long, exerciseName: String, setNumber: Int, weightKg: Double, reps: Int) {
        val id = _sessionId.value ?: return
        viewModelScope.launch {
            repository.logSet(id, exerciseId, exerciseName, setNumber, weightKg, reps)
        }
    }

    fun getSetsForSession(id: Long) = repository.getSetsForSession(id)

    fun finishSession(onFinished: () -> Unit) {
        val session = currentSession
        if (session == null) {
            // No había ninguna sesión activa (p. ej. la rutina no tenía ejercicios y nunca
            // llegó a crearse la sesión). Antes esto dejaba al usuario atrapado en la pantalla
            // de entrenamiento sin poder salir. Ahora simplemente cerramos la pantalla.
            onFinished()
            return
        }
        viewModelScope.launch {
            try {
                android.util.Log.d("WorkoutViewModel", "Finishing session: ${session.id}")
                totalTimeJob?.cancel()
                repository.finishSession(session)
                android.util.Log.d("WorkoutViewModel", "Session finished successfully")
                onFinished()
            } catch (e: Exception) {
                android.util.Log.e("WorkoutViewModel", "Error finishing session", e)
                onFinished() // Cerramos igual, pero logueamos el error
            }
        }
    }

    fun startRestTimer(totalSeconds: Int) {
        viewModelScope.launch {
            _timerSeconds.value = totalSeconds
            _timerRunning.value = true
            while (_timerSeconds.value > 0 && _timerRunning.value) {
                delay(1000)
                _timerSeconds.value -= 1
            }
            _timerRunning.value = false
        }
    }

    fun cancelRestTimer() {
        _timerRunning.value = false
        _timerSeconds.value = 0
    }
}
