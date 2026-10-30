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

data class QuestionResponse(
    val question: DynamicQuestion,
    val selectedOptionIndex: Int?, // 0 to 4 if an option was chosen; null if pure free-text
    val answerText: String,        // Option text or free-text answer
    val customNote: String = ""    // Optional user explanation or reflection
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

data class GeneratedWeeklyTask(
    val dayNumber: Int,
    val dayLabel: String,
    val title: String,
    val description: String,
    val category: String // "Sosyal", "Dijital Detoks", "Aktivite", "İlk Adım"
)

data class GeneratedWeeklyPlan(
    val mainGoal: String,
    val focusArea: String,
    val tasks: List<GeneratedWeeklyTask>
)

data class GeneratedReviewSummary(
    val summary: String,
    val adviceForNextWeek: String,
    val encouragement: String
)
