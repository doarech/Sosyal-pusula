package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weekly_tasks",
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
data class WeeklyTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planId: Long,
    val dayNumber: Int,               // 1 to 7
    val dayLabel: String,             // e.g. "1. Gün (Pazartesi)"
    val title: String,
    val description: String,
    val category: String,             // "Sosyal", "Dijital Detoks", "Aktivite", "İlk Adım"
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val userReflection: String = ""   // Optional personal notes or "cümleye dök" text
)
