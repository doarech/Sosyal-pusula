package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_tasks",
    foreignKeys = [
        ForeignKey(
            entity = CheckInEntity::class,
            parentColumns = ["id"],
            childColumns = ["checkInId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["checkInId"])]
)
data class DailyTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val checkInId: Long,
    val title: String,
    val category: String,        // "Sosyal", "Dijital Detoks", "Aktivite", "İlk Adım"
    val isCompleted: Boolean = false,
    val dateString: String,
    val userReflection: String = "" // User's written text, reflection, or draft for this task
)
