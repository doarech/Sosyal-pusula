package com.example.data.ai

val DEFAULT_5_OPTIONS = listOf(
    "Hiçbir Zaman",
    "Çok Nadir",
    "Ara Sıra",
    "Çoğu Zaman",
    "Her Zaman"
)

data class DynamicQuestion(
    val id: Int,
    val text: String,
    val isPositive: Boolean = true, // True = social connection question; False = isolation/loneliness question
    val options: List<String> = DEFAULT_5_OPTIONS
)

data class GeneratedTask(
    val title: String,
    val category: String // "Sosyal", "Dijital Detoks", "Aktivite", "İlk Adım"
)

data class AiAnalysisResult(
    val score: Int,
    val levelLabel: String,
    val summary: String,
    val motivationQuote: String,
    val motivationAuthor: String = "",
    val tasks: List<GeneratedTask>
)
