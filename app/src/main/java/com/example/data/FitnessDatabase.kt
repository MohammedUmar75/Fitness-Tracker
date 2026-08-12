package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WorkoutProgress::class, DietIntake::class, AIRecommendation::class, NutritionAnalysis::class, UserProfileEntity::class, DailyActivityEntity::class],
    version = 4,
    exportSchema = false
)
abstract class FitnessDatabase : RoomDatabase() {
    abstract fun fitnessDao(): FitnessDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun dailyActivityDao(): DailyActivityDao

    companion object {
        fun getDatabase(context: Context): AppDatabase {
            return DatabaseModule.provideAppDatabase(context)
        }
    }
}
