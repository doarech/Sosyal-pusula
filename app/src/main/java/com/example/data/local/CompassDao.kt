package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompassDao {

    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC")
    fun getAllCheckIns(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC LIMIT 1")
    fun getLatestCheckIn(): Flow<CheckInEntity?>

    @Query("SELECT * FROM check_ins WHERE weekString = :week LIMIT 1")
    suspend fun getCheckInByWeek(week: String): CheckInEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckInEntity): Long

    @Query("DELETE FROM check_ins WHERE id = :id")
    suspend fun deleteCheckIn(id: Long)

    @Query("SELECT * FROM daily_tasks WHERE checkInId = :checkInId ORDER BY id ASC")
    fun getTasksForCheckIn(checkInId: Long): Flow<List<DailyTaskEntity>>

    @Query("SELECT * FROM daily_tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<DailyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DailyTaskEntity>)

    @Query("UPDATE daily_tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Long, isCompleted: Boolean)

    @Query("UPDATE daily_tasks SET userReflection = :reflection, isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskReflection(taskId: Long, reflection: String, isCompleted: Boolean)

    @Query("DELETE FROM daily_tasks WHERE checkInId = :checkInId")
    suspend fun deleteTasksForCheckIn(checkInId: Long)

    @Query("SELECT COUNT(*) FROM check_ins")
    suspend fun getCheckInCount(): Int
}
