package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_plans")
data class WeeklyPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekString: String,           // e.g. "2026-W44"
    val startDate: Long,              // Epoch ms
    val endDate: Long,                // Epoch ms
    val mainGoal: String,             // Central qualitative objective for the week
    val focusArea: String,            // e.g. "Sosyal Bağlar", "Dijital Farkındalık", "İçten Sohbetler"
    val status: String = "ACTIVE",    // "ACTIVE", "COMPLETED"
    val isDemo: Boolean = false,      // Distinguishes seeded demo plans from real plans
    val createdAt: Long = System.currentTimeMillis()
)
