package com.example.data

import android.content.Context

/**
 * Singleton database module providing Room database instances and DAOs for the fitness tracker app.
 */
object DatabaseModule {
    @Volatile
    private var databaseInstance: AppDatabase? = null

    fun provideAppDatabase(context: Context): AppDatabase {
        return databaseInstance ?: synchronized(this) {
            val instance = AppDatabase.getDatabase(context)
            databaseInstance = instance
            instance
        }
    }

    fun provideFitnessDao(context: Context): FitnessDao {
        return provideAppDatabase(context).fitnessDao()
    }

    fun provideUserProfileDao(context: Context): UserProfileDao {
        return provideAppDatabase(context).userProfileDao()
    }
}
