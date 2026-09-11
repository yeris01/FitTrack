package com.fittrack.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE userId IS NULL OR userId = :userId ORDER BY muscleGroup, name")
    fun getAll(userId: Long): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE (userId IS NULL OR userId = :userId) AND muscleGroup = :group ORDER BY name")
    fun getByGroup(userId: Long, group: MuscleGroup): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Insert
    suspend fun insert(exercise: ExerciseEntity): Long

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    // Búsqueda directa (suspend, no depende de que el Flow ya haya emitido) para armar
    // las rutinas sugeridas sin riesgo de carrera con la precarga de datos.
    @Query("SELECT * FROM exercises WHERE name IN (:names)")
    suspend fun getByNames(names: List<String>): List<ExerciseEntity>
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllRoutines(userId: Long): Flow<List<RoutineEntity>>

    @Insert
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    @Insert
    suspend fun insertRoutineExercise(re: RoutineExerciseEntity)

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex")
    fun getExercisesForRoutine(routineId: Long): Flow<List<RoutineExerciseEntity>>

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun clearExercisesForRoutine(routineId: Long)
}

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getAllSessions(userId: Long): Flow<List<WorkoutSessionEntity>>

    @Insert
    suspend fun insertSetLog(setLog: SetLogEntity): Long

    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY id")
    fun getSetsForSession(sessionId: Long): Flow<List<SetLogEntity>>

    @Query("SELECT * FROM set_logs WHERE exerciseId = :exerciseId AND sessionId IN (SELECT id FROM workout_sessions WHERE userId = :userId) ORDER BY loggedAt DESC LIMIT 20")
    fun getRecentSetsForExercise(userId: Long, exerciseId: Long): Flow<List<SetLogEntity>>

    // Para la gráfica de progreso: todo el historial de series de un ejercicio, ordenado por fecha
    @Query("SELECT * FROM set_logs WHERE exerciseId = :exerciseId AND sessionId IN (SELECT id FROM workout_sessions WHERE userId = :userId) ORDER BY loggedAt ASC")
    fun getAllSetsForExercise(userId: Long, exerciseId: Long): Flow<List<SetLogEntity>>

    // Para el calendario: fechas (día) en las que hubo sesiones
    @Query("SELECT DISTINCT startedAt FROM workout_sessions WHERE userId = :userId ORDER BY startedAt")
    fun getAllSessionDates(userId: Long): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE userId = :userId AND startedAt >= :weekStart AND startedAt < :weekEnd")
    fun countSessionsBetween(userId: Long, weekStart: Long, weekEnd: Long): Flow<Int>
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM weekly_goals WHERE userId = :userId AND weekStartMillis = :weekStart LIMIT 1")
    fun getGoalForWeek(userId: Long, weekStart: Long): Flow<WeeklyGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: WeeklyGoalEntity)
}

@Dao
interface PremiumDao {
    @Query("SELECT * FROM premium_status WHERE userId = :userId LIMIT 1")
    fun getStatus(userId: Long): Flow<PremiumStatusEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setStatus(status: PremiumStatusEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert
    suspend fun insertUser(user: UserEntity): Long
}
