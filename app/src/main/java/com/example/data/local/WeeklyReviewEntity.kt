package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weekly_reviews",
    foreignKeys = [
        ForeignKey(
            entity = WeeklyPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["planId"])]
)
data class WeeklyReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planId: Long,
    val weekString: String,
    val emotionalRating: Int,           // 1 to 5 (1 = Çok Yalnız/Kopuk, 5 = Çok İyi/Bağlı)
    val helpfulTasksSummary: String,    // Summary or list of helpful tasks
    val challengingTasksSummary: String,// Summary of challenging tasks
    val challengeReasons: String,       // Selected challenge reasons
    val socialChangeLabel: String,      // "Belirgin güçlendi", "Daha açık hissettim", vb.
    val socialChangeNote: String,       // User free text note
    val nextWeekGoal: String,           // Selected priority goal for next week
    val aiReviewSummary: String,        // Empathetic AI evaluation of this review
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
