package com.fittrack.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromMuscleGroup(value: MuscleGroup): String = value.name

    @TypeConverter
    fun toMuscleGroup(value: String): MuscleGroup = MuscleGroup.valueOf(value)
}

@Database(
    entities = [
        ExerciseEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        WorkoutSessionEntity::class,
        SetLogEntity::class,
        WeeklyGoalEntity::class,
        PremiumStatusEntity::class,
        UserEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun goalDao(): GoalDao
    abstract fun premiumDao(): PremiumDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fittrack.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seeding on creation using a background thread
                            CoroutineScope(Dispatchers.IO).launch {
                                android.util.Log.d("AppDatabase", "Seeding database from callback...")
                                getInstance(context).exerciseDao().insertAll(SeedData.defaultExercises)
                                android.util.Log.d("AppDatabase", "Seeding complete.")
                            }
                        }
                    })
                    .build()
                INSTANCE = instance

                // También verificamos si está vacía por si acaso falló el callback o fue una migración
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (instance.exerciseDao().count() == 0) {
                            android.util.Log.d("AppDatabase", "Seeding default exercises (manual check)...")
                            instance.exerciseDao().insertAll(SeedData.defaultExercises)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AppDatabase", "Error in manual seed check", e)
                    }
                }
                instance
            }
        }
    }
}

object SeedData {
    val defaultExercises = listOf(
        // Pecho
        ExerciseEntity(name = "Press banca", muscleGroup = MuscleGroup.PECHO),
        ExerciseEntity(name = "Press inclinado", muscleGroup = MuscleGroup.PECHO),
        ExerciseEntity(name = "Aperturas con mancuerna", muscleGroup = MuscleGroup.PECHO),
        ExerciseEntity(name = "Fondos en paralelas", muscleGroup = MuscleGroup.PECHO),
        // Espalda
        ExerciseEntity(name = "Dominadas", muscleGroup = MuscleGroup.ESPALDA),
        ExerciseEntity(name = "Remo con barra", muscleGroup = MuscleGroup.ESPALDA),
        ExerciseEntity(name = "Jalón al pecho", muscleGroup = MuscleGroup.ESPALDA),
        ExerciseEntity(name = "Peso muerto", muscleGroup = MuscleGroup.ESPALDA),
        // Piernas
        ExerciseEntity(name = "Sentadilla", muscleGroup = MuscleGroup.PIERNAS),
        ExerciseEntity(name = "Prensa de piernas", muscleGroup = MuscleGroup.PIERNAS),
        ExerciseEntity(name = "Zancadas", muscleGroup = MuscleGroup.PIERNAS),
        ExerciseEntity(name = "Extensión de cuádriceps", muscleGroup = MuscleGroup.PIERNAS),
        // Hombros
        ExerciseEntity(name = "Press militar", muscleGroup = MuscleGroup.HOMBROS),
        ExerciseEntity(name = "Elevaciones laterales", muscleGroup = MuscleGroup.HOMBROS),
        ExerciseEntity(name = "Pájaros posteriores", muscleGroup = MuscleGroup.HOMBROS),
        // Bíceps
        ExerciseEntity(name = "Curl con barra", muscleGroup = MuscleGroup.BICEPS),
        ExerciseEntity(name = "Curl martillo", muscleGroup = MuscleGroup.BICEPS),
        // Tríceps
        ExerciseEntity(name = "Press francés", muscleGroup = MuscleGroup.TRICEPS),
        ExerciseEntity(name = "Extensión en polea", muscleGroup = MuscleGroup.TRICEPS),
        // Abdominales
        ExerciseEntity(name = "Crunch", muscleGroup = MuscleGroup.ABDOMINALES),
        ExerciseEntity(name = "Plancha", muscleGroup = MuscleGroup.ABDOMINALES),
        // Glúteos
        ExerciseEntity(name = "Hip thrust", muscleGroup = MuscleGroup.GLUTEOS),
        // Cardio
        ExerciseEntity(name = "Cinta de correr", muscleGroup = MuscleGroup.CARDIO),
        ExerciseEntity(name = "Bicicleta estática", muscleGroup = MuscleGroup.CARDIO)
    )
}
