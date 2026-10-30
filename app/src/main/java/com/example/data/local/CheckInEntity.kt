package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_ins")
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekString: String,      // e.g. "2026-W42"
    val timestamp: Long,
    val score: Int,              // 20 - 80 scale
    val screenHours: Float,      // Daily average screen hours
    val aiSummary: String,       // Empathetic personal summary
    val levelLabel: String,      // "Düşük Yalnızlık", "Orta Seviye", "Yüksek Yalnızlık", "Şiddetli Yalnızlık"
    val motivationQuote: String, // Daily inspirational quote
    val motivationAuthor: String = "", // Real author of the quote
    val isDemo: Boolean = false // Distinguishes sample/test data from genuine user entries
)
