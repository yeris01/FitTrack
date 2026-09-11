package com.fittrack.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MuscleGroup(val label: String) {
    PECHO("Pecho"),
    ESPALDA("Espalda"),
    PIERNAS("Piernas"),
    HOMBROS("Hombros"),
    BICEPS("Bíceps"),
    TRICEPS("Tríceps"),
    ABDOMINALES("Abdominales"),
    GLUTEOS("Glúteos"),
    CARDIO("Cardio")
}

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroup: MuscleGroup,
    val isCustom: Boolean = false,
    val userId: Long? = null // null para ejercicios globales, ID para personalizados
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val password: String // En una app real, esto debería estar hasheado
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

// Relación rutina <-> ejercicio (con orden dentro de la rutina)
@Entity(tableName = "routine_exercises")
data class RoutineExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: MuscleGroup,
    val orderIndex: Int
)

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val routineId: Long,
    val routineName: String,
    val startedAt: Long = System.currentTimeMillis(),
    var finishedAt: Long? = null
)

@Entity(tableName = "set_logs")
data class SetLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val loggedAt: Long = System.currentTimeMillis()
)

// Objetivo semanal: cuántos entrenamientos querés completar esta semana
@Entity(tableName = "weekly_goals")
data class WeeklyGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val weekStartMillis: Long, // lunes 00:00 de la semana que corresponde
    val targetSessions: Int
)

// Estado simple de la suscripción/compra premium, persistido localmente.
// La fuente de verdad real es Google Play (BillingClient); esto es una caché local.
@Entity(tableName = "premium_status")
data class PremiumStatusEntity(
    @PrimaryKey val userId: Long,
    val isPremium: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)
