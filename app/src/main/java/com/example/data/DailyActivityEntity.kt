package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "daily_activity_history")
data class DailyActivityEntity(
    @PrimaryKey val date: String, // format "yyyy-MM-dd"
    val stepCount: Int = 0,
    val distanceKm: Float = 0f,
    val caloriesBurned: Int = 0,
    val activeMinutes: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface DailyActivityDao {
    @Query("SELECT * FROM daily_activity_history WHERE date = :date LIMIT 1")
    fun getActivityForDate(date: String): Flow<DailyActivityEntity?>

    @Query("SELECT * FROM daily_activity_history WHERE date = :date LIMIT 1")
    suspend fun getActivityForDateOnce(date: String): DailyActivityEntity?

    @Query("SELECT * FROM daily_activity_history ORDER BY date DESC")
    fun getAllActivityHistory(): Flow<List<DailyActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateActivity(activity: DailyActivityEntity)

    @Query("DELETE FROM daily_activity_history WHERE date = :date")
    suspend fun deleteActivityForDate(date: String)

    @Query("DELETE FROM daily_activity_history")
    suspend fun clearAllActivityHistory()
}
