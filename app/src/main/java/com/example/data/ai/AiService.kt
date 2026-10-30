package com.example.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.roundToInt

private const val TAG = "AiService"

class AiService {

    private val textModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )
    }

    /**
     * Dynamically generates between 3 and 5 empathetic questions tailored for measuring loneliness.
     * The AI decides the optimal question count (3, 4, or 5) based on context to avoid overwhelming the user.
     * Each question generates its OWN 5 natural, grammatically matching options.
     */
    suspend fun generateDynamicQuestions(): List<DynamicQuestion> = withContext(Dispatchers.IO) {
        val uniqueSeed = UUID.randomUUID().toString().take(6)
        val themes = listOf(
            "sosyal aidiyet, samimi dostluklar ve dijital alışkanlıklar",
            "yüz yüze iletişim, kalabalıktaki yabancılaşma hissi ve paylaşım",
            "duygusal destek, derin sohbetler ve gün içi sosyal enerji",
            "yakın çevre ile bağlar, dinlenilme ihtiyacı ve ekran sonrası hisler",
            "topluluk hissi, içten arkadaşlıklar ve yalnız geçirilen zamanın niteliği"
        ).random()

        val prompt = """
            Kullanıcının haftalık sosyal refah ve yalnızlık düzeyini ölçmek için sorular üret.
            SORU SAYISI KURALI: Kullanıcıyı bunaltmamak ve en isabetli ölçümü yapmak için kendi kararına göre 3 ile 5 arasında (duruma göre 3, 4 veya 5 soru) dinamik soru belirle (en fazla 5 soru üret).
            Odak Teması: $themes (Seed: $uniqueSeed).
            
            ÖNEMLİ KURAL - SEÇENEKLERİN SORUYLA GRAMER VE ANLAM UYUMU:
            Her soru için 5 adet seçenek (options) üret. Seçenekler sorunun soru köküne TAM UYUMLU olmalıdır!
            - Soru "kaç kere / kaç kez / ne kadar sık" diye soruyorsa:
              Örn: ["Hiç olmadı", "1-2 kere", "Birkaç kere", "Çok sık", "Neredeyse her gün"]
            - Soru "hissediyor musun / var mı" diye soruyorsa:
              Örn: ["Hiç hissetmiyorum", "Pek değil", "Kısmen", "Büyük ölçüde", "Tamamen hissediyorum"]
            - Soru "ne sıklıkla / olur mu" diye soruyorsa:
              Örn: ["Hiçbir zaman", "Çok nadir", "Ara sıra", "Çoğu zaman", "Her zaman"]
            - Soru bir düşünceye katılma soruyorsa:
              Örn: ["Kesinlikle katılmıyorum", "Pek katılmıyorum", "Kısmen", "Katılıyorum", "Tamamen katılıyorum"]
            
            Seçenekler her zaman derece olarak en azdan (indeks 0) en çoğa (indeks 4) doğru sıralanmalıdır!
            
            isPositive kuralı:
            - Soru sosyal bağ, aidiyet, dostluk veya anlaşılma soruyorsa: true
            - Soru yalnızlık, yabancılaşma, kopukluk veya boşluk soruyorsa: false

            Sadece aşağıdaki JSON formatında geçerli bir liste döndür:
            [
              {
                "id": 1,
                "text": "Bu hafta sevdiklerinle konuşurken gerçekten dinlendiğini ve anlaşıldığını hissettin mi?",
                "isPositive": true,
                "options": ["Hiç hissetmedim", "Pek hissetmedim", "Kısmen", "Çoğunlukla", "Tamamen hissettim"]
              }
            ]
        """.trimIndent()

        try {
            val response = withTimeoutOrNull(9_000L) {
                textModel.generateContent(prompt)
            }
            val jsonText = response?.text
            if (!jsonText.isNullOrBlank()) {
                val array = JSONArray(cleanJson(jsonText))
                val list = mutableListOf<DynamicQuestion>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val opts = mutableListOf<String>()
                    val optsArray = obj.optJSONArray("options")
                    if (optsArray != null && optsArray.length() == 5) {
                        for (j in 0 until optsArray.length()) {
                            opts.add(optsArray.getString(j))
                        }
                    } else {
                        opts.addAll(listOf("Hiçbir zaman", "Çok nadir", "Ara sıra", "Çoğu zaman", "Her zaman"))
                    }
                    list.add(
                        DynamicQuestion(
                            id = obj.optInt("id", i + 1),
                            text = obj.getString("text"),
                            isPositive = obj.optBoolean("isPositive", true),
                            options = opts
                        )
                    )
                }
                if (list.size >= 3) return@withContext list
            }
        } catch (e: Exception) {
            Log.w(TAG, "Dynamic question generation through Gemini had issue", e)
        }

        getDiverseAiQuestions()
    }

    /**
     * Evaluates user's answers, screen time, personal question notes, and feeling notes.
     * Computes calibrated 0 to 100 score:
     * - 0: Minimum possible loneliness (completely connected, high social fulfillment)
     * - 0 - 25: Düşük Yalnızlık (İyi / Güçlü Sosyal Bağlar)
     * - 26 - 50: Orta Seviye (Dengeli)
     * - 51 - 75: Yüksek Yalnızlık
     * - 76 - 100: Şiddetli Yalnızlık (100 = Maximum isolation)
     */
    /**
     * Evaluates user's answers with structured question responses.
     */
    suspend fun evaluateLonelinessAndGeneratePlan(
        responses: List<QuestionResponse>,
        screenHours: Float,
        userNotes: String
    ): AiAnalysisResult = withContext(Dispatchers.IO) {
        val answersSummary = responses.joinToString("\n") { r ->
            val optPart = if (r.selectedOptionIndex != null) "Seçenek: ${r.answerText} (indeks ${r.selectedOptionIndex}/4, isPositive=${r.question.isPositive})" else "Serbest Yazı: ${r.answerText}"
            val notePart = if (r.customNote.isNotBlank()) " | Ek Not: ${r.customNote}" else ""
            "- Soru: \"${r.question.text}\" -> $optPart$notePart"
        }

        val prompt = """
            Kullanıcı bir sosyal refah ve yalnızlık ölçeğini tamamladı.
            Ekran Süresi: $screenHours saat
            Genel Kullanıcı Duygu Notu: "${if (userNotes.isBlank()) "Belirtilmedi" else userNotes}"
            Kullanıcının Soru Bazlı Yanıtları ve Özel Yazdığı Açıklamalar:
            $answersSummary

            SKORLAMA KURALI (0 ile 100 Puan Skalası):
            - 0 PUAN: En düşük yalnızlık, en güçlü sosyal bağlar, yüksek aidiyet.
            - 100 PUAN: En şiddetli yalnızlık ve derin izolasyon.
            - 0 - 25: "Düşük Yalnızlık (İyi)" (Güçlü bağlar, canlı iletişim).
            - 26 - 50: "Orta Seviye" (Sosyal denge yerinde, ufak iniş çıkışlar).
            - 51 - 75: "Yüksek Yalnızlık" (İçine kapanma, kopukluk hissi).
            - 76 - 100: "Şiddetli Yalnızlık" (Derin izolasyon).
            
            ÖNEMLİ: Kullanıcının soru bazında kendi yazdığı özel serbest metinleri ve hislerini DİKKATE AL; durumu özetlerken ve öneri üretirken bu yazılan detaylara bizzat değin!
            Klinik tanı veya tıbbi teşhis koyma; şefkatli bir sosyal koç gibi yaklaş.
            
            GÖREVİN:
            1. Puanı 0 ile 100 arasında hesapla (score: tamsayı).
            2. Seviye etiketini belirle ("Düşük Yalnızlık (İyi)", "Orta Seviye", "Yüksek Yalnızlık", "Şiddetli Yalnızlık").
            3. Şefkatli, kullanıcının yazdığı detayları anlayan 2-3 cümlelik samimi Türkçe durum özeti (summary) yaz.
            4. 3 adet uygulanabilir mikro-görev (tasks) oluştur. Birisi mutlaka 'cümleye dökme' önerisi olsun.
            5. Gerçek bir düşünürden/yazardan motivasyon sözü (motivationQuote) ve sahibini (motivationAuthor) ekle.

            Çıktıyı SADECE aşağıdaki JSON formatında ver:
            {
              "score": 15,
              "levelLabel": "Düşük Yalnızlık (İyi)",
              "summary": "Sosyal bağların oldukça kuvvetli ve paylaşımların sana güç veriyor.",
              "motivationQuote": "Bir insanı sevmekle başlar her şey.",
              "motivationAuthor": "Sait Faik Abasıyanık",
              "tasks": [
                {"title": "Günün sonunda hissettiğin bir duyguyu buraya tek bir cümleye dök.", "category": "İlk Adım"},
                {"title": "Eski bir dostuna sadece hatır sormak için kısa bir mesaj at.", "category": "Sosyal"},
                {"title": "Akşam yemeğinde telefonu başka bir odada bırak.", "category": "Dijital Detoks"}
              ]
            }
        """.trimIndent()

        try {
            val response = withTimeoutOrNull(9_000L) {
                textModel.generateContent(prompt)
            }
            val jsonText = response?.text
            if (!jsonText.isNullOrBlank()) {
                val json = JSONObject(cleanJson(jsonText))
                val score = json.getInt("score").coerceIn(0, 100)
                val levelLabel = json.optString("levelLabel", getFallbackLevelLabel(score))
                val summary = json.getString("summary")
                val motivationQuote = json.optString("motivationQuote", "İnsan, insanın sığınağıdır.")
                val motivationAuthor = json.optString("motivationAuthor", "Cemil Meriç")

                val tasksList = mutableListOf<GeneratedTask>()
                val tasksArray = json.optJSONArray("tasks")
                if (tasksArray != null) {
                    for (i in 0 until tasksArray.length()) {
                        val taskObj = tasksArray.getJSONObject(i)
                        tasksList.add(
                            GeneratedTask(
                                title = taskObj.getString("title"),
                                category = taskObj.optString("category", "İlk Adım")
                            )
                        )
                    }
                }
                if (tasksList.isEmpty()) {
                    tasksList.addAll(getFallbackTasks(score))
                }

                return@withContext AiAnalysisResult(
                    score = score,
                    levelLabel = levelLabel,
                    summary = summary,
                    motivationQuote = motivationQuote,
                    motivationAuthor = motivationAuthor,
                    tasks = tasksList
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "AI evaluation timed out or had network issue, using calibrated instant calculation", e)
        }

        // Calibrated deterministic calculation
        val fallbackScore = calculateHeuristicScore(responses, screenHours)
        val fallbackQuotes = listOf(
            "İnsan, insanın kurdu değil; insan, insanın sığınağıdır." to "Cemil Meriç",
            "Yalnız kalma cesaretini gösteremeyen insan, başkalarıyla da sahici bir bağ kuramaz." to "Erich Fromm",
            "Karanlığa küfredeceğine bir mum yak." to "Konfüçyüs",
            "Yalnızlık paylaşılamaz; paylaşılsa yalnızlık olmaz." to "Özdemir Asaf",
            "Bir insanı sevmekle başlar her şey." to "Sait Faik Abasıyanık",
            "Sevgiyle bakılan her şey güzeldir." to "Mevlânâ"
        ).random()

        AiAnalysisResult(
            score = fallbackScore,
            levelLabel = getFallbackLevelLabel(fallbackScore),
            summary = when {
                fallbackScore <= 25 -> "Harika bir sosyal bağ gücün var! Çevrendeki insanlarla olan iletişimin sana huzur ve güçlü bir aidiyet kazandırıyor."
                fallbackScore <= 50 -> "Sosyal hayatında genel olarak dengeli bir dönemdesin. Sevdiklerinle yapacağın küçük ve samimi paylaşımlar sana çok iyi gelecektir."
                fallbackScore <= 75 -> "Son günlerde biraz içine çekilmiş ve insanlarla arandaki mesafeyi açmış olabilirsin. Küçük adımlarla bağlarını tazeleyebilirsin."
                else -> "Yoğun bir yalnızlık veya kopukluk hissettiğin bir süreçtesin. Kendine şefkat gösterip güvendiğin bir dostuna ulaşmak iyi bir ilk adım olacaktır."
            },
            motivationQuote = fallbackQuotes.first,
            motivationAuthor = fallbackQuotes.second,
            tasks = getFallbackTasks(fallbackScore)
        )
    }

    /**
     * Backwards-compatible overload for string pairs.
     */
    @JvmName("evaluateLonelinessWithAnswers")
    suspend fun evaluateLonelinessAndGeneratePlan(
        answers: List<Pair<String, String>>,
        screenHours: Float,
        userNotes: String
    ): AiAnalysisResult {
        val adaptedResponses = answers.mapIndexed { idx, pair ->
            QuestionResponse(
                question = DynamicQuestion(id = idx + 1, text = pair.first, isPositive = true),
                selectedOptionIndex = inferOptionIndex(pair.second),
                answerText = pair.second,
                customNote = ""
            )
        }
        return evaluateLonelinessAndGeneratePlan(adaptedResponses, screenHours, userNotes)
    }

    /**
     * Generates a complete 7-day personalized weekly development program tailored to user's score,
     * preferences, and previous weekly reviews.
     */
    suspend fun generateWeeklyPlan(
        score: Int,
        userPreferences: String = "",
        previousReview: String? = null
    ): GeneratedWeeklyPlan = withContext(Dispatchers.IO) {
        val prompt = """
            Kullanıcı için 7 günlük kişisel sosyal gelişim ve denge haftalık programı hazırla.
            Yalnızlık Skoru: $score / 100
            Kullanıcı Tercihleri: "${if (userPreferences.isBlank()) "Genel sosyal denge" else userPreferences}"
            Önceki Hafta Değerlendirmesi: "${previousReview ?: "İlk hafta başlangıcı"}"

            GEREKSİNİMLER:
            1. Haftalık Ana Hedef (mainGoal): Ulaşılabilir, şefkatli, motive edici 1 cümlelik haftalık ana amaç.
            2. Odak Alanı (focusArea): "Sosyal Bağlar", "Dijital Farkındalık", "İçten İletişim" veya "Yeni Adımlar".
            3. 7 Günlük Görevler (tasks): Gün 1'den Gün 7'ye sıralı, zorlayıcı olmayan, gerçekçi 7 mikro eylem:
               - Gün 1: Pazartesi
               - Gün 2: Salı
               - Gün 3: Çarşamba
               - Gün 4: Perşembe
               - Gün 5: Cuma
               - Gün 6: Cumartesi
               - Gün 7: Pazar
               Her görev için dayNumber (1-7), dayLabel, title, 1-2 cümlelik açıklayıcı description ve category ("Sosyal", "Dijital Detoks", "Aktivite", "İlk Adım") belirle.

            Çıktıyı SADECE aşağıdaki JSON formatında ver:
            {
              "mainGoal": "Bu hafta sevdiklerinle derin ve kesintisiz paylaşımlar oluştur.",
              "focusArea": "Sosyal Bağlar",
              "tasks": [
                {"dayNumber": 1, "dayLabel": "1. Gün - Pazartesi", "title": "Günün bir anını cümleye dök.", "description": "Bugün hissettiğin bir duyguyu not alarak farkındalık kazan.", "category": "İlk Adım"},
                {"dayNumber": 2, "dayLabel": "2. Gün - Salı", "title": "Bir arkadaşına sesli mesaj at.", "description": "Kısa ve samimi bir ses kaydıyla hatır sor.", "category": "Sosyal"},
                {"dayNumber": 3, "dayLabel": "3. Gün - Çarşamba", "title": "Akşam 1 saat ekransız mola ver.", "description": "Yemekte veya dinlenirken telefonu uzak tut.", "category": "Dijital Detoks"},
                {"dayNumber": 4, "dayLabel": "4. Gün - Perşembe", "title": "Birlikte bir kahve/çay iç.", "description": "Yüz yüze 15 dakikalık sıcak bir sohbet yap.", "category": "Sosyal"},
                {"dayNumber": 5, "dayLabel": "5. Gün - Cuma", "title": "Haftanın bir güzelliğini paylaş.", "description": "Sevdiğin biriyle haftanın keyifli bir anısını konuş.", "category": "Aktivite"},
                {"dayNumber": 6, "dayLabel": "6. Gün - Cumartesi", "title": "Açık havada kısa bir yürüyüş yap.", "description": "Etraftaki insanları ve çevreyi gözlemleyerek yürü.", "category": "Aktivite"},
                {"dayNumber": 7, "dayLabel": "7. Gün - Pazar", "title": "Haftalık değerlendirmeni tamamla.", "description": "Bu hafta sana neyin iyi geldiğini gözden geçir.", "category": "İlk Adım"}
              ]
            }
        """.trimIndent()

        try {
            val response = withTimeoutOrNull(9_000L) {
                textModel.generateContent(prompt)
            }
            val jsonText = response?.text
            if (!jsonText.isNullOrBlank()) {
                val json = JSONObject(cleanJson(jsonText))
                val mainGoal = json.getString("mainGoal")
                val focusArea = json.optString("focusArea", "Sosyal Bağlar")
                val tasksArray = json.getJSONArray("tasks")
                val list = mutableListOf<GeneratedWeeklyTask>()
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    list.add(
                        GeneratedWeeklyTask(
                            dayNumber = obj.optInt("dayNumber", i + 1),
                            dayLabel = obj.optString("dayLabel", "${i + 1}. Gün"),
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "Sosyal")
                        )
                    )
                }
                if (list.size == 7) {
                    return@withContext GeneratedWeeklyPlan(mainGoal, focusArea, list)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "AI weekly plan generation error, using fallback plan", e)
        }

        getFallbackWeeklyPlan(score)
    }

    /**
     * Evaluates the 5-step weekly review and produces constructive, compassionate guidance.
     */
    suspend fun evaluateWeeklyReview(
        emotionalRating: Int,
        helpfulTasks: List<String>,
        challenges: List<String>,
        reasons: List<String>,
        socialChange: String,
        nextGoal: String
    ): GeneratedReviewSummary = withContext(Dispatchers.IO) {
        val prompt = """
            Kullanıcı 7 günlük haftalık programını tamamladı ve 5 adımlı haftalık değerlendirme yaptı:
            - Haftalık Duygusal Durum (1-5): $emotionalRating / 5
            - En Çok Fayda Sağlayan Görevler: ${helpfulTasks.ifEmpty { listOf("Belirtilmedi") }.joinToString(", ")}
            - Zorlayan Görevler: ${challenges.ifEmpty { listOf("Yok") }.joinToString(", ")}
            - Zorlanma Nedenleri: ${reasons.ifEmpty { listOf("Yok") }.joinToString(", ")}
            - Gözlemlenen Sosyal Değişim: $socialChange
            - Gelecek Hafta Ana Hedefi: $nextGoal

            ÖNEMLİ KURAL:
            Tamamlanamayan veya zorlayan görevleri asla başarısızlık olarak görme! Şefkatli, kabullenici, motive edici ve yapıcı bir değerlendirme yap.

            SADECE aşağıdaki JSON formatında cevap ver:
            {
              "summary": "Bu hafta kendine zaman ayırıp küçük adımlar atman çok değerliydi. Özellikle sesli mesaj adımının sana iyi geldiği anlaşılıyor.",
              "adviceForNextWeek": "Gelecek hafta belirlediğin hedefe odaklanırken acele etmeden, kendi temponda ilerlemeyi hatırla.",
              "encouragement": "Her küçük paylaşım, yalnızlığın duvarlarına açılan birer penceredir."
            }
        """.trimIndent()

        try {
            val response = withTimeoutOrNull(8_000L) {
                textModel.generateContent(prompt)
            }
            val jsonText = response?.text
            if (!jsonText.isNullOrBlank()) {
                val json = JSONObject(cleanJson(jsonText))
                return@withContext GeneratedReviewSummary(
                    summary = json.getString("summary"),
                    adviceForNextWeek = json.optString("adviceForNextWeek", "Kendi hızında, küçük ve samimi adımlarla ilerlemeye devam et."),
                    encouragement = json.optString("encouragement", "Her temas yeni bir başlangıçtır.")
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "AI review evaluation error, using fallback", e)
        }

        GeneratedReviewSummary(
            summary = "Bu hafta gösterdiğin çaba ve farkındalık takdire şayan. Zorlandığın anlar bile sana kendi ihtiyaçların hakkında önemli ipuçları verdi.",
            adviceForNextWeek = "Gelecek hafta için seçtiğin '$nextGoal' hedefine odaklanırken kendini baskı altına almadan, küçük temasların tadını çıkar.",
            encouragement = "İnsan, insanın sığınağıdır; küçük adımlar büyük yakınlıklar doğurur."
        )
    }

    /**
     * DETERMINISTIC LONELINESS SCORING ALGORITHM:
     * 
     * Scale: 0 to 100 points.
     * - 0: Minimum loneliness / Optimal social connection and emotional fulfillment.
     * - 100: Maximum loneliness / Profound isolation.
     * 
     * POLARITY MATHEMATICS:
     * 1. Positive questions (question.isPositive == true):
     *    Measures connectedness, belonging, feeling listened to, having trusted companions.
     *    - Option 4 ("Tamamen / Her zaman"): 0 loneliness points (Best social connection)
     *    - Option 3 ("Büyük ölçüde / Çoğu zaman"): 1 loneliness point
     *    - Option 2 ("Kısmen / Ara sıra"): 2 loneliness points
     *    - Option 1 ("Pek değil / Nadiren"): 3 loneliness points
     *    - Option 0 ("Hiç / Yok"): 4 loneliness points (Maximum loneliness contribution)
     *    Formula: points = 4 - index
     * 
     * 2. Negative questions (question.isPositive == false):
     *    Measures isolation, feeling misunderstood, emptiness after screen time, alienation.
     *    - Option 0 ("Hiç / Asla"): 0 loneliness points (Best - no isolation)
     *    - Option 1 ("Nadir / 1-2 kere"): 1 loneliness point
     *    - Option 2 ("Ara sıra / Kısmen"): 2 loneliness points
     *    - Option 3 ("Sık sık / Çoğu zaman"): 3 loneliness points
     *    - Option 4 ("Her zaman / Neredeyse her gün"): 4 loneliness points (Maximum loneliness contribution)
     *    Formula: points = index
     * 
     * FREE-TEXT & MISSING HANDLING:
     * - Free-text responses (selectedOptionIndex == null) are NEVER converted to default option indices.
     *   They are reserved strictly for qualitative Gemini prompts.
     * - Missing answers are not counted as valid and are not automatically assumed positive.
     * - Total fraction = sum(points) / (validCount * 4).
     * - If validCount == 0: baseline score is 25 (neutral).
     * 
     * DISCLAIMER:
     * This evaluation does NOT constitute a clinical diagnosis and is intended for personal awareness tracking.
     */
    fun calculateHeuristicScore(
        responses: List<QuestionResponse>,
        screenHours: Float = 0f
    ): Int {
        val validResponses = responses.filter { it.selectedOptionIndex != null && it.selectedOptionIndex in 0..4 }
        if (validResponses.isEmpty()) return 25

        var rawSum = 0
        for (r in validResponses) {
            val idx = r.selectedOptionIndex!!
            val points = if (r.question.isPositive) {
                4 - idx
            } else {
                idx
            }
            rawSum += points
        }

        val maxPossible = validResponses.size * 4
        val fraction = (rawSum.toFloat() / maxPossible.toFloat()).coerceIn(0f, 1f)
        var calculatedScore = (fraction * 100f).roundToInt()

        // Minor calibrated adjustment for device screen time (within 1..99 range)
        if (calculatedScore in 1..99) {
            if (screenHours > 7.0f) calculatedScore += 3
            else if (screenHours < 3.0f && screenHours > 0.1f) calculatedScore -= 3
        }

        return calculatedScore.coerceIn(0, 100)
    }

    /**
     * Helper to infer option index from text if migrating from legacy answer pairs.
     */
    private fun inferOptionIndex(answer: String): Int? {
        val ans = answer.trim()
        if (ans.isBlank()) return null
        return when {
            ans.contains("Hiç", ignoreCase = true) || ans.contains("Asla", ignoreCase = true) || ans.contains("Kesinlikle katılmı", ignoreCase = true) || ans.contains("Yok", ignoreCase = true) -> 0
            ans.contains("Nadir", ignoreCase = true) || ans.contains("1-2", ignoreCase = true) || ans.contains("1 kişi", ignoreCase = true) || ans.contains("Pek", ignoreCase = true) -> 1
            ans.contains("Ara sıra", ignoreCase = true) || ans.contains("Birkaç", ignoreCase = true) || ans.contains("Kısmen", ignoreCase = true) || ans.contains("Belki", ignoreCase = true) || ans.contains("Bazen", ignoreCase = true) -> 2
            ans.contains("Çoğu", ignoreCase = true) || ans.contains("Çok", ignoreCase = true) || ans.contains("Katılıyorum", ignoreCase = true) || ans.contains("Büyük", ignoreCase = true) -> 3
            ans.contains("Her zaman", ignoreCase = true) || ans.contains("Tamamen", ignoreCase = true) || ans.contains("Her gün", ignoreCase = true) -> 4
            else -> null // Unknown or free text
        }
    }

    fun getFallbackWeeklyPlan(score: Int): GeneratedWeeklyPlan {
        val focus = if (score >= 51) "Küçük Adımlar & Öz Şefkat" else "Sosyal Paylaşım & Derin Bağlar"
        val goal = if (score >= 51) {
            "Bu hafta kendini zorlamadan, küçük ve güvenli temaslarla sosyal enerjini yeniden canlandır."
        } else {
            "Bu hafta sevdiklerinle daha derin ve anlamlı yüz yüze paylaşımlar inşa et."
        }
        val tasks = listOf(
            GeneratedWeeklyTask(1, "1. Gün - Pazartesi", "Günün hissini tek cümleye dök.", "Bugün seni en çok düşündüren hissi buraya yazarak iç dünyanı fark et.", "İlk Adım"),
            GeneratedWeeklyTask(2, "2. Gün - Salı", "Eski bir dosta kısa bir selam ver.", "Aklına gelen bir yakınına 'Aklıma geldin, nasılsın?' mesajı ilet.", "Sosyal"),
            GeneratedWeeklyTask(3, "3. Gün - Çarşamba", "Akşam yemeğinde telefonu masadan uzak tut.", "Ekrandan bağımsız, anın lezzetine ve ortamına odaklan.", "Dijital Detoks"),
            GeneratedWeeklyTask(4, "4. Gün - Perşembe", "Günün içinde birine içten teşekkür et.", "İş arkadaşına veya esnafa içten bir tebessümle teşekkür et.", "Sosyal"),
            GeneratedWeeklyTask(5, "5. Gün - Cuma", "Seni heyecanlandıran bir konuyu biriyle paylaş.", "İlgi duyduğun bir kitap, müzik veya haberi birine anlat.", "Aktivite"),
            GeneratedWeeklyTask(6, "6. Gün - Cumartesi", "Açık havada ekransız 20 dakika geçir.", "Doğayı veya sokaktaki canlılığı hissederek kısa bir yürüyüş yap.", "Aktivite"),
            GeneratedWeeklyTask(7, "7. Gün - Pazar", "Haftalık değerlendirmeni tamamla.", "Bu hafta hangi adımların sana iyi geldiğini gözden geçir.", "İlk Adım")
        )
        return GeneratedWeeklyPlan(goal, focus, tasks)
    }

    private fun cleanJson(raw: String): String {
        return raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun getFallbackLevelLabel(score: Int): String {
        return when {
            score >= 76 -> "Şiddetli Yalnızlık"
            score >= 51 -> "Yüksek Yalnızlık"
            score >= 26 -> "Orta Seviye"
            else -> "Düşük Yalnızlık (İyi)"
        }
    }

    /**
     * Multiple diverse question sets with natural, grammatically matching options (3-5 questions).
     */
    private fun getDiverseAiQuestions(): List<DynamicQuestion> {
        val pool1 = listOf(
            DynamicQuestion(
                id = 1,
                text = "Bu hafta sevdiklerinle konuşurken gerçekten dinlendiğini ve anlaşıldığını hissettin mi?",
                isPositive = true,
                options = listOf("Hiç hissetmedim", "Pek hissetmedim", "Kısmen", "Büyük oranda", "Tamamen hissettim")
            ),
            DynamicQuestion(
                id = 2,
                text = "İçini dökmek veya dertleşmek istediğinde başvurabileceğin bir yakının var mı?",
                isPositive = true,
                options = listOf("Kesinlikle yok", "Pek yok", "Belki 1 kişi", "Birkaç kişi var", "Her an ulaşabileceğim yakınlarım var")
            ),
            DynamicQuestion(
                id = 3,
                text = "Bu hafta kaç kez bir ortama veya kalabalığa yabancılaşmış hissettin?",
                isPositive = false,
                options = listOf("Hiç olmadı", "1-2 kere", "Birkaç kere", "Çok sık", "Neredeyse her gün")
            ),
            DynamicQuestion(
                id = 4,
                text = "Telefonunda sosyal medyada gezinirken zamanın nasıl geçtiğini unutup sonrasında boşluk hissi yaşadın mı?",
                isPositive = false,
                options = listOf("Asla yaşamadım", "Çok nadir", "Ara sıra", "Sık sık", "Neredeyse her zaman")
            ),
            DynamicQuestion(
                id = 5,
                text = "Kendini bir arkadaş grubunun ya da topluluğun samimi bir parçası olarak görüyor musun?",
                isPositive = true,
                options = listOf("Hiç görmüyorum", "Pek sayılmaz", "Kısmen", "Büyük ölçüde", "Tamamen ait hissediyorum")
            )
        )

        val pool2 = listOf(
            DynamicQuestion(
                id = 1,
                text = "Son günlerde birileriyle göz göze gelip kahkaha attığın veya içten bir an paylaştığın oldu mu?",
                isPositive = true,
                options = listOf("Hiç olmadı", "1 kere", "Birkaç kere", "Sık sık oldu", "Her gün keyifle paylaştım")
            ),
            DynamicQuestion(
                id = 2,
                text = "Seni gerçekten sen olduğun için önemseyen insanların varlığına inanıyor musun?",
                isPositive = true,
                options = listOf("Hiç inanmıyorum", "Pek inanmıyorum", "Kısmen", "İnanıyorum", "Gönülden inanıyorum")
            ),
            DynamicQuestion(
                id = 3,
                text = "Günün sonunda konuşacak kimsenin olmadığını düşünüp üzüldüğün anlar ne sıklıkla oluyor?",
                isPositive = false,
                options = listOf("Hiç olmuyor", "Çok nadir", "Ara sıra", "Çoğu zaman", "Her gün oluyor")
            ),
            DynamicQuestion(
                id = 4,
                text = "Yalnızlık hissini bastırmak için telefonuna veya ekranlara sığındığın anlar oluyor mu?",
                isPositive = false,
                options = listOf("Asla sığınmıyorum", "Çok nadir", "Ara sıra", "Sık sık", "Her zaman sığınıyorum")
            )
        )

        val pool3 = listOf(
            DynamicQuestion(
                id = 1,
                text = "Bu hafta sevdiğin bir arkadaşınla spontane ve plansız bir sohbet ettin mi?",
                isPositive = true,
                options = listOf("Hiç etmedim", "Pek olmadı", "1-2 kere", "Birkaç defa", "Sık sık sohbet ettik")
            ),
            DynamicQuestion(
                id = 2,
                text = "Duygularını yargılanma korkusu olmadan anlatabileceğin bir yakının var mı?",
                isPositive = true,
                options = listOf("Kesinlikle yok", "Pek yok", "Belki var", "Evet, var", "Güvendiğim birçok insan var")
            ),
            DynamicQuestion(
                id = 3,
                text = "Sosyal ortamlarda bulunurken bile kendini yalnız hissettiğin oldu mu?",
                isPositive = false,
                options = listOf("Hiç olmadı", "Çok nadir", "Ara sıra", "Çoğu zaman", "Her zaman öyle hissediyorum")
            ),
            DynamicQuestion(
                id = 4,
                text = "Ekrandan başını kaldırdığında gerçek dünyada kendini ait ve güvende hissediyor musun?",
                isPositive = true,
                options = listOf("Hiç hissetmiyorum", "Pek değil", "Kısmen", "Büyük oranda", "Tamamen güvende hissediyorum")
            )
        )

        return listOf(pool1, pool2, pool3).random()
    }

    private fun getFallbackTasks(score: Int): List<GeneratedTask> {
        return if (score >= 51) {
            listOf(
                GeneratedTask("Günün sonunda hissettiğin bir duyguyu buraya tek bir cümleye dök.", "İlk Adım"),
                GeneratedTask("Eski bir dostuna sadece 'Nasılsın, aklıma geldin' mesajı yaz.", "Sosyal"),
                GeneratedTask("Uyumadan önceki son 30 dakikayı ekransız, sakin bir müzikle geçir.", "Dijital Detoks"),
                GeneratedTask("En sevdiğin kafeye gidip sadece anı hissederek bir kahve iç.", "Aktivite")
            )
        } else {
            listOf(
                GeneratedTask("Bugün seni mutlu eden veya şükrettiğin 1 şeyi tek bir cümleye dök.", "İlk Adım"),
                GeneratedTask("Bir arkadaşına veya çalışma arkadaşına içten bir tebessümle günaydın de.", "Sosyal"),
                GeneratedTask("Hafta sonu için sevdiklerinle ortak keyifli bir buluşma planı yap.", "Aktivite"),
                GeneratedTask("Akşam yemeğinde telefonu masadan uzak tutarak sevdiklerine odaklan.", "Dijital Detoks")
            )
        }
    }
}
