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
    suspend fun evaluateLonelinessAndGeneratePlan(
        answers: List<Pair<String, String>>,
        screenHours: Float,
        userNotes: String
    ): AiAnalysisResult = withContext(Dispatchers.IO) {
        val answersSummary = answers.joinToString("\n") { "- Soru: ${it.first} -> Cevap / Not: ${it.second}" }
        val prompt = """
            Kullanıcı bir sosyal refah ve yalnızlık ölçeğini tamamladı.
            Ekran Süresi: $screenHours saat
            Genel Kullanıcı Duygu Notu: "${if (userNotes.isBlank()) "Belirtilmedi" else userNotes}"
            Kullanıcının Soru Bazlı Yanıtları ve Özel Yazdığı Açıklamalar:
            $answersSummary

            SKORLAMA KURALI (0 ile 100 Puan Skalası):
            - 0 PUAN: En düşük yalnızlık, en güçlü sosyal bağlar, mükemmel refah. Kullanıcı tüm sorulara olumlu/sosyal yanıtlar verdiyse skor 0 (veya 0-10) OLABİLİR!
            - 100 PUAN: En şiddetli yalnızlık ve derin izolasyon. Kullanıcı tamamen yalnız hissettiğini belirttiyse skor 95-100 OLABİLİR!
            - 0 - 25: "Düşük Yalnızlık (İyi)" (Güçlü bağlar, canlı iletişim).
            - 26 - 50: "Orta Seviye" (Sosyal denge yerinde, ufak iniş çıkışlar).
            - 51 - 75: "Yüksek Yalnızlık" (İçine kapanma, kopukluk hissi).
            - 76 - 100: "Şiddetli Yalnızlık" (Derin izolasyon).
            
            ÖNEMLİ: Kullanıcının soru bazında kendi yazdığı özel açıklamaları ve hislerini DİKKATE AL; durumu özetlerken ve öneri/görev üretirken bu yazılan detaylara bizzat değin!
            
            GÖREVİN:
            1. Puanı 0 ile 100 arasında hesapla (score: tamsayı).
            2. Seviye etiketini belirle ("Düşük Yalnızlık (İyi)", "Orta Seviye", "Yüksek Yalnızlık", "Şiddetli Yalnızlık").
            3. Şefkatli, kullanıcının yazdığı detayları anlayan 2-3 cümlelik samimi Türkçe durum özeti (summary) yaz.
            4. 3 adet uygulanabilir mikro-görev (tasks) oluştur. Kullanıcının notlarına uygun kişiselleştir. Birisi mutlaka 'cümleye dökme' önerisi olsun.
            5. Tarihten gerçek bir düşünürden/yazardan (Mevlânâ, Erich Fromm, Cemil Meriç, Konfüçyüs, Sait Faik vb.) motivasyon sözü (motivationQuote) ve sahibini (motivationAuthor) ekle.

            Çıktıyı SADECE aşağıdaki JSON formatında ver:
            {
              "score": 15,
              "levelLabel": "Düşük Yalnızlık (İyi)",
              "summary": "Sosyal bağların oldukça kuvvetli ve çevrenle kurduğun samimi paylaşımlar sana güç veriyor.",
              "motivationQuote": "Bir insanı sevmekle başlar her şey.",
              "motivationAuthor": "Sait Faik Abasıyanık",
              "tasks": [
                {"title": "Günün sonunda hissettiğin bir duyguyu buraya tek bir cümleye dök.", "category": "İlk Adım"},
                {"title": "Eski bir dostuna sadece hatır sormak için kısa bir mesaj at.", "category": "Sosyal"},
                {"title": "Akşam yemeğinde telefonu başka bir odada bırakarak anın tadını çıkar.", "category": "Dijital Detoks"}
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
                val motivationQuote = json.optString(
                    "motivationQuote",
                    "İnsan, insanın sığınağıdır."
                )
                val motivationAuthor = json.optString(
                    "motivationAuthor",
                    "Cemil Meriç"
                )

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

        // Calibrated 0-100 scientific calculation in milliseconds
        val fallbackScore = calculateHeuristicScore(answers, screenHours)
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
            summary = if (fallbackScore <= 25) {
                "Harika bir sosyal bağ gücün var! Çevrendeki insanlarla olan iletişimin sana huzur ve güçlü bir aidiyet kazandırıyor."
            } else if (fallbackScore <= 50) {
                "Sosyal hayatında genel olarak dengeli bir dönemdesin. Sevdiklerinle yapacağın küçük ve samimi paylaşımlar sana çok iyi gelecektir."
            } else if (fallbackScore <= 75) {
                "Son günlerde biraz içine çekilmiş ve insanlarla arandaki mesafeyi açmış olabilirsin. Küçük adımlarla bağlarını tazeleyebilirsin."
            } else {
                "Yoğun bir yalnızlık veya kopukluk hissettiğin bir süreçtesin. Kendine şefkat gösterip güvendiğin bir dostuna ulaşmak iyi bir ilk adım olacaktır."
            },
            motivationQuote = fallbackQuotes.first,
            motivationAuthor = fallbackQuotes.second,
            tasks = getFallbackTasks(fallbackScore)
        )
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
     * Accurately calculates 0-100 score by checking question polarity.
     * All best social answers produce exactly 0!
     * All worst isolated answers produce exactly 100!
     */
    private fun calculateHeuristicScore(answers: List<Pair<String, String>>, screenHours: Float): Int {
        if (answers.isEmpty()) return 25

        var rawSum = 0
        answers.forEach { (questionText, answer) ->
            val ans = answer.trim()
            val lower = questionText.lowercase()
            val isNegativeQuestion = lower.contains("yabancılaş") ||
                    lower.contains("kopuk") ||
                    lower.contains("boşluk") ||
                    lower.contains("yalnız") ||
                    lower.contains("dışlan") ||
                    lower.contains("anlaşılmadık") ||
                    lower.contains("kaç kere") ||
                    lower.contains("kaç kez")

            // Index mapping: 0 (en az) to 4 (en çok)
            val index = when {
                ans.contains("Hiç", ignoreCase = true) || ans.contains("Asla", ignoreCase = true) || ans.contains("Kesinlikle katılmı", ignoreCase = true) || ans.contains("Yok", ignoreCase = true) -> 0
                ans.contains("Nadir", ignoreCase = true) || ans.contains("1-2", ignoreCase = true) || ans.contains("1 kişi", ignoreCase = true) || ans.contains("Pek", ignoreCase = true) -> 1
                ans.contains("Ara sıra", ignoreCase = true) || ans.contains("Birkaç", ignoreCase = true) || ans.contains("Kısmen", ignoreCase = true) || ans.contains("Belki", ignoreCase = true) || ans.contains("Bazen", ignoreCase = true) -> 2
                ans.contains("Çoğu", ignoreCase = true) || ans.contains("Çok", ignoreCase = true) || ans.contains("Katılıyorum", ignoreCase = true) || ans.contains("Büyük", ignoreCase = true) || ans.contains("Evet", ignoreCase = true) -> 3
                else -> 4 // "Her zaman", "Tamamen", "Neredeyse her gün", "Pek çok"
            }

            val points = if (isNegativeQuestion) {
                index // 0 to 4
            } else {
                4 - index // 0 (best) to 4 (worst)
            }
            rawSum += points
        }

        val n = answers.size.coerceAtLeast(1)
        val maxPossible = n * 4
        val fraction = (rawSum.toFloat() / maxPossible.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

        // Exact 0..100 scale
        var calculatedScore = (fraction * 100f).roundToInt()

        // Slight screen time adjustment for scores strictly in 1..99
        if (calculatedScore in 1..99) {
            if (screenHours > 7.0f) calculatedScore += 3
            else if (screenHours < 3.0f) calculatedScore -= 3
        }

        return calculatedScore.coerceIn(0, 100)
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
