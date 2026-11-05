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

    @Query("DELETE FROM check_ins WHERE isDemo = 1")
    suspend fun deleteDemoCheckIns()

    // --- Weekly Personal Development Plan ---

    @Query("SELECT * FROM weekly_plans WHERE status = 'ACTIVE' ORDER BY createdAt DESC LIMIT 1")
    fun getActiveWeeklyPlan(): Flow<WeeklyPlanEntity?>

    @Query("SELECT * FROM weekly_plans ORDER BY createdAt DESC")
    fun getAllWeeklyPlans(): Flow<List<WeeklyPlanEntity>>

    @Query("SELECT * FROM weekly_plans WHERE id = :id LIMIT 1")
    suspend fun getWeeklyPlanById(id: Long): WeeklyPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyPlan(plan: WeeklyPlanEntity): Long

    @Query("UPDATE weekly_plans SET status = :status WHERE id = :planId")
    suspend fun updateWeeklyPlanStatus(planId: Long, status: String)

    @Query("DELETE FROM weekly_plans WHERE isDemo = 1")
    suspend fun deleteDemoWeeklyPlans()

    // --- Weekly Tasks (7-Day Program) ---

    @Query("SELECT * FROM weekly_tasks WHERE planId = :planId ORDER BY dayNumber ASC")
    fun getTasksForPlan(planId: Long): Flow<List<WeeklyTaskEntity>>

    @Query("SELECT * FROM weekly_tasks WHERE planId = :planId ORDER BY dayNumber ASC")
    suspend fun getTasksForPlanSync(planId: Long): List<WeeklyTaskEntity>

    @Query("SELECT * FROM weekly_tasks ORDER BY id DESC")
    fun getAllWeeklyTasks(): Flow<List<WeeklyTaskEntity>>

    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestCheckInSync(): CheckInEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyTasks(tasks: List<WeeklyTaskEntity>)

    @Query("UPDATE weekly_tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateWeeklyTaskCompletion(taskId: Long, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE weekly_tasks SET userReflection = :reflection, isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateWeeklyTaskReflection(taskId: Long, reflection: String, isCompleted: Boolean, completedAt: Long?)

    // --- Weekly Reviews ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyReview(review: WeeklyReviewEntity): Long

    @Query("SELECT * FROM weekly_reviews WHERE planId = :planId LIMIT 1")
    fun getReviewForPlan(planId: Long): Flow<WeeklyReviewEntity?>

    @Query("SELECT * FROM weekly_reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<WeeklyReviewEntity>>

    @Query("DELETE FROM weekly_reviews WHERE isDemo = 1")
    suspend fun deleteDemoReviews()
}
