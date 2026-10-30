package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CompassViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeeklyReviewScreen(
    planId: Long,
    viewModel: CompassViewModel,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val tasksFlow = remember(planId) { viewModel.getWeeklyTasks(planId) }
    val planTasks by tasksFlow.collectAsState(initial = emptyList())

    var currentStep by remember { mutableIntStateOf(1) } // 1 to 5

    // Step 1: Emotional Rating (1..5)
    var emotionalRating by remember { mutableIntStateOf(3) }

    // Step 2: Helpful Tasks
    val selectedHelpfulTasks = remember { mutableStateListOf<String>() }

    // Step 3: Challenging Tasks and Reasons
    val selectedChallengingTasks = remember { mutableStateListOf<String>() }
    val selectedReasons = remember { mutableStateListOf<String>() }

    // Step 4: Social Change
    var socialChangeLabel by remember { mutableStateOf("Kendimi daha açık ve rahat hissettim") }
    var socialChangeNote by remember { mutableStateOf("") }

    // Step 5: Next Week Goal
    var nextWeekGoal by remember { mutableStateOf("Daha fazla yüz yüze görüşme") }
    var customNextGoalText by remember { mutableStateOf("") }

    if (isAnalyzing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Haftalık Değerlendirmen Analiz Ediliyor...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Deneyimlerin, fayda gördüğün adımlar ve zorlandığın noktalar yapay zeka tarafından inceleniyor. Gelecek haftanın yeni 7 günlük programı kişiselleştiriliyor.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .background(MaterialTheme.colorScheme.background)
            .testTag("weekly_review_screen")
    ) {
        // Top App Bar & Progress Dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (currentStep > 1) currentStep--
                    else onBack()
                },
                modifier = Modifier.testTag("weekly_review_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Step Indicator Dots
            Row(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (step in 1..5) {
                    val isActive = step <= currentStep
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .background(
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }

            Text(
                text = "$currentStep / 5",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Scrollable Step Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentStep) {
                1 -> {
                    // Step 1: Emotional Feeling
                    Text(
                        text = "1. Adım: Bu haftayı duygusal ve sosyal açıdan nasıl hissettin?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "İç dünyana şefkatle kulak ver. Hiçbir duygu yanlış değildir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val ratingOptions = listOf(
                        1 to ("😔" to "Yoğun Yalnızlık / Kopuk"),
                        2 to ("🌧️" to "Zorlayıcı / Yorgun"),
                        3 to ("⚖️" to "Dengeli / Fena Değil"),
                        4 to ("🌤️" to "İyi / Sosyal Olarak Canlı"),
                        5 to ("🌟" to "Çok İyi / Derin Bağlar")
                    )

                    ratingOptions.forEach { (rating, labelPair) ->
                        val (emoji, desc) = labelPair
                        val isSelected = emotionalRating == rating
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { emotionalRating = rating }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // Step 2: Helpful Tasks
                    Text(
                        text = "2. Adım: Hangi görevler sana en çok fayda sağladı?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Bu hafta gerçekleştirdiğin veya denediğin, ruhuna iyi gelen eylemleri seçebilirsin (Birden fazla seçilebilir).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (planTasks.isEmpty()) {
                        Text("Bu plana ait görevler yükleniyor...", color = MaterialTheme.colorScheme.outline)
                    } else {
                        planTasks.forEach { task ->
                            val isSelected = selectedHelpfulTasks.contains(task.title)
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedHelpfulTasks.remove(task.title)
                                        else selectedHelpfulTasks.add(task.title)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Filled.Check else Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${task.dayLabel}: ${task.title}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Step 3: Challenging Tasks & Reasons
                    Text(
                        text = "3. Adım: Hangi görevlerde zorlandın ve sence nedeni neydi?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Zorlanmak tamamen doğaldır ve bir başarısızlık değildir. Nerede takıldığını bilmek programını sana göre uyarlamamızı sağlar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Zorlayan veya atlanan görevler:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    planTasks.forEach { task ->
                        val isSelected = selectedChallengingTasks.contains(task.title)
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.2.dp, MaterialTheme.colorScheme.error) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedChallengingTasks.remove(task.title)
                                    else selectedChallengingTasks.add(task.title)
                                }
                        ) {
                            Text(
                                text = "${task.dayLabel}: ${task.title}",
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Zorlanma nedenleri (Varsa seç):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val reasons = listOf(
                        "Zaman yetersizliği",
                        "Sosyal kaygı / çekingenlik",
                        "İlgi duymama",
                        "Uygun ortam yoktu",
                        "Yorgunluk / enerji düşüklüğü",
                        "Baskı altında hissetme"
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        reasons.forEach { r ->
                            val isSelected = selectedReasons.contains(r)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) selectedReasons.remove(r)
                                    else selectedReasons.add(r)
                                },
                                label = { Text(r, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                4 -> {
                    // Step 4: Social Change
                    Text(
                        text = "4. Adım: İlişkilerinde veya gün içi enerjinde nasıl bir değişim gözlemledin?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    val changeOptions = listOf(
                        "Sosyal bağlarım belirgin şekilde güçlendi",
                        "Kendimi daha açık ve rahat hissettim",
                        "Belirgin bir değişim hissetmedim",
                        "İçime kapanma eğilimim devam etti"
                    )

                    changeOptions.forEach { opt ->
                        val isSelected = socialChangeLabel == opt
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { socialChangeLabel = opt }
                        ) {
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Eklemek istediğin kişisel gözlem veya hisler (İsteğe bağlı):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = socialChangeNote,
                        onValueChange = { socialChangeNote = it },
                        placeholder = { Text("Örn: Arkadaşımla kahve içmek gün boyu bana iyi geldi...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )
                }

                5 -> {
                    // Step 5: Next Week's Goal
                    Text(
                        text = "5. Adım: Gelecek hafta için ana odak hedefin ne olsun?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Yapay zeka, yeni 7 günlük programını tam olarak bu hedefe göre şekillendirecektir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val goals = listOf(
                        "Daha fazla yüz yüze samimi sohbet",
                        "Ekran süresini azaltıp anın içinde kalma",
                        "Eski dostlara ve sevdiklere selam verme",
                        "Sosyal ortamlarda ilk adımı atma cesareti",
                        "Kendi Özel Hedefimi Yazacağım"
                    )

                    goals.forEach { g ->
                        val isSelected = nextWeekGoal == g
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { nextWeekGoal = g }
                        ) {
                            Text(
                                text = g,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    if (nextWeekGoal == "Kendi Özel Hedefimi Yazacağım") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customNextGoalText,
                            onValueChange = { customNextGoalText = it },
                            placeholder = { Text("Gelecek hafta için kendi hedefini yaz...", fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Navigation Bar (Back & Next / Finish)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Önceki Adım")
                    }
                }

                Button(
                    onClick = {
                        if (currentStep < 5) {
                            currentStep++
                        } else {
                            val resolvedGoal = if (nextWeekGoal == "Kendi Özel Hedefimi Yazacağım" && customNextGoalText.isNotBlank()) {
                                customNextGoalText.trim()
                            } else {
                                nextWeekGoal
                            }

                            viewModel.submitWeeklyReview(
                                planId = planId,
                                emotionalRating = emotionalRating,
                                helpfulTasks = selectedHelpfulTasks.toList(),
                                challenges = selectedChallengingTasks.toList(),
                                reasons = selectedReasons.toList(),
                                socialChangeLabel = socialChangeLabel,
                                socialChangeNote = socialChangeNote.trim(),
                                nextGoal = resolvedGoal,
                                onSuccess = {
                                    onFinished()
                                }
                            )
                        }
                    },
                    modifier = Modifier.weight(1.3f).testTag("weekly_review_next_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (currentStep == 5) {
                        Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Değerlendirmeyi Tamamla")
                    } else {
                        Text("Sonraki Adım")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
