package com.example

import com.example.data.ai.AiService
import com.example.data.ai.DynamicQuestion
import com.example.data.ai.QuestionResponse
import com.example.data.local.CheckInEntity
import com.example.data.repository.CompassRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val aiService = AiService()

    @Test
    fun `positive questions all answered strongly agree produce 0 loneliness`() {
        // Question: "Sevdiklerinle konuşurken dinlendiğini hissettin mi?" (isPositive = true)
        // Option 4 = "Tamamen hissettim" (Maximum social fulfillment -> 0 loneliness points)
        val q1 = DynamicQuestion(id = 1, text = "Sevdiklerinle anlaşıldığını hissettin mi?", isPositive = true)
        val q2 = DynamicQuestion(id = 2, text = "Güvendiğin bir yakının var mı?", isPositive = true)

        val responses = listOf(
            QuestionResponse(question = q1, selectedOptionIndex = 4, answerText = "Tamamen"),
            QuestionResponse(question = q2, selectedOptionIndex = 4, answerText = "Her zaman")
        )

        val score = aiService.calculateHeuristicScore(responses)
        assertEquals(0, score)
    }

    @Test
    fun `positive questions all answered strongly disagree produce 100 loneliness`() {
        // Question: isPositive = true
        // Option 0 = "Hiç hissetmedim" -> 4 loneliness points (Maximum loneliness)
        val q1 = DynamicQuestion(id = 1, text = "Sevdiklerinle anlaşıldığını hissettin mi?", isPositive = true)
        val q2 = DynamicQuestion(id = 2, text = "Güvendiğin bir yakının var mı?", isPositive = true)

        val responses = listOf(
            QuestionResponse(question = q1, selectedOptionIndex = 0, answerText = "Hiç"),
            QuestionResponse(question = q2, selectedOptionIndex = 0, answerText = "Yok")
        )

        val score = aiService.calculateHeuristicScore(responses)
        assertEquals(100, score)
    }

    @Test
    fun `negative questions all answered strongly agree produce 100 loneliness`() {
        // Question: "Kalabalıkta yalnız hissettin mi?" (isPositive = false)
        // Option 4 = "Her zaman" (Maximum isolation -> 4 loneliness points)
        val q1 = DynamicQuestion(id = 1, text = "Kalabalığa yabancılaşmış hissettin mi?", isPositive = false)
        val q2 = DynamicQuestion(id = 2, text = "Ekrandan sonra boşluk hissi yaşadın mı?", isPositive = false)

        val responses = listOf(
            QuestionResponse(question = q1, selectedOptionIndex = 4, answerText = "Her zaman"),
            QuestionResponse(question = q2, selectedOptionIndex = 4, answerText = "Çok sık")
        )

        val score = aiService.calculateHeuristicScore(responses)
        assertEquals(100, score)
    }

    @Test
    fun `negative questions all answered never produce 0 loneliness`() {
        // Question: isPositive = false
        // Option 0 = "Hiç olmadı" -> 0 loneliness points
        val q1 = DynamicQuestion(id = 1, text = "Kalabalığa yabancılaşmış hissettin mi?", isPositive = false)
        val q2 = DynamicQuestion(id = 2, text = "Ekrandan sonra boşluk hissi yaşadın mı?", isPositive = false)

        val responses = listOf(
            QuestionResponse(question = q1, selectedOptionIndex = 0, answerText = "Hiç olmadı"),
            QuestionResponse(question = q2, selectedOptionIndex = 0, answerText = "Asla")
        )

        val score = aiService.calculateHeuristicScore(responses)
        assertEquals(0, score)
    }

    @Test
    fun `free text answers are excluded from numeric Likert calculation`() {
        val q1 = DynamicQuestion(id = 1, text = "Sevdiklerinle anlaşıldığını hissettin mi?", isPositive = true)
        val q2 = DynamicQuestion(id = 2, text = "Özel durumunu açıkla", isPositive = true)

        val responses = listOf(
            QuestionResponse(question = q1, selectedOptionIndex = 4, answerText = "Tamamen"), // 0 points
            QuestionResponse(question = q2, selectedOptionIndex = null, answerText = "Bazen zorlanıyorum ama iyiyim") // Pure free-text
        )

        // Only q1 participates in the numeric Likert score, giving score 0 instead of defaulting q2
        val score = aiService.calculateHeuristicScore(responses)
        assertEquals(0, score)
    }

    @Test
    fun `correlation calculation strictly requires at least 5 samples`() {
        val fourSamples = (1..4).map { i ->
            CheckInEntity(
                id = i.toLong(),
                weekString = "2026-W$i",
                timestamp = System.currentTimeMillis(),
                score = 30 + i * 5,
                screenHours = 3.0f + i * 0.5f,
                aiSummary = "",
                levelLabel = "",
                motivationQuote = ""
            )
        }

        // Less than 5 samples must return null
        assertNull(CompassRepository.calculateCorrelation(fourSamples))

        val fiveSamples = fourSamples + CheckInEntity(
            id = 5L,
            weekString = "2026-W5",
            timestamp = System.currentTimeMillis(),
            score = 55,
            screenHours = 5.5f,
            aiSummary = "",
            levelLabel = "",
            motivationQuote = ""
        )

        // 5 samples should return a non-null correlation coefficient
        val corr = CompassRepository.calculateCorrelation(fiveSamples)
        assertNotNull(corr)
        assertTrue(corr!! in -1.0f..1.0f)
    }
}
