package com.fittrack.app.data

class FitTrackRepository(private val db: AppDatabase) {

    // Ejercicios
    fun getAllExercises(userId: Long) = db.exerciseDao().getAll(userId)
    fun getExercisesByGroup(userId: Long, group: MuscleGroup) = db.exerciseDao().getByGroup(userId, group)
    suspend fun getExercisesByNames(names: List<String>): List<ExerciseEntity> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            db.exerciseDao().getByNames(names)
        }
    suspend fun addCustomExercise(userId: Long, name: String, group: MuscleGroup): Long {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                android.util.Log.d("FitTrackRepository", "Inserting exercise: $name for user: $userId")
                val id = db.exerciseDao().insert(ExerciseEntity(name = name, muscleGroup = group, isCustom = true, userId = userId))
                android.util.Log.d("FitTrackRepository", "Insert successful, id: $id")
                id
            } catch (e: Exception) {
                android.util.Log.e("FitTrackRepository", "Error adding custom exercise for userId: $userId", e)
                throw e
            }
        }
    }

    // Rutinas
    fun getAllRoutines(userId: Long) = db.routineDao().getAllRoutines(userId)
    fun getExercisesForRoutine(routineId: Long) = db.routineDao().getExercisesForRoutine(routineId)

    suspend fun createRoutine(userId: Long, name: String, exercises: List<ExerciseEntity>): Long {
        val routineId = db.routineDao().insertRoutine(RoutineEntity(userId = userId, name = name))
        exercises.forEachIndexed { index, ex ->
            db.routineDao().insertRoutineExercise(
                RoutineExerciseEntity(
                    routineId = routineId,
                    exerciseId = ex.id,
                    exerciseName = ex.name,
                    muscleGroup = ex.muscleGroup,
                    orderIndex = index
                )
            )
        }
        return routineId
    }

    suspend fun deleteRoutine(routine: RoutineEntity) = db.routineDao().deleteRoutine(routine)

    // Entrenamiento activo / historial
    suspend fun startSession(userId: Long, routineId: Long, routineName: String): Long =
        db.workoutDao().insertSession(WorkoutSessionEntity(userId = userId, routineId = routineId, routineName = routineName))

    suspend fun finishSession(session: WorkoutSessionEntity) {
        session.finishedAt = System.currentTimeMillis()
        db.workoutDao().updateSession(session)
    }

    suspend fun logSet(sessionId: Long, exerciseId: Long, exerciseName: String, setNumber: Int, weightKg: Double, reps: Int) =
        db.workoutDao().insertSetLog(
            SetLogEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                setNumber = setNumber,
                weightKg = weightKg,
                reps = reps
            )
        )

    fun getSetsForSession(sessionId: Long) = db.workoutDao().getSetsForSession(sessionId)
    fun getAllSessions(userId: Long) = db.workoutDao().getAllSessions(userId)
    fun getRecentSetsForExercise(userId: Long, exerciseId: Long) = db.workoutDao().getRecentSetsForExercise(userId, exerciseId)

    // --- Progreso ---
    fun getAllSetsForExercise(userId: Long, exerciseId: Long) = db.workoutDao().getAllSetsForExercise(userId, exerciseId)

    // --- Calendario ---
    fun getAllSessionDates(userId: Long) = db.workoutDao().getAllSessionDates(userId)

    // --- Objetivos semanales ---
    fun getGoalForWeek(userId: Long, weekStart: Long) = db.goalDao().getGoalForWeek(userId, weekStart)
    fun countSessionsInWeek(userId: Long, weekStart: Long, weekEnd: Long) = db.workoutDao().countSessionsBetween(userId, weekStart, weekEnd)

    suspend fun setWeeklyGoal(userId: Long, weekStart: Long, targetSessions: Int, existingId: Long? = null) {
        db.goalDao().upsertGoal(
            WeeklyGoalEntity(id = existingId ?: 0, userId = userId, weekStartMillis = weekStart, targetSessions = targetSessions)
        )
    }

    // --- Premium ---
    fun getPremiumStatus(userId: Long) = db.premiumDao().getStatus(userId)
    suspend fun setPremiumStatus(userId: Long, isPremium: Boolean) = db.premiumDao().setStatus(PremiumStatusEntity(userId = userId, isPremium = isPremium))
}
